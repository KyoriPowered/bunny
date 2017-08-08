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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.kyori.membrane.facet.Connectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkState;

/**
 * The entry point to Bunny.
 */
@Singleton
public class Bunny implements Connectable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Bunny.class);
  private final BunnyConfiguration config;
  private Connection connection;
  @Nullable private Channel channel;

  @Inject
  private Bunny(final BunnyConfiguration config) {
    this.config = config;
  }

  /**
   * Gets the channel.
   *
   * @return the channel
   */
  @Nonnull
  public Channel channel() {
    checkState(this.channel != null, "bunny has not been connected");
    return this.channel;
  }

  @Override
  public void connect() throws IOException, TimeoutException {
    final ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername(this.config.username());
    factory.setPassword(this.config.password());
    factory.setVirtualHost(this.config.virtualHost());
    factory.setAutomaticRecoveryEnabled(this.config.automaticRecovery());
    factory.setNetworkRecoveryInterval(this.config.automaticRecoveryInterval());
    factory.setTopologyRecoveryEnabled(this.config.topologyRecovery());
    LOGGER.debug("Connecting to '{}'", this.config.addresses());
    this.connection = factory.newConnection(this.config.addresses());
    this.channel = this.connection.createChannel();
  }

  @Override
  public void disconnect() throws IOException, TimeoutException {
    if(this.channel != null) {
      LOGGER.debug("Disconnecting from {}", this.connection.toString());
      this.channel.close();
      this.connection.close();
    }
  }
}
