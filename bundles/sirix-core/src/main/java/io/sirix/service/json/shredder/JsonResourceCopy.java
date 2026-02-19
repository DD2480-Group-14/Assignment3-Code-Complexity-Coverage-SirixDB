/*
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: * Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.sirix.service.json.shredder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.sirix.access.ResourceConfiguration;
import io.sirix.access.trx.node.json.InsertOperations;
import io.sirix.access.trx.node.json.objectvalue.ArrayValue;
import io.sirix.access.trx.node.json.objectvalue.BooleanValue;
import io.sirix.access.trx.node.json.objectvalue.NullValue;
import io.sirix.access.trx.node.json.objectvalue.NumberValue;
import io.sirix.access.trx.node.json.objectvalue.ObjectValue;
import io.sirix.access.trx.node.json.objectvalue.StringValue;
import io.sirix.api.json.JsonNodeReadOnlyTrx;
import io.sirix.api.json.JsonNodeTrx;
import io.sirix.api.json.JsonResourceSession;
import io.sirix.axis.DescendantAxis;
import io.sirix.axis.IncludeSelf;
import io.sirix.node.NodeKind;
import io.sirix.service.InsertPosition;
import io.sirix.service.ShredderCommit;
import io.sirix.settings.Constants;
import io.sirix.settings.Fixed;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;

/**
 * Copy a resource or a subtree into another
 * resoure. even copy all changes and revisions between a given revision/transaction.
 */
public final class JsonResourceCopy implements Callable<Void> {

  private final String INSERT = InsertOperations.INSERT.getName();
  private final String UPDATE = InsertOperations.UPDATE.getName();
  private final String DELETE = InsertOperations.DELETE.getName();
  private final String REPLACE = InsertOperations.REPLACE.getName();

  private final JsonResourceSession readResourceSession;

  /**
   * {@link JsonNodeTrx}.
   */
  private final JsonNodeTrx wtx;

  /**
   * Determines if changes are going to be commit right after shredding.
   */
  private final ShredderCommit commit;

  private final JsonNodeReadOnlyTrx rtx;

  private final long startNodeKey;

  /**
   * Insertion position.
   */
  private final InsertPosition insert;

  /**
   * Determines if diffs between revisions should be copied.
   */
  private final boolean copyAllRevisionsUpToMostRecent;

  /**
   * Builder to build an {@link JsonItemShredder} instance.
   */
  public static class Builder {

    /**
     * {@link JsonNodeTrx} implementation.
     */
    private final JsonNodeTrx wtx;

    /**
     * The transaction to read from.
     */
    private final JsonNodeReadOnlyTrx rtx;

    /**
     * Insertion position.
     */
    private final InsertPosition insert;

    /**
     * Determines if after shredding the transaction should be immediately committed.
     */
    private ShredderCommit commit = ShredderCommit.NOCOMMIT;

    private boolean copyAllRevisionsUpToMostRecent;

    /**
     * Constructor.
     *
     * @param wtx    the transaction to write to
     * @param rtx    the transaction to read from
     * @param insert insertion position
     * @throws NullPointerException if one of the arguments is {@code null}
     */
    public Builder(final JsonNodeTrx wtx, final JsonNodeReadOnlyTrx rtx, final InsertPosition insert) {
      this.wtx = requireNonNull(wtx);
      this.rtx = requireNonNull(rtx);
      this.insert = requireNonNull(insert);
    }

    /**
     * Commit afterwards.
     *
     * @return this builder instance
     */
    public JsonResourceCopy.Builder commitAfterwards() {
      commit = ShredderCommit.COMMIT;
      return this;
    }

    /**
     * Determines if changes between the revisions should be copied up to the most recent revision.
     *
     * @return this builder instance
     */
    public JsonResourceCopy.Builder copyAllRevisionsUpToMostRecent() {
      copyAllRevisionsUpToMostRecent = true;
      return this;
    }

    /**
     * Build an instance.
     *
     * @return {@link JsonItemShredder} instance
     */
    public JsonResourceCopy build() {
      return new JsonResourceCopy(wtx, rtx, this);
    }
  }

