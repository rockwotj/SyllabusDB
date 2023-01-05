package com.rockwotj.syllabusdb.core.bytes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** An immutable view of a byte array, with expected equality and comparison semantics. */
@Immutable
public class ByteArray implements Comparable<ByteArray> {
  @Nonnull private final byte[] bytes;

  private ByteArray(@Nonnull byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Wrap a given byte array into an immutable view.
   *
   * @param bytes should never be modified after being passed in.
   */
  public static ByteArray wrap(@Nonnull byte[] bytes) {
    return new ByteArray(bytes);
  }

  public static ByteArray copyUtf8(@Nonnull String str) {
    return new ByteArray(str.getBytes(StandardCharsets.UTF_8));
  }

  public int length() {
    return this.bytes.length;
  }

  public byte get(int i) {
    return this.bytes[i];
  }

  @Override
  public int compareTo(@Nonnull ByteArray o) {
    return Arrays.compare(bytes, o.bytes);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof ByteArray other) {
      return Arrays.equals(bytes, other.bytes);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }
}
