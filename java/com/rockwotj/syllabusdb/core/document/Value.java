package com.rockwotj.syllabusdb.core.document;

import com.google.common.collect.ImmutableSortedMap;
import com.rockwotj.syllabusdb.core.util.compare.CodepointComparator;
import com.rockwotj.syllabusdb.core.util.compare.TotalOrderDoubleComparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A value within a JSON document.
 *
 * <p>Values can have 6 different types: - null - boolean - number (which while the JSON spec is not
 * specific about these we'll assume they are doubles) - strings - lists - objects (essentially
 * nested JSON documents).
 */
@Immutable
public final class Value implements Comparable<Value> {

  public static Value NULL = new Value(null);
  public static Value FALSE = new Value(Boolean.FALSE);
  public static Value TRUE = new Value(Boolean.TRUE);
  public static Value NAN = new Value(Double.NaN);
  public static Value EMPTY_LIST = new Value(List.of());
  public static Value EMPTY_OBJECT = new Value(ImmutableSortedMap.of());

  // This class is an unsafe wrapper around an arbitrary object.
  // This does box primitives, but we're not trying to micro optimize here.
  private final @Nullable Object data;

  private Value(@Nullable Object data) {
    this.data = data;
  }

  public static Value of(boolean val) {
    return val ? TRUE : FALSE;
  }

  public static Value of(double val) {
    if (Double.isNaN(val)) {
      // Canonicalize to a single NaN value (yes there are different kinds of NaN).
      return new Value(Double.NaN);
    }
    return new Value(val);
  }

  public static Value of(String val) {
    return new Value(val);
  }

  public static Value ofList(Value v1, Value... rest) {
    var list = new ArrayList<Value>();
    list.add(v1);
    list.addAll(Arrays.asList(rest));
    return new Value(Collections.unmodifiableList(list));
  }

  public static Value of(List<Value> val) {
    return new Value(List.copyOf(val));
  }

  public static Value of(FieldName name, Value value) {
    return new Value(ImmutableSortedMap.of(name, value));
  }

  public static Value of(FieldName n1, Value v1, FieldName n2, Value v2) {
    return new Value(ImmutableSortedMap.of(n1, v1, n2, v2));
  }

  public static Value of(SortedMap<FieldName, Value> val) {
    return new Value(ImmutableSortedMap.copyOfSorted(val));
  }

  public Type type() {
    if (data == null) {
      return Type.Null;
    } else if (data instanceof Double) {
      return Type.Number;
    } else if (data instanceof Boolean) {
      return Type.Boolean;
    } else if (data instanceof String) {
      return Type.String;
    } else if (data instanceof List<?>) {
      return Type.List;
    } else {
      return Type.Object;
    }
  }

  public boolean isNaN() {
    return (data instanceof Double d) && d.isNaN();
  }

  public boolean asBoolean() {
    return (Boolean) Objects.requireNonNull(data);
  }

  public double asDouble() {
    return (double) Objects.requireNonNull(data);
  }

  @Nonnull
  public String asString() {
    return (String) Objects.requireNonNull(data);
  }

  public SortedMap<FieldName, Value> asObject() {
    return (SortedMap<FieldName, Value>) Objects.requireNonNull(data);
  }

  public List<Value> asList() {
    return (List<Value>) Objects.requireNonNull(data);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof Value other) {
      return compareTo(other) == 0;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(data);
  }

  @Override
  public String toString() {
    return Objects.toString(data);
  }

  @Override
  public int compareTo(Value other) {
    var cmp = this.type().compareTo(other.type());
    if (cmp != 0) return cmp;
    return switch (this.type()) {
      case Null -> 0;
      case Boolean -> Boolean.compare(this.asBoolean(), other.asBoolean());
      case Number -> {
        var a = this.asDouble();
        var b = other.asDouble();
        yield TotalOrderDoubleComparator.INSTANCE.compareDouble(a, b);
      }
      case String -> CodepointComparator.INSTANCE.compare(asString(), other.asString());
      case List -> {
        var a = asList();
        var b = other.asList();
        var minSize = Math.min(a.size(), b.size());
        for (int i = 0; i < minSize; i++) {
          cmp = a.get(i).compareTo(b.get(i));
          if (cmp != 0) yield cmp;
        }
        yield Integer.compare(a.size(), b.size());
      }
      case Object -> {
        var a = asObject().entrySet().iterator();
        var b = other.asObject().entrySet().iterator();
        while (a.hasNext() && b.hasNext()) {
          var aEntry = a.next();
          var bEntry = b.next();
          cmp = aEntry.getKey().compareTo(bEntry.getKey());
          if (cmp != 0) yield cmp;
          cmp = aEntry.getValue().compareTo(bEntry.getValue());
          if (cmp != 0) yield cmp;
        }
        yield Boolean.compare(a.hasNext(), b.hasNext());
      }
    };
  }

  public enum Type {
    Null,
    Boolean,
    Number,
    String,
    List,
    Object
  }
}
