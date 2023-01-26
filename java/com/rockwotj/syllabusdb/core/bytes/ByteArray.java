package com.rockwotj.syllabusdb.core.bytes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable view of a byte array, with proper hashing & equality. Provides unsigned comparison
 * semantics.
 */
@Immutable
public final class ByteArray implements Comparable<ByteArray> {
  @Nonnull private final byte[] bytes;

  private ByteArray(@Nonnull byte[] bytes) {
    this.bytes = bytes;
  }

  public static Output newOutput() {
    return new Output();
  }

  public static Output newOutput(int initialCapacity) {
    return new Output(initialCapacity);
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

  public String toUtf8() {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public InputStream toInputStream() {
    return new ByteArrayInputStream(bytes);
  }

  @Override
  public int compareTo(@Nonnull ByteArray o) {
    return Arrays.compareUnsigned(bytes, o.bytes);
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

  @Override
  public String toString() {
    return "0x" + HexFormat.ofDelimiter("_").withUpperCase().formatHex(bytes);
  }

  public String toHexString() {
    return toString();
  }

  public String toBinaryString() {
    var sb = new StringBuilder();
    sb.append("0b");
    for (byte b : bytes) {
      var formatted = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
      sb.append(formatted);
      sb.append('_');
    }
    if (!sb.isEmpty()) {
      sb.setLength(sb.length() - 1);
    }
    return sb.toString();
  }

  /** A wrapper around ByteArrayOutputStream for ByteArray instances directly. */
  public static final class Output extends OutputStream {
    @Nonnull private final ByteArrayOutputStream underlying;

    private Output() {
      underlying = new ByteArrayOutputStream();
    }

    private Output(int initialCapacity) {
      underlying = new ByteArrayOutputStream(initialCapacity);
    }

    @Override
    public void write(int b) {
      underlying.write(b);
    }

    @Override
    public void write(@Nonnull byte[] b) {
      underlying.write(b, 0, b.length);
    }

    @Override
    public void write(@Nonnull byte[] b, int off, int len) {
      underlying.write(b, off, len);
    }

    @Override
    public void flush() {
      try {
        underlying.flush();
      } catch (IOException e) {
        // Impossible
        throw new RuntimeException(e);
      }
    }

    @Override
    public void close() {
      try {
        underlying.close();
      } catch (IOException e) {
        // Impossible
        throw new RuntimeException(e);
      }
    }

    public void reset() {
      underlying.reset();
    }

    public ByteArray toByteArray() {
      return ByteArray.wrap(underlying.toByteArray());
    }
  }
}
