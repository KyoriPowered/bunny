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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.reflect.Reflection;
import com.google.common.reflect.TypeToken;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import net.kyori.bunny.document.Document;
import net.kyori.bunny.message.Message;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * A document deserializer is a special type of deserializer that exists for each {@link Message}, as Jackson does
 * not support type hierarchy deserialization.
 *
 * @param <D> the document type
 */
public final class DocumentDeserializer<D extends Document> extends JsonDeserializer<D> {

  private final DocumentRegistry registry;
  private final DocumentMeta<D> meta;

  @AssistedInject
  private DocumentDeserializer(final DocumentRegistry registry, @Assisted final Class<D> type) {
    this.registry = registry;
    this.meta = registry.meta(type);
  }

  @Override
  public D deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
    final JsonNode node = parser.getCodec().readTree(parser);
    return this.deserialize0(this.meta, node, parser);
  }

  private <O extends Document> O deserialize0(final DocumentMeta<O> meta, final JsonNode node, final JsonParser parser) throws IOException {
    final Map<String, Object> fields = new HashMap<>();
    for(final Map.Entry<String, DocumentMeta.Field<?>> entry : meta.fields.entrySet()) {
      final String name = entry.getKey();
      final Class<?> type = TypeToken.of(entry.getValue().type()).getRawType();
      final Object value;
      if(Document.class.isAssignableFrom(type)) {
        value = this.deserialize0(this.registry.meta(type.asSubclass(Document.class)), node.get(name), parser);
      } else {
        value = this.parser(node.get(name), parser.getCodec()).readValueAs(type);
      }
      fields.put(name, entry.getValue() instanceof DocumentMeta.OptionalField ? Optional.ofNullable(value) : value);
    }
    return this.createDocument(meta, fields);
  }

  // Traversing a node does not carry over the codec from the parent node
  private JsonParser parser(final JsonNode node, final ObjectCodec codec) {
    final JsonParser newParser = node.traverse();
    newParser.setCodec(codec);
    return newParser;
  }

  private <O extends Document> O createDocument(final DocumentMeta<O> meta, final Map<String, Object> fields) {
    final LoadingCache<Method, MethodHandle> handles = Caffeine.newBuilder().build(method -> {
      final String name = method.getName();
      if(meta.fields.containsKey(name) && fields.containsKey(name)) {
        return MethodHandles.constant(method.getReturnType(), fields.get(name));
      }
      if(method.getName().equals("toString") && method.getReturnType() == String.class) {
        return MethodHandles.constant(method.getReturnType(), meta.type.getSimpleName() + fields.toString());
      }
      return MethodHandles.constant(method.getReturnType(), null);
    });
    return Reflection.newProxy(meta.type, (proxy, method, args) -> handles.get(method).invokeWithArguments(args));
  }

  // Cannot use generics here
  public interface Factory {

    /**
     * Creates a document deserializer for the specified document type.
     *
     * @param type the document type
     * @return a document deserializer
     */
    @Nonnull
    DocumentDeserializer<? extends Document> create(final Class<? extends Document> type);
  }
}
