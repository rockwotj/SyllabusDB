package com.rockwotj.syllabusdb.core.index;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import com.rockwotj.syllabusdb.core.document.Value;
import com.rockwotj.syllabusdb.core.document.testdata.Values;

import java.io.IOException;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IndexTest {

  @Test
  public void ascendingByteOrder() {
    var encoded = new ArrayList<EncodedValue>();
    for (var v : Values.TOTAL_ORDER) {
      encoded.add(EncodedValue.asc(v));
    }
    assertThat(encoded).isInStrictOrder();
  }

  @Test
  public void descendingByteOrder() {
    var encoded = new ArrayList<EncodedValue>();
    for (var v : Lists.reverse(Values.TOTAL_ORDER)) {
      encoded.add(EncodedValue.desc(v));
    }
    assertThat(encoded).isInStrictOrder();
  }

  @Test
  public void ascendingRoundTrip() throws IOException {
    for (var v : Values.TOTAL_ORDER) {
      var decoder = new IndexDecoder(EncodedValue.asc(v).encoded().toInputStream());
      assertThat(decoder.asc().readValue()).isEqualTo(v);
    }
  }

  @Test
  public void descendingRoundTrip() throws IOException {
    for (var v : Values.TOTAL_ORDER) {
      var decoder = new IndexDecoder(EncodedValue.desc(v).encoded().toInputStream());
      assertThat(decoder.desc().readValue()).isEqualTo(v);
    }
  }

  private record EncodedValue(Value value, ByteArray encoded) implements Comparable<EncodedValue> {
    public static EncodedValue asc(Value value) {
      var encoder = new IndexEncoder();
      encoder.asc().writeValue(value);
      return new EncodedValue(value, encoder.toByteArray());
    }

    public static EncodedValue desc(Value value) {
      var encoder = new IndexEncoder();
      encoder.desc().writeValue(value);
      return new EncodedValue(value, encoder.toByteArray());
    }

    @Override
    public int compareTo(EncodedValue other) {
      return encoded.compareTo(other.encoded);
    }

    @Override
    public String toString() {
      return "AscEncodedValue{" + "value=" + value + ", encoded=" + encoded.toBinaryString() + '}';
    }
  }
}
