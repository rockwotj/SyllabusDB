package com.rockwotj.syllabusdb.core.index;

import com.google.common.primitives.UnsignedBytes;
import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import com.rockwotj.syllabusdb.core.document.Value;
import java.nio.charset.StandardCharsets;

/** */
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

  private void writeAscByte(byte b) {
    if (b == 0x0) {
      output.write(Constants.ESCAPED_NULL);
    } else if (b == UnsignedBytes.MAX_VALUE) {
      output.write(Constants.ESCAPED_FF);
    } else {
      output.write(b);
    }
  }

  public abstract class Directional {

    private Directional() {}

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

    public void writeString(String s) {
      var bytes = s.getBytes(StandardCharsets.UTF_8);
      for (byte b : bytes) {
        writeByte(b);
      }
      writeSeparator();
    }

    private void writeDouble(double d) {
      //
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
      // {value=4.9E-324,
      // encoded=0b00000101_00000001_00000000_00000000_00000000_00000000_00000000_00000000_10000000}
      // {value=2.2250738585072014E-308,
      // encoded=0b00000101_00000000_00000000_00000000_00000000_00000000_00000000_00010000_10000000}
      var raw = Double.doubleToRawLongBits(d);
      if (Long.signum(raw) == -1) {
        // This is a negative number,
        // we need to invert the sign bit to make negatives
        // come first, then the rest of the bits to get a
        // ascending order instead of a descending one
        raw = ~raw;
      } else {
        // This is 0 or a positive number
        // we need to flip the sign bit and that's it
        // (so that it comes after negatives)
        raw ^= 0x8000_0000_0000_0000L;
      }
      // Write in big endian form
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

  public class Desc extends Directional {
    @Override
    protected void writeByte(byte b) {
      writeAscByte((byte) (~b & 0xFF));
    }

    @Override
    protected void writeSeparator() {
      output.write(Constants.ENCODED_MAX_VALUE);
    }
  }
}