  /**
   * Stack for reading end element.
   */
  private final LongArrayList stack = new LongArrayList();

  /**
   * Private constructor.
   *
   * @param wtx     the transaction used to write
   * @param rtx     the transaction used to read
   * @param builder builder of the JSON resource copy
   */
  private JsonResourceCopy(final JsonNodeTrx wtx, final JsonNodeReadOnlyTrx rtx, final Builder builder) {
    this.wtx = wtx;
    this.rtx = rtx;
    this.readResourceSession = rtx.getResourceSession();
    this.insert = builder.insert;
    this.commit = builder.commit;
    this.startNodeKey = rtx.getNodeKey();
    this.copyAllRevisionsUpToMostRecent = builder.copyAllRevisionsUpToMostRecent;
  }

  public Void call() {
    rtx.moveTo(startNodeKey);

    // Setup primitives.
    boolean moveToParent = false;
    boolean first = true;

    long previousKey = Fixed.NULL_NODE_KEY.getStandardProperty();

    insert(moveToParent, first, previousKey);

    if (copyAllRevisionsUpToMostRecent) {
      wtx.commit();

      for (var revision = rtx.getRevisionNumber() + 1;
           revision <= rtx.getResourceSession().getMostRecentRevisionNumber(); revision++) {
        try (final var rtxOnRevision = readResourceSession.beginNodeReadOnlyTrx(revision)) {
          final var updateOperationsFile = readResourceSession.getResourceConfig()
                                                              .getResource()
                                                              .resolve(ResourceConfiguration.ResourcePaths.UPDATE_OPERATIONS.getPath())
                                                              .resolve(
                                                                  "diffFromRev" + (revision - 1) + "toRev" + revision
                                                                      + ".json");

          final JsonElement jsonElement;

          try {
            jsonElement = JsonParser.parseString(Files.readString(updateOperationsFile));
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }

          final var jsonObject = jsonElement.getAsJsonObject();
          final var diffsArray = jsonObject.getAsJsonArray("diffs");

          for (final var diffsElement : diffsArray) {
            final var diffsObject = diffsElement.getAsJsonObject();
            if (diffsObject.has(INSERT)) {
              final JsonObject insertObject = diffsObject.getAsJsonObject(INSERT);
              executeInsert(insertObject, rtxOnRevision);
            } else if (diffsObject.has(REPLACE)) {
              final JsonObject replaceObject = diffsObject.getAsJsonObject(REPLACE);
              executeReplace(replaceObject, rtxOnRevision);
            } else if (diffsObject.has(UPDATE)) {
              final JsonObject updateObject = diffsObject.getAsJsonObject(UPDATE);
              executeUpdate(updateObject, rtxOnRevision);
            } else if (diffsObject.has(DELETE)) {
              final JsonObject deleteObject = diffsObject.getAsJsonObject(DELETE);
              final long nodeKey = deleteObject.get("nodeKey").getAsLong();
              executeDelete(nodeKey);
            }
          }

          wtx.commit();
        }
      }
    } else {
      commit.commit(wtx);
    }

    return null;
  }

  private void executeDelete(final long nodeKey) {
    wtx.moveTo(nodeKey);
    wtx.remove();
  }

  private void executeUpdate(JsonObject updateObject, JsonNodeReadOnlyTrx rtxOnRevision) {
    final var key = updateObject.get("nodeKey").getAsLong();
    final var type = updateObject.get("type").getAsString();

    rtxOnRevision.moveTo(key);
    wtx.moveTo(key);

    switch (type) {
      case "boolean" -> wtx.setBooleanValue(rtxOnRevision.getBooleanValue());
      case "string" -> wtx.setStringValue(rtxOnRevision.getValue());
      case "number" -> wtx.setNumberValue(rtxOnRevision.getNumberValue());
    }
  }

