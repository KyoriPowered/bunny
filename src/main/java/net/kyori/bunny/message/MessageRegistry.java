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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import net.kyori.lunar.reflect.Hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A message registry maintains a mapping of message classes to message metadata.
 */
@Singleton
public class MessageRegistry {
  /**
   * A map of message ids to metadata.
   */
  private final Map<String, MessageMeta<?>> id = new HashMap<>();
  /**
   * A loading cache of message classes to metadata.
   */
  private final LoadingCache<Class<? extends Message>, MessageMeta<? extends Message>> type = Caffeine.newBuilder().build(this::find);

  @Inject
  private MessageRegistry(final Set<MessageMeta<? extends Message>> messages) {
    messages.forEach(meta -> {
      this.id.put(meta.name(), meta);
      this.type.put(meta.type(), meta);
    });
  }

  /**
   * Gets the type token for the specified message id.
   *
   * @param name the message id
   * @return the type token, or {@code null}
   */
  @Nullable
  public TypeToken<? extends Message> type(@Nonnull final String name) {
    @Nullable final MessageMeta<?> meta = this.id.get(name);
    if(meta != null) {
      return TypeToken.of(meta.type());
    }
    return null;
  }

  /**
   * Gets the id for the specified message.
   *
   * @param klass the message class
   * @return the message id
   */
  @Nonnull
  public String id(@Nonnull final Class<? extends Message> klass) {
    return checkNotNull(this.type.get(klass), "metadata for '%s'", klass.getName()).name();
  }

  private <M extends Message> MessageMeta<M> find(@Nonnull final Class<? extends Message> klass) {
    final Map<Class<? extends Message>, MessageMeta<?>> byType = this.type.asMap();
    final Class<? extends Message> match = Hierarchy.find(klass, Message.class, byType::containsKey);
    return (MessageMeta<M>) byType.get(match);
  }
}
