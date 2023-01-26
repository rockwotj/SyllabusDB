package com.rockwotj.syllabusdb.core.index;

import com.google.common.primitives.UnsignedBytes;

/** A byte marker used to distinguish between types when the type isn't known in advance. */
enum ValueTag {
  NULL(0x1),
  FALSE(0x2),
  TRUE(0x3),
  NAN(0x4),
  DOUBLE(0x5),
  STRING(0x6),
  LIST(0x7),
  OBJECT(0x8);

  private final byte value;

  ValueTag(int i) {
    this.value = UnsignedBytes.checkedCast(i);
  }

  public static ValueTag fromValue(int b) {
    for (var tag : ValueTag.values()) {
      if (tag.value == b) return tag;
    }
    throw new InvalidEncodingException("Unknown tag value: " + b);
  }

  public byte value() {
    return value;
  }
}
