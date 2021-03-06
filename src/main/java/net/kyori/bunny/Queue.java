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

import com.google.common.reflect.TypeToken;
import net.kyori.bunny.message.Consume;
import net.kyori.bunny.message.Message;
import net.kyori.bunny.message.MessageConsumer;
import net.kyori.bunny.message.TargetedMessageConsumer;
import net.kyori.lunar.Named;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

/**
 * Represents an AMQP queue.
 *
 * <p>Extend {@link Impl} instead of implementing this interface.</p>
 */
public interface Queue extends Named {
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
  @Nullable Map<String, Object> arguments();

  /**
   * Binds this queue to the exchange.
   *
   * @param exchange the exchange
   * @param routingKey the routing key
   */
  void bind(final @NonNull Exchange exchange, final @NonNull String routingKey);

  /**
   * Unbinds this queue from the exchange.
   *
   * @param exchange the exchange
   * @param routingKey the routing key
   */
  void unbind(final @NonNull Exchange exchange, final @NonNull String routingKey);

  /**
   * Creates a subscription.
   *
   * @param type the message class
   * @param consumer the message consumer
   * @param <M> the message type
   * @return a representation of the subscription
   */
  default <M extends Message> @NonNull Subscription subscribe(final @NonNull Class<M> type, final @NonNull TargetedMessageConsumer<M> consumer) {
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
  <M extends Message> @NonNull Subscription subscribe(final @NonNull TypeToken<M> type, final @NonNull TargetedMessageConsumer<M> consumer);

  /**
   * Creates subscriptions for all {@link Consume consumers} found in {@code consumer}.
   *
   * @param consumer the consumer
   */
  void subscribe(final @NonNull MessageConsumer consumer);

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
    protected Impl(final @NonNull String name, final boolean durable, final boolean exclusive, final boolean autoDelete) {
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
    protected Impl(final @NonNull String name, final boolean durable, final boolean exclusive, final boolean autoDelete, final @Nullable Map<String, Object> arguments) {
      super(name, durable, exclusive, autoDelete, arguments);
    }
  }
}
