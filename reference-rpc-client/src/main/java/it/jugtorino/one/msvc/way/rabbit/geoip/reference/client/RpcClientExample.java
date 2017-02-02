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
package it.jugtorino.one.msvc.way.rabbit.geoip.reference.client;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RpcClientExample {

  public static void main(String[] args) throws Exception {
    Channel channel = setupRabbitMQ();

    RpcClient client = new RpcClient(channel, "geoip_service_v1");
    client.startConsuming();

    Map<String, Object> ip = new HashMap<>();
    ip.put("ip", "166.30.201.3");
    Map<String, Object> message = new HashMap<>();
    message.put("resolve_ip", ip);

    client.send(message)
        .thenAccept(System.out::println)
        .thenAccept(r -> System.exit(0));
  }

  private static Channel setupRabbitMQ() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    Connection connection = factory.newConnection(Collections.singletonList(new Address("localhost")));
    Channel channel = connection.createChannel();
    channel.queueDeclare("geoip_service_v1", false, false, false, null);
    return channel;
  }
}
