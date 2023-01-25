package com.rockwotj.syllabusdb.core.document;

import com.rockwotj.syllabusdb.core.util.compare.CodepointComparator;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public record DocId(@Nonnull String raw) implements Comparable<DocId> {
  private static final Pattern VALID_ID_REGEX = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

  public DocId {
    if (!VALID_ID_REGEX.matcher(raw).matches()) {
      throw new IllegalArgumentException("Invalid document ID: " + raw);
    }
  }

  @Override
  public String toString() {
    return raw;
  }

  @Override
  public int compareTo(DocId other) {
    return CodepointComparator.INSTANCE.compare(this.raw, other.raw);
  }
}
