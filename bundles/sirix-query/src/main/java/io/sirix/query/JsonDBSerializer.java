/*
 * Copyright (c) 2022, SirixDB
 * <p>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.sirix.query;

import io.brackit.query.atomic.Atomic;
import io.brackit.query.jdm.Item;
import io.brackit.query.jdm.Iter;
import io.brackit.query.jdm.Sequence;
import io.brackit.query.jdm.Type;
import io.brackit.query.jdm.json.Array;
import io.brackit.query.jdm.json.Object;
import io.brackit.query.util.serialize.Serializer;
import io.brackit.query.util.serialize.StringSerializer;
import io.sirix.api.json.JsonNodeReadOnlyTrx;
import io.sirix.service.json.serialize.JsonSerializer;
import io.sirix.query.coverage.CoverageRegister;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * @author Johannes Lichtenberger <a href="mailto:lichtenberger.johannes@gmail.com">mail</a>
 */
public final class JsonDBSerializer implements Serializer, AutoCloseable {

  private final Appendable out;

  private final boolean prettyPrint;

  private boolean first;

  private final Set<JsonNodeReadOnlyTrx> trxSet;

  public JsonDBSerializer(final Appendable out, final boolean prettyPrint) {
    this.out = requireNonNull(out);
    this.prettyPrint = prettyPrint;
    first = true;
    trxSet = new HashSet<>();
  }

  @Override
  public void serialize(final Sequence sequence) {
    try {
      if (first) {

        CoverageRegister.register(0);

        first = false;
        out.append("{\"rest\":[");
      } else {

        CoverageRegister.register(1);

        out.append(",");
      }

      if (sequence != null) {

        CoverageRegister.register(2);

        Item item = null;
        Iter it;

        if (sequence instanceof Array || sequence instanceof Object) {

          CoverageRegister.register(3);

          item = (Item) sequence;
          it = null;
        } else {

          CoverageRegister.register(4);

          it = sequence.iterate();
        }

        try {
          if (item == null) {

            CoverageRegister.register(5);

            item = it.next();
          } else {
            CoverageRegister.register(6);
          }
          while (item != null) {

            CoverageRegister.register(7);

            if (item instanceof StructuredDBItem) {

              CoverageRegister.register(8);

              final var node = (StructuredDBItem<JsonNodeReadOnlyTrx>) item;
              trxSet.add(node.getTrx());

              var serializerBuilder =
                  new JsonSerializer.Builder(node.getTrx().getResourceSession(), out, node.getTrx().getRevisionNumber())
                      .serializeTimestamp(true)
                      .isXQueryResultSequence();
              if (prettyPrint) {

                CoverageRegister.register(9);

                serializerBuilder.prettyPrint().withInitialIndent();
              } else {
                CoverageRegister.register(10);
              }
              final JsonSerializer serializer = serializerBuilder.startNodeKey(node.getNodeKey()).build();
              serializer.call();

              item = printCommaIfNextItemExists(it);
            } else if (item instanceof Atomic) {

              CoverageRegister.register(11);

              if (((Atomic) item).type() == Type.STR) {

                CoverageRegister.register(12);

                out.append("\"");
              } else {

                CoverageRegister.register(13);
              
              }
              out.append(item.toString());
              if (((Atomic) item).type() == Type.STR) {

                CoverageRegister.register(14);

                out.append("\"");
              } else {
                CoverageRegister.register(15);
              }

              item = printCommaIfNextItemExists(it);
            } else if ((item instanceof Array) || (item instanceof Object)) {

              CoverageRegister.register(16);

              try (final var out = new ByteArrayOutputStream(); final var printWriter = new PrintWriter(out)) {
                new StringSerializer(printWriter).serialize(item);
                this.out.append(out.toString(StandardCharsets.UTF_8));
              }

              item = printCommaIfNextItemExists(it);
            } else {
              CoverageRegister.register(17);
            }
          }
        } finally {
          if (it != null) {
            CoverageRegister.register(18);
            it.close();
          } else {
            CoverageRegister.register(19);
          }
        }

      }
    } catch (final IOException e) {

      CoverageRegister.register(20);

      throw new UncheckedIOException(e);
    }
  }

  private Item printCommaIfNextItemExists(Iter it) throws IOException {
    Item item = null;
    if (it != null) {
      item = it.next();

      if (item != null) {
        out.append(",");
      }
    }
    return item;
  }

  @Override
  public void close() {
    try {
      out.append("]}");
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    trxSet.forEach(JsonNodeReadOnlyTrx::close);
  }
}