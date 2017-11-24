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
import com.google.common.reflect.Reflection;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.kyori.bunny.document.Document;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A serializer used for all {@link Document}s.
 */
@Singleton
public final class DocumentSerializer implements JsonDeserializer<Document>, JsonSerializer<Document> {
  private final DocumentRegistry registry;

  @Inject
  private DocumentSerializer(final DocumentRegistry registry) {
    this.registry = registry;
  }

  @Override
  public Document deserialize(final JsonElement element, final Type typeT, final JsonDeserializationContext context) throws JsonParseException {
    final JsonObject object = (JsonObject) element;
    final Map<String, Object> fields = new HashMap<>();
    final DocumentMeta<? extends Document> meta = this.registry.meta(TypeToken.of(typeT).getRawType().asSubclass(Document.class));
    for(final Map.Entry<String, DocumentMeta.Field<?>> entry : meta.fields.entrySet()) {
      final String name = entry.getKey();
      final TypeToken<?> type = TypeToken.of(entry.getValue().type());
      final Object value = context.deserialize(object.get(name), type.getType());
      fields.put(name, entry.getValue() instanceof DocumentMeta.OptionalField ? Optional.ofNullable(value) : value);
    }
    return this.createDocument(meta, fields);
  }

  @Override
  public JsonElement serialize(final Document document, final Type type, final JsonSerializationContext context) {
    final JsonObject object = new JsonObject();

    final DocumentMeta<? extends Document> meta = this.registry.meta(document.getClass());
    for(final Map.Entry<String, DocumentMeta.Field<?>> entry : meta.fields.entrySet()) {
      object.add(entry.getKey(), context.serialize(entry.getValue().get(document), entry.getValue().type()));
    }

    return object;
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
}
