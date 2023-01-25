package com.rockwotj.syllabusdb.core.document;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public record CollectionId(@Nonnull String raw) {
  private static final Pattern VALID_ID_REGEX = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

  public CollectionId {
    if (!VALID_ID_REGEX.matcher(raw).matches()) {
      throw new IllegalArgumentException("Invalid collection ID: " + raw);
    }
  }

  @Override
  public String toString() {
    return raw;
  }
}
