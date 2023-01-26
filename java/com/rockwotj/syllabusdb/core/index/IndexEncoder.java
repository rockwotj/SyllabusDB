package com.rockwotj.syllabusdb.core.index;

import com.google.common.primitives.UnsignedBytes;
import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import com.rockwotj.syllabusdb.core.document.Value;
import java.nio.charset.StandardCharsets;

/**
 * A class to create ordered keys of values for our KV Store. The encoding here does not prescribe a
 * schema - this must be enforced by the caller. Ascending and descending values can be freely
 * mixed.
 */
public final class IndexEncoder {

  private final ByteArray.Output output = ByteArray.newOutput();
  private final Asc asc = new Asc();
  private final Desc desc = new Desc();

  public Asc asc() {
    return asc;
  }

  public Desc desc() {
    return desc;
  }

  public void reset() {
    output.reset();
  }

  public ByteArray toByteArray() {
    return output.toByteArray();
  }

  /**
   * Write byte `b` in ascending order such that it respects the various escapes for 0x00 and 0xFF.
   */
  private void writeAscByte(byte b) {
    if (b == 0x0) {
      output.write(Constants.ESCAPED_NULL);
    } else if (b == UnsignedBytes.MAX_VALUE) {
      output.write(Constants.ESCAPED_FF);
    } else {
      output.write(b);
    }
  }

  /**
   * An abstract directional index encoder. It can write values such that they are sorted in either
   * ascendin or descending order depending on the implementation.
   */
  public abstract static class Directional {
    private Directional() {}

    /** Write `v` in the corresponding direction. */
    public void writeValue(Value v) {
      switch (v.type()) {
        case Null -> writeTag(ValueTag.NULL);
        case Boolean -> {
          if (v.asBoolean()) {
            writeTag(ValueTag.TRUE);
          } else {
            writeTag(ValueTag.FALSE);
          }
        }
        case Number -> {
          if (v.isNaN()) {
            writeTag(ValueTag.NAN);
          } else {
            writeTag(ValueTag.DOUBLE);
            writeDouble(v.asDouble());
          }
        }
        case String -> {
          writeTag(ValueTag.STRING);
          writeString(v.asString());
        }
        case List -> {
          writeTag(ValueTag.LIST);
          for (var e : v.asList()) {
            writeValue(e);
          }
          writeSeparator();
        }
        case Object -> {
          writeTag(ValueTag.OBJECT);
          for (var e : v.asObject().entrySet()) {
            writeString(e.getKey().raw());
            writeValue(e.getValue());
          }
          writeSeparator();
        }
      }
    }

    /** Write `s` in corresponding direction in codepoint order. */
    public void writeString(String s) {
      // UTF-8 preserves the ordering of individual codepoints.
      // Some interesting and recommended reading on UTF-8:
      // https://www.cl.cam.ac.uk/~mgk25/ucs/utf-8-history.txt
      var bytes = s.getBytes(StandardCharsets.UTF_8);
      for (byte b : bytes) {
        writeByte(b);
      }
      writeSeparator();
    }

    private void writeDouble(double d) {
      // This is the encoding of IEEE 754 double precision floating point numbers. The encoding for
      // doubles is the same,
      // but with more bits. To encode doubles into a total order from smallest to largest (we
      // assume NANs are
      // handled elsewhere) we need to always invert the sign bit so that negative values come
      // first.
      //
      // After that the order of the bytes are in ascending order corresponding with larger values
      // (ignoring the sign
      // bit) going last. This is what we want for positive numbers, but for negative numbers we
      // need to invert the
      // bits so that the larger absolute value'd numbers come first as negative values further from
      // zero should sort
      // first.
      //
      // The following table gives some common numbers and their binary format, which hopefully
      // helps for understanding
      // of the encoding.
      // ┌─────────┬──────┬──────────┬─────────────────────────┐
      // │ Number  │ Sign │ Exponent │       Significand       │
      // ├─────────┼──────┼──────────┼─────────────────────────┤
      // │ 0       │    0 │ 00000000 │ 00000000000000000000000 │
      // │ -0      │    1 │ 00000000 │ 00000000000000000000000 │
      // │ -2      │    1 │ 10000000 │ 00000000000000000000000 │
      // │ 1/3     │    0 │ 01111101 │ 01010101010101010101011 │
      // │ Pi      │    0 │ 10000000 │ 10010010000111111011011 │
      // │ inf     │    0 │ 11111111 │ 00000000000000000000000 │
      // │ -inf    │    1 │ 11111111 │ 00000000000000000000000 │
      // │ NaN     │    0 │ 11111111 │ 10000000000000000000001 │
      // └─────────┴──────┴──────────┴─────────────────────────┘
      //
      // Now for some JVM specifics - we need to get the raw bits of the double, so convert it to a
      // long.
      var raw = Double.doubleToRawLongBits(d);
      // signum allows us to check the leading bit to see if it's negative.
      if (Long.signum(raw) == -1) {
        // This is a negative number, we need to invert the sign bit, exponent and significand,
        // which means invert all
        // the bits.
        raw = ~raw;
      } else {
        // A positive number - we only need to flip the sign bit, so XOR just the sign bit to invert
        // it.
        raw ^= 0x8000_0000_0000_0000L;
      }
      // Write in big endian form, but by using writeByte we ensure it's still written in the
      // correct {asc,desc} direction.
      for (int i = Long.SIZE - Byte.SIZE; i >= 0; i -= Byte.SIZE) {
        writeByte((byte) ((raw >> i) & 0xFF));
      }
    }

    private void writeTag(ValueTag tag) {
      writeByte(tag.value());
    }

    protected abstract void writeByte(byte b);

    protected abstract void writeSeparator();
  }

  /**
   * An encoder that writes values in ascending order. Separators are the MIN value so that
   * smaller/shorter values always come before.
   */
  public class Asc extends Directional {
    @Override
    protected void writeByte(byte b) {
      writeAscByte(b);
    }

    @Override
    protected void writeSeparator() {
      output.write(Constants.ENCODED_MIN_VALUE);
    }
  }

  /**
   * An encoder that writes values in descending order. Separators are the MAX value so that
   * smaller/shorter values always come after.
   */
  public class Desc extends Directional {
    @Override
    protected void writeByte(byte b) {
      // Invert the bytes to reverse the sort order.
      writeAscByte((byte) (~b & 0xFF));
    }

    @Override
    protected void writeSeparator() {
      output.write(Constants.ENCODED_MAX_VALUE);
    }
  }
}
