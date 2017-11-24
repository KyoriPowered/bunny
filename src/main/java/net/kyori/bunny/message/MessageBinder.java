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

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import net.kyori.blizzard.NonNull;

/**
 * A message binder.
 */
public final class MessageBinder {
  /**
   * The message set.
   */
  private final Multibinder<MessageMeta<? extends Message>> messages;

  /**
   * Creates a new message binder.
   *
   * @param binder the binder
   * @return a new message binder
   */
  @NonNull
  public static MessageBinder create(@NonNull final Binder binder) {
    return new MessageBinder(binder);
  }

  protected MessageBinder(@NonNull final Binder binder) {
    this.messages = Multibinder.newSetBinder(binder, new TypeLiteral<MessageMeta<? extends Message>>() {});
  }

  /**
   * Adds a message to the set.
   *
   * @param message the message
   * @param <M> the message type
   * @return this message binder
   */
  @NonNull
  public <M extends Message> MessageBinder register(@NonNull final Class<M> message) {
    this.messages.addBinding().toInstance(new MessageMeta<>(message));
    return this;
  }
}
