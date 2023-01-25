package com.rockwotj.syllabusdb.core.document;

import com.rockwotj.syllabusdb.core.util.compare.CodepointComparator;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/** A name within document. */
public record FieldName(@Nonnull String raw) implements Comparable<FieldName> {
  private static final Pattern VALID_ID_REGEX = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

  public FieldName {
    if (!VALID_ID_REGEX.matcher(raw).matches()) {
      throw new IllegalArgumentException("Invalid field name: " + raw);
    }
  }

  @Override
  public String toString() {
    return raw;
  }

  @Override
  public int compareTo(FieldName other) {
    return CodepointComparator.INSTANCE.compare(this.raw, other.raw);
  }
}