  private void executeReplace(JsonObject replaceObject, JsonNodeReadOnlyTrx rtxOnRevision) {
    final var oldNodeKey = replaceObject.get("oldNodeKey").getAsLong();
    final var newNodeKey = replaceObject.get("newNodeKey").getAsLong();
    final var type = replaceObject.get("type").getAsString();
    wtx.moveTo(oldNodeKey);
    rtxOnRevision.moveTo(newNodeKey);

    final String insertPosition;
    if (wtx.hasRightSibling()) {
      insertPosition = "insertAsLeftSibling";
    } else if (wtx.hasLeftSibling()) {
      insertPosition = "insertAsRightSibling";
    } else {
      insertPosition = "insertAsFirstChild";
    }

    if (wtx.getParentKind() == NodeKind.OBJECT_KEY) {
      wtx.moveToParent();

      switch (rtxOnRevision.getKind()) {
        case OBJECT -> wtx.replaceObjectRecordValue(ObjectValue.INSTANCE);
        case ARRAY -> wtx.replaceObjectRecordValue(ArrayValue.INSTANCE);
        case OBJECT_NUMBER_VALUE, NUMBER_VALUE ->
            wtx.replaceObjectRecordValue(new NumberValue(rtxOnRevision.getNumberValue()));
        case OBJECT_NULL_VALUE, NULL_VALUE -> wtx.replaceObjectRecordValue(NullValue.INSTANCE);
        case OBJECT_STRING_VALUE, STRING_VALUE ->
            wtx.replaceObjectRecordValue(new StringValue(rtxOnRevision.getValue()));
        case OBJECT_BOOLEAN_VALUE, BOOLEAN_VALUE ->
            wtx.replaceObjectRecordValue(BooleanValue.of(rtxOnRevision.getBooleanValue()));
      }
    } else {
      wtx.remove();

      insert(type, rtxOnRevision, insertPosition);
    }
  }

  private void executeInsert(JsonObject insertObject, JsonNodeReadOnlyTrx rtxOnRevision) {
    final var key = insertObject.get("nodeKey").getAsLong();
    final var insertPosition = insertObject.get("insertPosition").getAsString();
    final var insertPositionNodeKey = insertObject.get("insertPositionNodeKey").getAsLong();
    final var type = insertObject.get("type").getAsString();
    wtx.moveTo(insertPositionNodeKey);
    rtxOnRevision.moveTo(key);

    insert(type, rtxOnRevision, insertPosition);
  }

  private void insert(String type, JsonNodeReadOnlyTrx rtxOnRevision, String insertPosition) {
    switch (type) {
      case "jsonFragment" -> insertFragment(rtxOnRevision, insertPosition);
      case "boolean" -> {
        if (insertPosition.equals("asFirstChild")) {
          wtx.insertBooleanValueAsFirstChild(rtxOnRevision.getBooleanValue());
        } else {
          wtx.insertBooleanValueAsRightSibling(rtxOnRevision.getBooleanValue());
        }
      }
      case "null" -> {
        if (insertPosition.equals("asFirstChild")) {
          wtx.insertNullValueAsFirstChild();
        } else {
          wtx.insertNullValueAsRightSibling();
        }
      }
      case "string" -> {
        if (insertPosition.equals("asFirstChild")) {
          wtx.insertStringValueAsFirstChild(rtxOnRevision.getValue());
        } else {
          wtx.insertStringValueAsRightSibling(rtxOnRevision.getValue());
        }
      }
    }
  }

  private void insertFragment(JsonNodeReadOnlyTrx rtxOnRevision, String insertPosition) {
    final var copyResource = new Builder(wtx, rtxOnRevision, InsertPosition.ofString(insertPosition)).build();
    copyResource.call();
  }

