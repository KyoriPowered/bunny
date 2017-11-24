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

import com.rabbitmq.client.Address;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Represents the configuration for a {@link Bunny}.
 */
public interface BunnyConfiguration {
  /**
   * Gets a list of the known broker addresses.
   *
   * @return a list of the known broker addresses
   */
  @Nonnull
  List<Address> addresses();

  /**
   * Gets the username to use when connecting to the broker.
   *
   * @return the username to use when connecting to the broker
   */
  @Nonnull
  String username();

  /**
   * Gets the password to use when connecting to the broker.
   *
   * @return the password to use when connecting to the broker
   */
  @Nonnull
  String password();

  /**
   * Gets the virtual host to use when connecting to the broker.
   *
   * @return the virtual host to use when connecting to the broker
   */
  @Nonnull
  String virtualHost();

  /**
   * Tests if automatic connection recovery should be used.
   *
   * @return {@code true} if automatic connection recovery should be used, {@code false} otherwise
   */
  boolean automaticRecovery();

  /**
   * Gets the automatic connection recovery interval, in milliseconds.
   *
   * @return the automatic connection recovery interval in milliseconds
   */
  long automaticRecoveryInterval();

  /**
   * Tests if automatic connection recovery should be used.
   *
   * @return {@code true} if automatic connection recovery should be used, {@code false} otherwise
   */
  boolean topologyRecovery();
}
