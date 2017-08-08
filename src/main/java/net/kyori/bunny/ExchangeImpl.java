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
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import net.kyori.bunny.message.Message;
import net.kyori.bunny.message.MessageRegistry;
import net.kyori.membrane.facet.Connectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * An abstract implementation of an exchange.
 */
abstract class ExchangeImpl implements Connectable, Exchange {

  private static final Logger LOGGER = LoggerFactory.getLogger(Exchange.class);
  @Inject private Bunny bunny;
  @Inject private ObjectMapper mapper;
  @Inject private MessageRegistry mr;
  @Nonnull private final String name;
  @Nonnull private final String type;
  private final boolean durable;
  private final boolean autoDelete;
  private final boolean internal;
  @Nullable private final Map<String, Object> arguments;

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
  ExchangeImpl(@Nonnull final String name, @Nonnull final BuiltinExchangeType type, final boolean durable, final boolean autoDelete, final boolean internal, @Nullable final Map<String, Object> arguments) {
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
  ExchangeImpl(@Nonnull final String name, @Nonnull final String type, final boolean durable, final boolean autoDelete, final boolean internal, @Nullable final Map<String, Object> arguments) {
    this.name = name;
    this.type = type;
    this.durable = durable;
    this.autoDelete = autoDelete;
    this.internal = internal;
    this.arguments = arguments;
  }

  @Nonnull
  @Override
  public String name() {
    return this.name;
  }

  @Nonnull
  @Override
  public String type() {
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

  @Nullable
  @Override
  public Map<String, Object> arguments() {
    return this.arguments != null ? Collections.unmodifiableMap(this.arguments) : null;
  }

  @Override
  public void connect() throws IOException, TimeoutException {
    LOGGER.info("Declaring exchange '{}'", this);
    this.bunny.channel().exchangeDeclare(this.name, this.type, this.durable, this.autoDelete, this.internal, this.arguments);
  }

  @Override
  public void disconnect() throws IOException, TimeoutException {
  }

  @Override
  public void publish(@Nonnull final Message message, final String routingKey, final boolean mandatory, final boolean immediate, AMQP.BasicProperties properties) {
    properties = properties.builder()
      .type(this.mr.id(message.getClass()))
      .build();
    try {
      final String json = this.mapper.writerFor(message.getClass()).writeValueAsString(message);
      this.bunny.channel().basicPublish(this.name, routingKey, mandatory, immediate, properties, json.getBytes(StandardCharsets.UTF_8));
    } catch(final IOException e) {
      e.printStackTrace();
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
