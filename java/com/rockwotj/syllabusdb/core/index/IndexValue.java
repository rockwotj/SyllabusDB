package com.rockwotj.syllabusdb.core.index;

import com.rockwotj.syllabusdb.core.document.Value;
import java.util.Objects;
import javax.annotation.Nonnull;

/** A single indexed value within an index entry. */
public record IndexValue(@Nonnull Value value, @Nonnull Direction direction) {

  public IndexValue {
    Objects.requireNonNull(value, "Missing required index value value");
    Objects.requireNonNull(direction, "Missing required index value direction");
  }

  public static IndexValue asc(Value value) {
    return new IndexValue(value, Direction.ASCENDING);
  }

  public static IndexValue desc(Value value) {
    return new IndexValue(value, Direction.DESCENDING);
  }
}
