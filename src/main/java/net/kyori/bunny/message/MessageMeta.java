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
package net.kyori.bunny.message;

import net.kyori.blizzard.NonNull;
import net.kyori.blizzard.Nullable;

/**
 * Metadata describing a message.
 *
 * @param <M> the message type
 */
public final class MessageMeta<M extends Message> {
  /**
   * The message class.
   */
  @NonNull private final Class<M> type;
  /**
   * The message name.
   *
   * <p>This is either the value of {@link Message.Name}, or the {@link Class#getSimpleName() simple name}.</p>
   */
  @NonNull private final String name;

  MessageMeta(@NonNull final Class<M> type) {
    this.type = type;

    @Nullable final Message.Name name = type.getAnnotation(Message.Name.class);
    this.name = name != null ? name.value() : type.getSimpleName();
  }

  /**
   * Gets the message class.
   *
   * @return the message class
   */
  @NonNull
  public Class<M> type() {
    return this.type;
  }

  /**
   * Gets the message name.
   *
   * @return the message name
   */
  @NonNull
  public String name() {
    return this.name;
  }
}
