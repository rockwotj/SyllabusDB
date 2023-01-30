package com.rockwotj.syllabusdb.core.document;

import java.util.Objects;
import javax.annotation.Nonnull;

public record Path(@Nonnull CollectionId collection, @Nonnull DocId id) {

  public Path {
    Objects.requireNonNull(collection, "Missing required path collection");
    Objects.requireNonNull(id, "Missing required path id");
  }

  @Override
  public String toString() {
    return collection + "/" + id;
  }
}
