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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import it.jugtorino.one.msvc.way.rabbit.reference.utils.JSONUtils;

public class RpcClient {

  private final String callbackQueue;
  private final Channel channel;
  private final String queue;
  private final QueueingConsumer consumer;
  private final Map<String, Consumer<QueueingConsumer.Delivery>> deliveryListeners;

  public RpcClient(Channel channel, String queue) throws IOException {
    this.channel = channel;
    this.queue = queue;
    this.callbackQueue = channel.queueDeclare().getQueue();
    this.consumer = new QueueingConsumer(channel);
    channel.basicConsume(callbackQueue, true, consumer);
    this.deliveryListeners = new ConcurrentHashMap<>();
  }

  public void startConsuming() {
    new Thread(() -> {
      while (true) {
        try {
          Optional.ofNullable(consumer.nextDelivery())
              .ifPresent(delivery -> {
                String correlationId = delivery.getProperties().getCorrelationId();
                Consumer<QueueingConsumer.Delivery> listener = deliveryListeners.remove(correlationId);
                if (listener != null) {
                  listener.accept(delivery);
                }
              });
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }).start();
  }

  public CompletableFuture<Map<String, Object>> send(Map<String, Object> message) throws IOException {
    String correlationId = java.util.UUID.randomUUID().toString();

    AMQP.BasicProperties properties = new AMQP.BasicProperties
        .Builder()
        .correlationId(correlationId)
        .replyTo(callbackQueue)
        .build();

    CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
    deliveryListeners.put(correlationId, (delivery) -> {
      try {
        Map<String, Object> response = new HashMap<>(JSONUtils.toMap(delivery.getBody()));
        future.complete(response);
      } catch (Exception e) {
        future.completeExceptionally(e);
      }
    });

    channel.basicPublish("", queue, properties, JSONUtils.toBytes(message));

    return future;
  }
}
