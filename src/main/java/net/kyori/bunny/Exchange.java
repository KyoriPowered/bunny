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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import net.kyori.bunny.message.Message;
import net.kyori.lunar.Named;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

/**
 * Represents an AMQP exchange.
 *
 * <p>Extend {@link Impl} instead of implementing this interface.</p>
 */
public interface Exchange extends Named {
  /**
   * Gets the type of this exchange.
   *
   * <p>This is generally one of the {@link BuiltinExchangeType builtin types}.</p>
   *
   * @return the type
   */
  @NonNull String type();

  /**
   * Tests if this exchange is durable (survive a server restart).
   *
   * @return {@code true} if this exchange is durable, {@code false} otherwise
   */
  boolean durable();

  /**
   * Tests if this exchange should auto-delete when no longer in use.
   *
   * @return {@code true} if this exchange should auto-delete when no longer in use, {@code false} otherwise
   */
  boolean autoDelete();

  /**
   * Tests if this exchange is internal (can't be directly published to by a client).
   *
   * @return {@code true} if this exchange is internal, {@code false} otherwise
   */
  boolean internal();

  /**
   * Gets an unmodifiable map of additional construction arguments.
   *
   * @return an unmodifiable map of additional construction arguments, or {@code null}
   */
  @Nullable Map<String, Object> arguments();

  /**
   * Publish a message to this exchange.
   *
   * @param message the message
   * @param routingKey the routing key
   * @param properties the properties
   */
  default void publish(final @NonNull Message message, final @NonNull String routingKey, final AMQP.@NonNull BasicProperties properties) {
    this.publish(message, routingKey, false, false, properties);
  }

  /**
   * Publish a message to this exchange.
   *
   * @param message the message
   * @param routingKey the routing key
   * @param mandatory if the {@code mandatory} flag should be set
   * @param immediate if the {@code immediate} flag should be set
   * @param properties the properties
   */
  void publish(final @NonNull Message message, final @NonNull String routingKey, final boolean mandatory, final boolean immediate, final AMQP.@NonNull BasicProperties properties);

  /**
   * Publish a response to a request.
   *
   * <p>This is a helper method that simply calls {@link #publish(Message, String, boolean, boolean, AMQP.BasicProperties)} with a
   * mapping (see below) of request properties to response properties</p>
   *
   * <table summary="Request properties to Response properties mapping">
   *   <tr>
   *     <th>Request</th>
   *     <th>Response</th>
   *   </tr>
   *   <tr>
   *     <td>{@link AMQP.BasicProperties#getReplyTo()}</td>
   *     <td>routing key</td>
   *   </tr>
   *   <tr>
   *     <td>{@link AMQP.BasicProperties#getMessageId()}</td>
   *     <td>{@link AMQP.BasicProperties#getCorrelationId()}</td>
   *   </tr>
   * </table>
   *
   * @param message the response message
   * @param request the request properties
   */
  void publishResponse(final @NonNull Message message, final AMQP.@NonNull BasicProperties request);

  /**
   * An abstract implementation of an exchange.
   */
  abstract class Impl extends ExchangeImpl {
    /**
     * Constructs a new exchange.
     *
     * @param name the exchange name
     * @param type the exchange type
     * @param durable if the exchange should be durable (survive a server restart)
     * @param autoDelete if this exchange should auto-delete when no longer in use
     * @param internal if this exchange is internal (can't be directly published to by a client)
     */
    protected Impl(final @NonNull String name, final @NonNull BuiltinExchangeType type, final boolean durable, final boolean autoDelete, final boolean internal) {
      super(name, type, durable, autoDelete, internal, null);
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
    protected Impl(final @NonNull String name, final @NonNull BuiltinExchangeType type, final boolean durable, final boolean autoDelete, final boolean internal, final @Nullable Map<String, Object> arguments) {
      super(name, type, durable, autoDelete, internal, arguments);
    }

    /**
     * Constructs a new exchange.
     *
     * @param name the exchange name
     * @param type the exchange type
     * @param durable if the exchange should be durable (survive a server restart)
     * @param autoDelete if this exchange should auto-delete when no longer in use
     * @param internal if this exchange is internal (can't be directly published to by a client)
     */
    protected Impl(final @NonNull String name, final @NonNull String type, final boolean durable, final boolean autoDelete, final boolean internal) {
      super(name, type, durable, autoDelete, internal, null);
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
    protected Impl(final @NonNull String name, final @NonNull String type, final boolean durable, final boolean autoDelete, final boolean internal, final @Nullable Map<String, Object> arguments) {
      super(name, type, durable, autoDelete, internal, arguments);
    }
  }
}
