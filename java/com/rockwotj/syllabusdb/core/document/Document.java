package com.rockwotj.syllabusdb.core.document;

import java.util.Map;
import javax.annotation.Nonnull;

public record Document(
    @Nonnull DocId id, @Nonnull CollectionId collection, @Nonnull Map<FieldName, Value> fields) {
  public Document {
    fields = Map.copyOf(fields);
  }

  public Document(@Nonnull Path path, @Nonnull Map<FieldName, Value> fields) {
    this(path.id(), path.collection(), fields);
  }

  public Value get(FieldPath path) {
    var segmentIt = path.segments().iterator();
    // Never empty
    var v = fields.get(segmentIt.next());
    while (v != null && segmentIt.hasNext()) {
      if (v.type() != Value.Type.Object) {
        return null;
      }
      v = v.asObject().get(segmentIt.next());
    }
    return v;
  }

  public Path path() {
    return new Path(collection, id);
  }
}
