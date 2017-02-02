/*
 * Copyright (C) 2017 Federico Fissore
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.jugtorino.one.msvc.way.rabbit.geoip.reference.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;
import it.jugtorino.one.msvc.way.rabbit.geoip.reference.service.dao.GeoIPDao;
import it.jugtorino.one.msvc.way.rabbit.reference.utils.JSONUtils;

public class Main {

  public static void main(String[] args) throws Exception {
    GeoIPDao geoIPDao = setupDAO(args[0]);
    IPLocation ipLocation = setupIPLocation(geoIPDao);
    Channel channel = setupRabbitMQ();

    RpcServer rpcServer = new RpcServer(channel, "geoip_service_v1") {

      @Override
      public byte[] handleCall(byte[] requestBody, AMQP.BasicProperties replyProperties) {
        try {
          Map<String, Object> request = JSONUtils.toMap(requestBody);

          if (request.get("resolve_ip") == null) {
            return JSONUtils.toBytes(new HashMap<>());
          }
          Map<String, Object> response = handleIPLocationRequest(request, ipLocation);

          System.out.println("REQUEST=" + request);
          System.out.println("RESPONSE=" + response);
          return JSONUtils.toBytes(response);
        } catch (Exception e) {
          Map<String, Object> error = new HashMap<>();
          error.put("message", e.getMessage());
          return JSONUtils.toBytes(error);
        }
      }

    };

    System.out.println("READY");
    rpcServer.mainloop();
  }

  private static Map<String, Object> handleIPLocationRequest(Map<String, Object> request, IPLocation ipLocation) {
    Map<String, Object> body = (Map<String, Object>) request.get("resolve_ip");
    Optional<Map<String, Object>> location = ipLocation.locate(body.get("ip").toString());

    Map<String, Object> response = new HashMap<>();
    response.put("resolve_ip", location.orElse(null));
    return response;
  }

  private static Channel setupRabbitMQ() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    Connection connection = factory.newConnection(Arrays.asList(new Address("localhost")));
    Channel channel = connection.createChannel();
    channel.queueDeclare("geoip_service_v1", false, false, false, null);
    return channel;
  }

  private static IPLocation setupIPLocation(GeoIPDao geoIPDao) {
    return new IPLocation(geoIPDao);
  }

  private static GeoIPDao setupDAO(String arg) throws ClassNotFoundException, SQLException {
    return new GeoIPDao(arg);
  }

}
