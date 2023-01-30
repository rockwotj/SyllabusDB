package com.rockwotj.syllabusdb.core.index;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/** The entry of index values for a document. */
public record IndexEntry(@Nonnull IndexName name, @Nonnull List<IndexValue> values) {
  public IndexEntry {
    Objects.requireNonNull(name, "Missing required name for index entry's index");
    if (values.isEmpty()) {
      throw new IllegalArgumentException("Missing required index entry values");
    }
    values = List.copyOf(values);
  }
}
