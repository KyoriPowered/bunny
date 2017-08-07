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

import com.google.common.base.MoreObjects;
import net.kyori.lunar.Nameable;
import net.kyori.membrane.facet.Connectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an AMQP queue.
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
   * An abstract implementation of a queue.
   */
  abstract class Impl implements Connectable, Queue {

    private static final Logger LOGGER = LoggerFactory.getLogger(Queue.class);
    @Nonnull private final Bunny bunny;
    @Nonnull private final String name;
    private final boolean durable;
    private final boolean exclusive;
    private final boolean autoDelete;
    @Nullable private final Map<String, Object> arguments;

    /**
     * Constructs a new queue.
     *
     * @param bunny bunny
     * @param name the queue name
     * @param durable if the queue should be durable (survive a server restart)
     * @param exclusive if this queue is exclusive (restricted to this connection)
     * @param autoDelete if this queue should auto-delete when no longer in use
     */
    protected Impl(@Nonnull final Bunny bunny, @Nonnull final String name, final boolean durable, final boolean exclusive, final boolean autoDelete) {
      this(bunny, name, durable, exclusive, autoDelete, null);
    }

    /**
     * Constructs a new queue.
     *
     * @param bunny bunny
     * @param name the queue name
     * @param durable if the queue should be durable (survive a server restart)
     * @param exclusive if this queue is exclusive (restricted to this connection)
     * @param autoDelete if this queue should auto-delete when no longer in use
     * @param arguments other construction arguments
     */
    protected Impl(@Nonnull final Bunny bunny, @Nonnull final String name, final boolean durable, final boolean exclusive, final boolean autoDelete, @Nullable final Map<String, Object> arguments) {
      this.bunny = bunny;
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
      LOGGER.debug("Declaring queue '{}'", this);
      this.bunny.channel().queueDeclare(this.name, this.durable, this.exclusive, this.autoDelete, this.arguments);
    }

    @Override
    public void disconnect() throws IOException, TimeoutException {
    }

    @Override
    public void bind(@Nonnull final Exchange exchange, @Nonnull final String routingKey) {
      try {
        LOGGER.debug("Binding queue '{}' to exchange '{}' with routing key '{}'", this, exchange, routingKey);
        this.bunny.channel().queueBind(this.name, exchange.name(), routingKey, null);
      } catch(final IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void unbind(@Nonnull Exchange exchange, @Nonnull String routingKey) {
      try {
        LOGGER.debug("Unbinding queue '{}' from exchange '{}' with routing key '{}'", this, exchange, routingKey);
        this.bunny.channel().queueUnbind(this.name, exchange.name(), routingKey, null);
      } catch(final IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("name", this.name)
        .add("durable", this.durable)
        .add("exclusive", this.exclusive)
        .add("autoDelete", this.autoDelete)
        .add("arguments", this.arguments)
        .toString();
    }
  }
}
