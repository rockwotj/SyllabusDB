package com.rockwotj.syllabusdb.core.index;

import com.rockwotj.syllabusdb.core.document.CollectionId;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/** A specification for an index. */
public record Index(
    @Nonnull IndexName name, @Nonnull CollectionId collection, @Nonnull List<IndexField> fields) {
  public Index {
    Objects.requireNonNull(name, "Missing required index name");
    Objects.requireNonNull(collection, "Missing required index collection");
    Objects.requireNonNull(fields, "Missing required index fields");
    if (fields.isEmpty()) {
      throw new IllegalArgumentException("Missing required index fields");
    }
    fields = List.copyOf(fields);
  }
}
