/*
 * This file is part of bunny, licensed under the MIT License.
 *
 * Copyright (c) 2017 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.bunny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import net.kyori.bunny.message.Message;
import net.kyori.bunny.message.MessageConsumer;
import net.kyori.bunny.message.MessageRegistry;
import net.kyori.membrane.facet.Connectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * An abstract implementation of a queue.
 */
abstract class QueueImpl implements Connectable, Queue {

  private static final Logger LOGGER = LoggerFactory.getLogger(Queue.class);
  @Inject private Bunny bunny;
  @Inject private ObjectMapper mapper;
  @Inject private MessageRegistry mr;
  @Nonnull private final String name;
  private final boolean durable;
  private final boolean exclusive;
  private final boolean autoDelete;
  @Nullable private final Map<String, Object> arguments;
  @Nullable private String consumerTag;
  private final Multimap<TypeToken<? extends Message>, SubscriptionImpl<? extends Message>> consumers = HashMultimap.create();

  /**
   * Constructs a new queue.
   *
   * @param name the queue name
   * @param durable if the queue should be durable (survive a server restart)
   * @param exclusive if this queue is exclusive (restricted to this connection)
   * @param autoDelete if this queue should auto-delete when no longer in use
   * @param arguments other construction arguments
   */
  QueueImpl(@Nonnull final String name, final boolean durable, final boolean exclusive, final boolean autoDelete, @Nullable final Map<String, Object> arguments) {
    this.name = name;
    this.durable = durable;
    this.exclusive = exclusive;
    this.autoDelete = autoDelete;
    this.arguments = arguments;
  }

  @Nonnull
  @Override
  public String name() {
    return this.name;
  }

  @Override
  public boolean durable() {
    return this.durable;
  }

  @Override
  public boolean exclusive() {
    return this.exclusive;
  }

  @Override
  public boolean autoDelete() {
    return this.autoDelete;
  }

  @Nullable
  @Override
  public Map<String, Object> arguments() {
    return this.arguments != null ? Collections.unmodifiableMap(this.arguments) : null;
  }

  @Override
  public void connect() throws IOException, TimeoutException {
    LOGGER.info("Declaring queue '{}'", this);
    this.bunny.channel().queueDeclare(this.name, this.durable, this.exclusive, this.autoDelete, this.arguments);
    this.consumerTag = this.bunny.channel().basicConsume(this.name, false, "", false, true, null, new ConsumerImpl());
    LOGGER.info("Starting consume on '{}' with tag '{}'", this, this.consumerTag);
  }

  @Override
  public void disconnect() throws IOException, TimeoutException {
    if(this.consumerTag != null && this.bunny.active()) {
      LOGGER.info("Cancelling consume on '{}' with tag '{}'", this, this.consumerTag);
      this.bunny.channel().basicCancel(this.consumerTag);
    }
  }

  @Override
  public void bind(@Nonnull final Exchange exchange, @Nonnull final String routingKey) {
    try {
      LOGGER.info("Binding queue '{}' to exchange '{}' with routing key '{}'", this, exchange, routingKey);
      this.bunny.channel().queueBind(this.name, exchange.name(), routingKey, null);
    } catch(final IOException e) {
      LOGGER.error("Exception binding queue", e);
    }
  }

  @Override
  public void unbind(@Nonnull final Exchange exchange, @Nonnull final String routingKey) {
    try {
      LOGGER.info("Unbinding queue '{}' from exchange '{}' with routing key '{}'", this, exchange, routingKey);
      this.bunny.channel().queueUnbind(this.name, exchange.name(), routingKey, null);
    } catch(final IOException e) {
      LOGGER.error("Exception unbinding queue", e);
    }
  }

  @Nonnull
  @Override
  public <M extends Message> Subscription subscribe(@Nonnull final TypeToken<M> type, @Nonnull final MessageConsumer<M> consumer) {
    final SubscriptionImpl<M> subscription = new SubscriptionImpl<>(consumer);
    this.consumers.put(type, subscription);
    return subscription;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .addValue(this.name)
      .toString();
  }

  private final class ConsumerImpl implements Consumer {

    @Override
    public void handleConsumeOk(final String consumerTag) {
    }

    @Override
    public void handleCancelOk(final String consumerTag) {
    }

    @Override
    public void handleCancel(final String consumerTag) throws IOException {
    }

    @Override
    public void handleShutdownSignal(final String consumerTag, final ShutdownSignalException sig) {
    }

    @Override
    public void handleRecoverOk(final String consumerTag) {
    }

    @Override
    public void handleDelivery(final String consumerTag, final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) throws IOException {
      QueueImpl.this.bunny.channel().basicAck(envelope.getDeliveryTag(), false);

      try {
        this.delivery(properties, body);
      } catch(final Throwable t) {
        LOGGER.error("Exception delivering message: {}", t, describe(properties));
      }
    }

    private void delivery(final AMQP.BasicProperties properties, final byte[] body) throws IOException {
      final TypeToken<? extends Message> type = QueueImpl.this.mr.type(properties.getType());
      if(type == null) {
        return;
      }

      final Collection<SubscriptionImpl<? extends Message>> subscriptions = QueueImpl.this.consumers.get(type);
      if(subscriptions.isEmpty()) {
        return;
      }

      final String json = new String(body, StandardCharsets.UTF_8);
      final Message message = QueueImpl.this.mapper.readerFor(type.getRawType()).readValue(json);
      subscriptions.forEach(subscription -> subscription.consumer.accept(message, subscription, properties));
    }
  }

  private static String describe(final AMQP.BasicProperties properties) {
    final StringBuilder sb = new StringBuilder();
    properties.appendPropertyDebugStringTo(sb);
    return sb.toString();
  }

  private class SubscriptionImpl<M extends Message> implements Subscription {

    // raw
    final MessageConsumer consumer;

    private SubscriptionImpl(final MessageConsumer<M> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void cancel() {
      QueueImpl.this.consumers.values().remove(this);
    }
  }
}
