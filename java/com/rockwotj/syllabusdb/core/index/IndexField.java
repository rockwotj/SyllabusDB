package com.rockwotj.syllabusdb.core.index;

import com.rockwotj.syllabusdb.core.document.FieldPath;
import java.util.Objects;
import javax.annotation.Nonnull;

/** A specification for how a single field will be indexed within a document. */
public record IndexField(@Nonnull FieldPath path, @Nonnull Direction direction) {
  public IndexField {
    Objects.requireNonNull(path, "Missing required index field path");
    Objects.requireNonNull(direction, "Missing required index field direction");
  }
}
