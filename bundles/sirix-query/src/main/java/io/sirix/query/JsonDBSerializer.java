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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;

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

        // Branch 0: First call
       CoverageTool.cover(0);

        first = false;
        out.append("{\"rest\":[");
      } else {

        // Branch 1
        CoverageTool.cover(1);

        out.append(",");
      }

      if (sequence != null) {

        // Branch 2: Sequence is non-null
       CoverageTool.cover(2);

        Item item = null;
        Iter it;

        if (sequence instanceof Array || sequence instanceof Object) {

          // Branch 3: Sequence is Array/Object
         CoverageTool.cover(3);

          item = (Item) sequence;
          it = null;
        } else {

          // Branch 4
         CoverageTool.cover(4);

          it = sequence.iterate();
        }

        try {
          if (item == null) {

            // Branch 5: Item is not available, fetch next.
            CoverageTool.cover(5);

            item = it.next();
          } else {

            // Branch 6
           CoverageTool.cover(6);
          }
          while (item != null) {

            // Branch 7: Loop over items in sequence
            CoverageTool.cover(7);

            if (item instanceof StructuredDBItem) {

              // Branch 8: Item is a database node
              CoverageTool.cover(8);

              final var node = (StructuredDBItem<JsonNodeReadOnlyTrx>) item;
              trxSet.add(node.getTrx());

              var serializerBuilder =
                  new JsonSerializer.Builder(node.getTrx().getResourceSession(), out, node.getTrx().getRevisionNumber())
                      .serializeTimestamp(true)
                      .isXQueryResultSequence();
              if (prettyPrint) {

                // Branch 9: Pretty printing enabled
                CoverageTool.cover(9);

                serializerBuilder.prettyPrint().withInitialIndent();
              } else {

                // Branch 10: Pretty printing disabled
                CoverageTool.cover(10);
              }
              final JsonSerializer serializer = serializerBuilder.startNodeKey(node.getNodeKey()).build();
              serializer.call();

              item = printCommaIfNextItemExists(it);
            } else if (item instanceof Atomic) {

              // Branch 11: Item is atomic value
              CoverageTool.cover(11);

              if (((Atomic) item).type() == Type.STR) {

                // Branch 12: Atomic value is a string
                CoverageTool.cover(12);

                out.append("\"");
              } else {

                // Branch 13: Atomic value is not string
                CoverageTool.cover(13);
              
              }
              out.append(item.toString());
              if (((Atomic) item).type() == Type.STR) {

                // Branch 14: Close quote for string atomic value
                CoverageTool.cover(14);

                out.append("\"");
              } else {

                // Branch 15: No closing quote needed
                CoverageTool.cover(15);
              }

              item = printCommaIfNextItemExists(it);
            } else if ((item instanceof Array) || (item instanceof Object)) {

              // Branch 16: Item is JSON Array/Object
              CoverageTool.cover(16);

              try (final var out = new ByteArrayOutputStream(); final var printWriter = new PrintWriter(out)) {
                new StringSerializer(printWriter).serialize(item);
                this.out.append(out.toString(StandardCharsets.UTF_8));
              }

              item = printCommaIfNextItemExists(it);
            } else {

              // Branch 17: Item type not supported
              CoverageTool.cover(17);
            }
          }
        } finally {
          if (it != null) {

            // Branch 18: Iterator was used and must be closed
            CoverageTool.cover(18);
            it.close();
          } else {

            // Branch 19: No iterator was used, don't need to close.
            CoverageTool.cover(19);
          }
        }

      }
    } catch (final IOException e) {

      // Branch 20: IOException occured
      CoverageTool.cover(20);

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

class CoverageTool {
    static ArrayList<String> branches = new ArrayList<>();
    static boolean initialized = false;

    /**
     * Initialize the branch array if not yet initialized
     */ 
    static void initializeBranches() {
        for (int i = 0; i < 21; ++i) {
            branches.addLast("ID: " + i + "  false\n");
        }
        initialized = true;
    }

    /**
     * Cover the branch with the given branch ID.
     * The entire branch array is written to the
     * file each time this function is called.
     */
    static void cover(int branchId) {
        if(!CoverageTool.initialized) {
            initializeBranches();
        }

        branches.set(branchId, "ID: " + branchId + "  true\n");
        try {
            StringBuilder sb = new StringBuilder();
            for(String branch : branches) {
                sb.append(branch);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter("modifyCoverage.txt"));
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
