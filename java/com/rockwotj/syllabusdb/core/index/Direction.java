package com.rockwotj.syllabusdb.core.index;

/** A directional encoding for an index. */
public enum Direction {
  ASCENDING,
  DESCENDING;

  public boolean isAsc() {
    return this == ASCENDING;
  }
}