  private void insert(boolean moveToParent, boolean isFirst, long previousKey) {
    // Iterate over all nodes of the subtree including self.
    for (final var axis = new DescendantAxis(rtx, IncludeSelf.YES); axis.hasNext(); ) {
      final long key = axis.nextLong();

      // Process all pending moves to parents.
      if (moveToParent) {
        while (!stack.isEmpty() && stack.peekLong(0) != rtx.getLeftSiblingKey()) {
          rtx.moveTo(stack.popLong());
          rtx.moveTo(key);
          wtx.moveToParent();
        }
        if (!stack.isEmpty()) {
          rtx.moveTo(stack.popLong());
          wtx.moveToParent();
        }
        rtx.moveTo(key);
      }

      // Process node.
      final long nodeKey = rtx.getNodeKey();

      InsertPosition insertPosition;

      if (isFirst) {
        insertPosition = insert;
      } else {
        if (moveToParent) {
          insertPosition = InsertPosition.AS_RIGHT_SIBLING;
        } else if (rtx.hasLeftSibling() && previousKey == rtx.getLeftSiblingKey()) {
          insertPosition = InsertPosition.AS_RIGHT_SIBLING;
        } else {
          insertPosition = InsertPosition.AS_FIRST_CHILD;
        }
      }

      moveToParent = false;
      // Values of object keys have already been inserted.
      if (isFirst || rtx.getParentKind() != NodeKind.OBJECT_KEY) {
        processNode(rtx, insertPosition);
      }
      rtx.moveTo(nodeKey);

      isFirst = false;

      // Push end element to stack if we are a start element with children.
      boolean withChildren = false;
      if (!rtx.isDocumentRoot() && rtx.hasFirstChild()) {
        stack.push(rtx.getNodeKey());
        withChildren = true;
      }

      // Remember to process all pending moves to parents from stack if required.
      if (!withChildren && !rtx.isDocumentRoot() && !rtx.hasRightSibling()) {
        moveToParent = true;
      }

      previousKey = key;
    }

    // Finally emit all pending moves to parents.
    while (!stack.isEmpty() && stack.peekLong(0) != Constants.NULL_ID_LONG) {
      rtx.moveTo(stack.popLong());
    }
  }

  /**
   * Emit node.
   *
   * @param rtx Sirix {@link JsonNodeReadOnlyTrx}
   */
  public void processNode(final JsonNodeReadOnlyTrx rtx, final InsertPosition insertPosition) {
    switch (insertPosition) {
      case InsertPosition.AS_FIRST_CHILD:
        processNodeAsFirstChild(rtx);
        break;
      case InsertPosition.AS_RIGHT_SIBLING:
        processNodeAsRightSibling(rtx);
        break;
      case InsertPosition.AS_LEFT_SIBLING:
        processNodeAsLeftSibling(rtx);
      default:
        throw new IllegalStateException("Insert location not known!");
    }
  }

  private void processNodeAsFirstChild(final JsonNodeReadOnlyTrx rtx) {
    switch (rtx.getKind()) {
      case JSON_DOCUMENT:
        break;

      case OBJECT:
        wtx.insertObjectAsFirstChild();
        break;

      case ARRAY:
        wtx.insertArrayAsFirstChild();
        break;

      case OBJECT_KEY:
        final var key = rtx.getName().getLocalName();
        rtx.moveToFirstChild();
        switch (rtx.getKind()) {
          case OBJECT -> wtx.insertObjectRecordAsFirstChild(key, ObjectValue.INSTANCE);
          case ARRAY -> wtx.insertObjectRecordAsFirstChild(key, ArrayValue.INSTANCE);
          case OBJECT_BOOLEAN_VALUE ->
              wtx.insertObjectRecordAsFirstChild(key, BooleanValue.of(rtx.getBooleanValue()));
          case OBJECT_NULL_VALUE -> wtx.insertObjectRecordAsFirstChild(key, NullValue.INSTANCE);
          case OBJECT_STRING_VALUE -> wtx.insertObjectRecordAsFirstChild(key,
                                                                          new StringValue(rtx.getValue()));
          case OBJECT_NUMBER_VALUE -> wtx.insertObjectRecordAsFirstChild(key, new NumberValue(rtx.getNumberValue()));
        }
        break;

      case BOOLEAN_VALUE:
        wtx.insertBooleanValueAsFirstChild(rtx.getBooleanValue());
        break;

      case NULL_VALUE:
        wtx.insertNullValueAsFirstChild();
        break;

      case NUMBER_VALUE:
        wtx.insertNumberValueAsFirstChild(rtx.getNumberValue());
        break;

      case STRING_VALUE:
        wtx.insertStringValueAsFirstChild(rtx.getValue());
        break;

      default:
        throw new IllegalStateException("Node kind not known!");
    }
  }

