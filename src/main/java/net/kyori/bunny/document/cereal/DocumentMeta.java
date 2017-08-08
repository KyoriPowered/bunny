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

import net.kyori.bunny.document.Document;
import net.kyori.lunar.exception.Exceptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Metadata describing a document.
 *
 * @param <D> the document type
 */
final class DocumentMeta<D extends Document> {

  /**
   * The document class.
   */
  @Nonnull final Class<D> type;
  /**
   * A map of field names to field entries.
   */
  @Nonnull final Map<String, Field<?>> fields;

  DocumentMeta(@Nonnull final Class<D> type, @Nonnull final Map<String, Field<?>> fields) {
    this.type = type;
    this.fields = fields;
  }

  /*
   * While this class is called Field it actually represents a Method which is used to obtain type information
   * during deserialization, and the value of calling the method during serialization.
   */
  static final class Field<T> {

    private final Method method;

    Field(final Method method) {
      this.method = method;
      this.method.setAccessible(true);
    }

    @Nonnull
    Type type() {
      return this.method.getGenericReturnType();
    }

    @Nullable
    T get(final Object object) {
      try {
        return (T) this.method.invoke(object);
      } catch(final IllegalAccessException | InvocationTargetException e) {
        throw Exceptions.rethrow(e);
      }
    }
  }
}
