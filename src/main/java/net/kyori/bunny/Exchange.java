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
import com.rabbitmq.client.BuiltinExchangeType;
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
 * Represents an AMQP exchange.
 */
public interface Exchange extends Nameable {

  /**
   * Gets the type of this exchange.
   *
   * <p>This is generally one of the {@link BuiltinExchangeType builtin types}.</p>
   *
   * @return the type
   */
  @Nonnull
  String type();

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
  @Nullable
  Map<String, Object> arguments();

  /**
   * An abstract implementation of an exchange.
   */
  abstract class Impl implements Connectable, Exchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(Exchange.class);
    @Nonnull private final Bunny bunny;
    @Nonnull private final String name;
    @Nonnull private final String type;
    private final boolean durable;
    private final boolean autoDelete;
    private final boolean internal;
    @Nullable private final Map<String, Object> arguments;

    /**
     * Constructs a new exchange.
     *
     * @param bunny bunny
     * @param name the exchange name
     * @param type the exchange type
     * @param durable if the exchange should be durable (survive a server restart)
     * @param autoDelete if this exchange should auto-delete when no longer in use
     * @param internal if this exchange is internal (can't be directly published to by a client)
     */
    protected Impl(@Nonnull final Bunny bunny, @Nonnull final String name, @Nonnull final BuiltinExchangeType type, final boolean durable, final boolean autoDelete, final boolean internal) {
      this(bunny, name, type, durable, autoDelete, internal, null);
    }

    /**
     * Constructs a new exchange.
     *
     * @param bunny bunny
     * @param name the exchange name
     * @param type the exchange type
     * @param durable if the exchange should be durable (survive a server restart)
     * @param autoDelete if this exchange should auto-delete when no longer in use
     * @param internal if this exchange is internal (can't be directly published to by a client)
     * @param arguments other construction arguments
     */
    protected Impl(@Nonnull final Bunny bunny, @Nonnull final String name, @Nonnull final BuiltinExchangeType type, final boolean durable, final boolean autoDelete, final boolean internal, @Nullable final Map<String, Object> arguments) {
      this(bunny, name, type.getType(), durable, autoDelete, internal, arguments);
    }

    /**
     * Constructs a new exchange.
     *
     * @param bunny bunny
     * @param name the exchange name
     * @param type the exchange type
     * @param durable if the exchange should be durable (survive a server restart)
     * @param autoDelete if this exchange should auto-delete when no longer in use
     * @param internal if this exchange is internal (can't be directly published to by a client)
     */
    protected Impl(@Nonnull final Bunny bunny, @Nonnull final String name, @Nonnull final String type, final boolean durable, final boolean autoDelete, final boolean internal) {
      this(bunny, name, type, durable, autoDelete, internal, null);
    }

    /**
     * Constructs a new exchange.
     *
     * @param bunny bunny
     * @param name the exchange name
     * @param type the exchange type
     * @param durable if the exchange should be durable (survive a server restart)
     * @param autoDelete if this exchange should auto-delete when no longer in use
     * @param internal if this exchange is internal (can't be directly published to by a client)
     * @param arguments other construction arguments
     */
    protected Impl(@Nonnull final Bunny bunny, @Nonnull final String name, @Nonnull final String type, final boolean durable, final boolean autoDelete, final boolean internal, @Nullable final Map<String, Object> arguments) {
      this.bunny = bunny;
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
      LOGGER.debug("Declaring exchange '{}'", this);
      this.bunny.channel().exchangeDeclare(this.name, this.type, this.durable, this.autoDelete, this.internal, this.arguments);
    }

    @Override
    public void disconnect() throws IOException, TimeoutException {
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("name", this.name)
        .add("type", this.type)
        .add("durable", this.durable)
        .add("autoDelete", this.autoDelete)
        .add("internal", this.internal)
        .add("arguments", this.arguments)
        .toString();
    }
  }
}
