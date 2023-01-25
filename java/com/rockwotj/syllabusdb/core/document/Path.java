package com.rockwotj.syllabusdb.core.document;

public record Path(CollectionId collection, DocId id) {
  @Override
  public String toString() {
    return collection + "/" + id;
  }
}
