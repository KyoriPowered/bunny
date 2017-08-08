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

import com.google.common.reflect.TypeToken;
import net.kyori.bunny.message.Message;
import net.kyori.bunny.message.MessageConsumer;
import net.kyori.lunar.Nameable;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an AMQP queue.
 *
 * <p>Extend {@link Impl} instead of implementing this interface.</p>
 */
public interface Queue extends Nameable {

  /**
   * Tests if this queue is durable (survive a server restart).
   *
   * @return {@code true} if this queue is durable, {@code false} otherwise
   */
  boolean durable();

  /**
   * Tests if this queue is exclusive (restricted to this connection).
   *
   * @return {@code true} if this queue is exclusive, {@code false} otherwise
   */
  boolean exclusive();

  /**
   * Tests if this queue should auto-delete when no longer in use.
   *
   * @return {@code true} if this queue should auto-delete when no longer in use, {@code false} otherwise
   */
  boolean autoDelete();

  /**
   * Gets an unmodifiable map of additional construction arguments.
   *
   * @return an unmodifiable map of additional construction arguments, or {@code null}
   */
  @Nullable
  Map<String, Object> arguments();

  /**
   * Binds this queue to the exchange.
   *
   * @param exchange the exchange
   * @param routingKey the routing key
   */
  void bind(@Nonnull final Exchange exchange, @Nonnull final String routingKey);

  /**
   * Unbinds this queue from the exchange.
   *
   * @param exchange the exchange
   * @param routingKey the routing key
   */
  void unbind(@Nonnull final Exchange exchange, @Nonnull final String routingKey);

  /**
   * Creates a subscription.
   *
   * @param type the message class
   * @param consumer the message consumer
   * @param <M> the message type
   * @return a representation of the subscription
   */
  @Nonnull
  default <M extends Message> Subscription subscribe(@Nonnull final Class<M> type, @Nonnull final MessageConsumer<M> consumer) {
    return this.subscribe(TypeToken.of(type), consumer);
  }

  /**
   * Creates a subscription.
   *
   * @param type the message class
   * @param consumer the message consumer
   * @param <M> the message type
   * @return a representation of the subscription
   */
  @Nonnull
  <M extends Message> Subscription subscribe(@Nonnull final TypeToken<M> type, @Nonnull final MessageConsumer<M> consumer);

  /**
   * An abstract implementation of a queue.
   */
  abstract class Impl extends QueueImpl {

    /**
     * Constructs a new queue.
     *
     * @param name the queue name
     * @param durable if the queue should be durable (survive a server restart)
     * @param exclusive if this queue is exclusive (restricted to this connection)
     * @param autoDelete if this queue should auto-delete when no longer in use
     */
    protected Impl(@Nonnull final String name, final boolean durable, final boolean exclusive, final boolean autoDelete) {
      super(name, durable, exclusive, autoDelete, null);
    }

    /**
     * Constructs a new queue.
     *
     * @param name the queue name
     * @param durable if the queue should be durable (survive a server restart)
     * @param exclusive if this queue is exclusive (restricted to this connection)
     * @param autoDelete if this queue should auto-delete when no longer in use
     * @param arguments other construction arguments
     */
    protected Impl(@Nonnull final String name, final boolean durable, final boolean exclusive, final boolean autoDelete, @Nullable final Map<String, Object> arguments) {
      super(name, durable, exclusive, autoDelete, arguments);
    }
  }
}
