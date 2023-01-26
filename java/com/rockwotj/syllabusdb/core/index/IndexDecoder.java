package com.rockwotj.syllabusdb.core.index;

import static com.rockwotj.syllabusdb.core.index.Constants.MAX_UNSIGNED_BYTE;

import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import com.rockwotj.syllabusdb.core.document.FieldName;
import com.rockwotj.syllabusdb.core.document.Value;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/**
 * A decoder that can read values produced by `IndexEncoder`. There is no API provided to guess in
 * advance how the values where encoded. The schema must be known in advance and stored elsewhere.
 */
public final class IndexDecoder {
  private static final int EOF_MARKER = -1;
  private static final int MIN_VALUE_MARKER = Integer.MIN_VALUE;
  private static final int MAX_VALUE_MARKER = Integer.MAX_VALUE;
  private final InputStream stream;
  private final Asc asc = new Asc();
  private final Desc desc = new Desc();
  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

  public IndexDecoder(InputStream stream) {
    this.stream = stream;
  }

  public Asc asc() {
    return asc;
  }

  public Desc desc() {
    return desc;
  }

  private int readAscByte() throws IOException {
    var r = stream.read();
    return switch (r) {
      case EOF_MARKER -> EOF_MARKER;
      case 0x00 -> {
        var v = stream.read();
        yield switch (v) {
          case 0x01 -> MIN_VALUE_MARKER;
          case 0xFF -> 0x00;
          default -> throw new InvalidEncodingException("Invalid lower escape byte value: " + v);
        };
      }
      case 0xFF -> {
        var v = stream.read();
        yield switch (v) {
          case 0x00 -> Byte.toUnsignedInt(MAX_UNSIGNED_BYTE);
          case 0xFF -> MAX_VALUE_MARKER;
          default -> throw new InvalidEncodingException("Invalid upper escape byte value: " + v);
        };
      }
      default -> {
        UnsignedBytes.checkedCast(r);
        yield r;
      }
    };
  }

  public abstract class Directional {
    // For the recursive values such as lists and objects, we need to peek at the next byte to
    // determine if
    // we've reached the end or not. Writing the length at the beginning of values would ruin our
    // sort order
    // by sorting everything by length, then lexicographically. We're forced to lookahead instead
    // and then
    // "reuse" that value if needed to parse subvalues.
    private int peek;

    private Directional() {}

    @Nonnull
    public Value readValue() throws IOException {
      peek = readByte();
      return readValueInternal();
    }

    @Nonnull
    private Value readValueInternal() throws IOException {
      var tag = ValueTag.fromValue(peek);
      return switch (tag) {
        case NULL -> Value.NULL;
        case FALSE -> Value.FALSE;
        case TRUE -> Value.TRUE;
        case NAN -> Value.NAN;
        case DOUBLE -> Value.of(readDouble());
        case STRING -> Value.of(readString());
        case LIST -> {
          var list = new ArrayList<Value>();
          while ((peek = readByte()) != separatorMarker()) {
            list.add(readValueInternal());
          }
          yield Value.of(list);
        }
        case OBJECT -> {
          var object = new TreeMap<FieldName, Value>();
          while ((peek = readByte()) != separatorMarker()) {
            // Seed readString() with the value we just read
            buffer.write(peek);
            var name = new FieldName(readString());
            peek = readByte();
            var value = readValueInternal();
            object.put(name, value);
          }
          yield Value.of(object);
        }
      };
    }

    public String readString() throws IOException {
      int b;
      while ((b = readByte()) != separatorMarker()) {
        buffer.write(UnsignedBytes.checkedCast(b));
      }
      var str = buffer.toString(StandardCharsets.UTF_8);
      buffer.reset();
      return str;
    }

    private Double readDouble() throws IOException {
      // See IndexEncoder.Directional.writeDouble for a full explaination of the encoding format.
      // We apply the reverse operation here to get back to our original double.
      var raw =
          Longs.fromBytes(
              readByteChecked(),
              readByteChecked(),
              readByteChecked(),
              readByteChecked(),
              readByteChecked(),
              readByteChecked(),
              readByteChecked(),
              readByteChecked());
      // Invert the operations from encoding
      if (Long.signum(raw) == -1) {
        raw ^= 0x8000_0000_0000_0000L;
      } else {
        raw = ~raw;
      }
      return Double.longBitsToDouble(raw);
    }

    /** Assert we're reading a non-special byte (EOF, MIN, MAX). */
    private byte readByteChecked() throws IOException {
      return UnsignedBytes.checkedCast(readByte());
    }

    protected abstract int separatorMarker();

    protected abstract int readByte() throws IOException;
  }

  public class Asc extends Directional {
    @Override
    protected int separatorMarker() {
      return MIN_VALUE_MARKER;
    }

    @Override
    protected int readByte() throws IOException {
      return readAscByte();
    }
  }

  public class Desc extends Directional {
    @Override
    protected int separatorMarker() {
      return MAX_VALUE_MARKER;
    }

    @Override
    protected int readByte() throws IOException {
      var r = readAscByte();
      return switch (r) {
        case EOF_MARKER -> EOF_MARKER;
        case MAX_VALUE_MARKER -> MAX_VALUE_MARKER;
        case MIN_VALUE_MARKER -> MIN_VALUE_MARKER;
        default -> ~r & 0xFF;
      };
    }
  }
}
