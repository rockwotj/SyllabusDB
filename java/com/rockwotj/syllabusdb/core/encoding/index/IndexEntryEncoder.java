package com.rockwotj.syllabusdb.core.encoding.index;

import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import com.rockwotj.syllabusdb.core.encoding.value.ValueEncoder;
import com.rockwotj.syllabusdb.core.index.IndexEntry;
import com.rockwotj.syllabusdb.core.index.IndexValue;

/** Encode an IndexEntry into bytes for insertion into a KV Store for the proper sorted order. */
public class IndexEntryEncoder {
  private final ValueEncoder encoder = new ValueEncoder();

  public ByteArray encode(IndexEntry entry) {
    encoder.reset();
    encoder.asc().writeString(entry.name().raw());
    for (IndexValue value : entry.values()) {
      var directional = value.direction().isAsc() ? encoder.asc() : encoder.desc();
      directional.writeValue(value.value());
    }
    return encoder.toByteArray();
  }
}
