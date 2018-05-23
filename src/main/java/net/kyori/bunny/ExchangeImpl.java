/*
 * This file is part of bunny, licensed under the MIT License.
 *
 * Copyright (c) 2017-2018 KyoriPowered
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

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import net.kyori.bunny.message.Message;
import net.kyori.bunny.message.MessageRegistry;
import net.kyori.membrane.facet.Connectable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

/**
 * An abstract implementation of an exchange.
 */
abstract class ExchangeImpl implements Connectable, Exchange {
  private static final Logger LOGGER = LoggerFactory.getLogger(Exchange.class);
  private @Inject Bunny bunny;
  private @Inject Gson gson;
  private @Inject MessageRegistry mr;
  private final @NonNull String name;
  private final @NonNull String type;
  private final boolean durable;
  private final boolean autoDelete;
  private final boolean internal;
  private final @Nullable Map<String, Object> arguments;

  /**
   * Constructs a new exchange.
   *
   * @param name the exchange name
   * @param type the exchange type
   * @param durable if the exchange should be durable (survive a server restart)
   * @param autoDelete if this exchange should auto-delete when no longer in use
   * @param internal if this exchange is internal (can't be directly published to by a client)
   * @param arguments other construction arguments
   */
  ExchangeImpl(final @NonNull String name, final @NonNull BuiltinExchangeType type, final boolean durable, final boolean autoDelete, final boolean internal, final @Nullable Map<String, Object> arguments) {
    this(name, type.getType(), durable, autoDelete, internal, arguments);
  }

  /**
   * Constructs a new exchange.
   *
   * @param name the exchange name
   * @param type the exchange type
   * @param durable if the exchange should be durable (survive a server restart)
   * @param autoDelete if this exchange should auto-delete when no longer in use
   * @param internal if this exchange is internal (can't be directly published to by a client)
   * @param arguments other construction arguments
   */
  ExchangeImpl(final @NonNull String name, final @NonNull String type, final boolean durable, final boolean autoDelete, final boolean internal, final @Nullable Map<String, Object> arguments) {
    this.name = name;
    this.type = type;
    this.durable = durable;
    this.autoDelete = autoDelete;
    this.internal = internal;
    this.arguments = arguments;
  }

  @Override
  public @NonNull String name() {
    return this.name;
  }

  @Override
  public @NonNull String type() {
    return this.type;
  }

  @Override
  public boolean durable() {
    return this.durable;
  }

  @Override
  public boolean autoDelete() {
    return this.autoDelete;
  }

  @Override
  public boolean internal() {
    return this.internal;
  }

  @Override
  public @Nullable Map<String, Object> arguments() {
    return this.arguments != null ? Collections.unmodifiableMap(this.arguments) : null;
  }

  @Override
  public void connect() throws IOException {
    LOGGER.info("Declaring exchange '{}'", this);
    this.bunny.channel().exchangeDeclare(this.name, this.type, this.durable, this.autoDelete, this.internal, this.arguments);
  }

  @Override
  public void disconnect() {
  }

  @Override
  public void publish(final @NonNull Message message, final @NonNull String routingKey, final boolean mandatory, final boolean immediate, final AMQP.@NonNull BasicProperties properties) {
    this.publish(message, routingKey, mandatory, immediate, properties.builder());
  }

  @Override
  public void publishResponse(final @NonNull Message message, final AMQP.@NonNull BasicProperties request) {
    final AMQP.BasicProperties.Builder properties = new AMQP.BasicProperties.Builder()
      .correlationId(request.getMessageId());
    this.publish(message, request.getReplyTo(), false, false, properties);
  }

  public void publish(final @NonNull Message message, final String routingKey, final boolean mandatory, final boolean immediate, final AMQP.BasicProperties.Builder properties) {
    properties
      .messageId(UUID.randomUUID().toString())
      .type(this.mr.id(message.getClass()));

    try {
      final String json = this.gson.toJson(message);
      this.bunny.channel().basicPublish(this.name, routingKey, mandatory, immediate, properties.build(), json.getBytes(StandardCharsets.UTF_8));
    } catch(final IOException e) {
      LOGGER.error("Exception encountered while publishing message", e);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("name", this.name)
      .add("type", this.type)
      .toString();
  }
}
