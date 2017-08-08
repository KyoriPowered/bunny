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
package net.kyori.bunny.document.cereal;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.kyori.bunny.document.Document;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

/**
 * A document registry maintains a mapping of document classes to document metadata.
 */
@Singleton
final class DocumentRegistry {

  private final LoadingCache<Class<? extends Document>, DocumentMeta<? extends Document>> meta = Caffeine.newBuilder().build(type -> {
    final Map<String, DocumentMeta.Field<?>> getters = new HashMap<>();
    for(final Method method : type.getMethods()) {
      if(!Document.class.isAssignableFrom(method.getDeclaringClass())) {
        continue;
      }
      getters.put(method.getName(), new DocumentMeta.Field<>(method));
    }
    return new DocumentMeta<>(type, getters);
  });

  /**
   * Gets the document metadata for the specified document class.
   *
   * @param type the document class
   * @param <D> the document type
   * @return the document metadata
   */
  @Nonnull
  <D extends Document> DocumentMeta<D> meta(@Nonnull final Class<D> type) {
    return (DocumentMeta<D>) this.meta.get(type);
  }
}