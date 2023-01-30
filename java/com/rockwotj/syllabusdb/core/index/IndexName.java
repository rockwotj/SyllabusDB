package com.rockwotj.syllabusdb.core.index;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/** A name of an index. */
public record IndexName(@Nonnull String raw) {
  private static final Pattern VALID_NAME_REGEX = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

  public IndexName {
    if (raw == null || !VALID_NAME_REGEX.matcher(raw).matches()) {
      throw new IllegalArgumentException("Invalid field name: " + raw);
    }
  }

  @Override
  public String toString() {
    return raw;
  }
}
