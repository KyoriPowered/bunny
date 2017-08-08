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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.kyori.bunny.document.Document;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A serializer used for all {@link Document}s.
 */
@Singleton
public final class DocumentSerializer extends JsonSerializer<Document> {

  private final DocumentRegistry registry;

  @Inject
  private DocumentSerializer(final DocumentRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void serialize(final Document document, final JsonGenerator generator, final SerializerProvider serializers) throws IOException {
    generator.writeStartObject();

    final DocumentMeta<? extends Document> meta = this.registry.meta(document.getClass());
    for(final Map.Entry<String, DocumentMeta.Field<?>> entry : meta.fields.entrySet()) {
      generator.writeObjectField(entry.getKey(), entry.getValue().get(document));
    }

    generator.writeEndObject();
  }
}
