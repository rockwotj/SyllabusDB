package com.rockwotj.syllabusdb.core.document;

import com.rockwotj.syllabusdb.core.util.compare.LexicographicalComparator;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

/** A path to a value within a document. */
public record FieldPath(@Nonnull List<FieldName> segments) implements Comparable<FieldPath> {

  public FieldPath(FieldName... segments) {
    this(Arrays.asList(segments));
  }

  public FieldPath {
    if (segments.isEmpty()) {
      throw new IllegalArgumentException("Invalid empty field path");
    }
    segments = List.copyOf(segments);
  }

  @Override
  public String toString() {
    return String.join(".", segments.stream().map(FieldName::raw).toList());
  }

  @Override
  public int compareTo(FieldPath other) {
    return LexicographicalComparator.<FieldName>naturalOrder()
        .compare(this.segments, other.segments);
  }
}