  private void processNodeAsRightSibling(final JsonNodeReadOnlyTrx rtx) {
    switch (rtx.getKind()) {
      case JSON_DOCUMENT:
        break;

      case OBJECT:
        wtx.insertObjectAsRightSibling();
        break;

      case ARRAY:
        wtx.insertArrayAsRightSibling();
        break;

      case OBJECT_KEY:
        final var key = rtx.getName().getLocalName();
        rtx.moveToFirstChild();
        switch (rtx.getKind()) {
          case OBJECT -> wtx.insertObjectRecordAsRightSibling(key, ObjectValue.INSTANCE);
          case ARRAY -> wtx.insertObjectRecordAsRightSibling(key, ArrayValue.INSTANCE);
          case OBJECT_BOOLEAN_VALUE ->
              wtx.insertObjectRecordAsRightSibling(key, BooleanValue.of(rtx.getBooleanValue()));
          case OBJECT_NULL_VALUE -> wtx.insertObjectRecordAsRightSibling(key, NullValue.INSTANCE);
          case OBJECT_STRING_VALUE -> wtx.insertObjectRecordAsRightSibling(key,
                                                                          new StringValue(rtx.getValue()));
          case OBJECT_NUMBER_VALUE -> wtx.insertObjectRecordAsRightSibling(key, new NumberValue(rtx.getNumberValue()));
        }
        break;

      case BOOLEAN_VALUE:
        wtx.insertBooleanValueAsRightSibling(rtx.getBooleanValue());
        break;

      case NULL_VALUE:
        wtx.insertNullValueAsRightSibling();
        break;

      case NUMBER_VALUE:
        wtx.insertNumberValueAsRightSibling(rtx.getNumberValue());
        break;

      case STRING_VALUE:
        wtx.insertStringValueAsRightSibling(rtx.getValue());
        break;

      default:
        throw new IllegalStateException("Node kind not known!");
    }
  }

  private void processNodeAsLeftSibling(final JsonNodeReadOnlyTrx rtx) {
    switch (rtx.getKind()) {
      case JSON_DOCUMENT:
        break;

      case OBJECT:
        wtx.insertObjectAsLeftSibling();
        break;

      case ARRAY:
        wtx.insertArrayAsLeftSibling();
        break;

      case OBJECT_KEY:
        final var key = rtx.getName().getLocalName();
        rtx.moveToFirstChild();
        switch (rtx.getKind()) {
          case OBJECT -> wtx.insertObjectRecordAsLeftSibling(key, ObjectValue.INSTANCE);
          case ARRAY -> wtx.insertObjectRecordAsLeftSibling(key, ArrayValue.INSTANCE);
          case OBJECT_BOOLEAN_VALUE ->
              wtx.insertObjectRecordAsLeftSibling(key, BooleanValue.of(rtx.getBooleanValue()));
          case OBJECT_NULL_VALUE -> wtx.insertObjectRecordAsLeftSibling(key, NullValue.INSTANCE);
          case OBJECT_STRING_VALUE -> wtx.insertObjectRecordAsLeftSibling(key,
                                                                          new StringValue(rtx.getValue()));
          case OBJECT_NUMBER_VALUE -> wtx.insertObjectRecordAsLeftSibling(key, new NumberValue(rtx.getNumberValue()));
        }
        break;

      case BOOLEAN_VALUE:
        wtx.insertBooleanValueAsLeftSibling(rtx.getBooleanValue());
        break;

      case NULL_VALUE:
        wtx.insertNullValueAsLeftSibling();
        break;

      case NUMBER_VALUE:
        wtx.insertNumberValueAsLeftSibling(rtx.getNumberValue());
        break;

      case STRING_VALUE:
        wtx.insertStringValueAsLeftSibling(rtx.getValue());
        break;

      default:
        throw new IllegalStateException("Node kind not known!");
    }
  }
}



