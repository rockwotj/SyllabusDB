package com.rockwotj.syllabusdb.core.document.testdata;

import com.rockwotj.syllabusdb.core.document.FieldName;
import com.rockwotj.syllabusdb.core.document.Value;
import java.util.List;

public class Values {

  private Values() {}

  public static List<Value> TOTAL_ORDER =
      List.of(
          Value.NULL,
          Value.FALSE,
          Value.TRUE,
          Value.NAN,
          Value.of(Double.NEGATIVE_INFINITY),
          Value.of(-Double.MAX_VALUE),
          Value.of(-Math.PI),
          Value.of(-Double.MIN_NORMAL),
          Value.of(-Double.MIN_VALUE),
          Value.of(-0.0),
          Value.of(+0.0),
          Value.of(Double.MIN_VALUE),
          Value.of(Double.MIN_NORMAL),
          Value.of(Math.PI),
          Value.of(Double.MAX_VALUE),
          Value.of(Double.POSITIVE_INFINITY),
          Value.of(""),
          Value.of("\0"),
          Value.of("\0\0"),
          Value.of(codepoints(0x61)),
          Value.of(codepoints(0x20ac)),
          Value.of(codepoints(0xFF61)),
          Value.of(codepoints(0x10002)),
          Value.of(codepoints(0x23456)),
          Value.EMPTY_LIST,
          Value.ofList(Value.NULL),
          Value.ofList(Value.FALSE),
          Value.ofList(Value.FALSE, Value.NULL),
          Value.ofList(Value.FALSE, Value.TRUE),
          Value.ofList(Value.TRUE),
          Value.ofList(Value.TRUE, Value.NULL),
          Value.EMPTY_OBJECT,
          Value.of(new FieldName("a"), Value.NULL),
          Value.of(new FieldName("a"), Value.FALSE),
          Value.of(new FieldName("aa"), Value.NULL),
          Value.of(new FieldName("nest"), Value.ofList(Value.TRUE, Value.FALSE)));

  private static String codepoints(int... codepoints) {
    return new String(codepoints, 0, codepoints.length);
  }
}
