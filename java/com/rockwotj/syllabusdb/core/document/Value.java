package com.rockwotj.syllabusdb.core.document;

import com.google.common.collect.ImmutableSortedMap;
import com.rockwotj.syllabusdb.core.util.compare.CodepointComparator;
import com.rockwotj.syllabusdb.core.util.compare.LexicographicalComparator;
import com.rockwotj.syllabusdb.core.util.compare.TotalOrderDoubleComparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A value within a JSON document.
 *
 * <p>Values can have 6 different types:
 *
 * <ul>
 *   <li>- null
 *   <li>- boolean
 *   <li>- number (which while the JSON spec is not specific about these we'll assume they are
 *       doubles)
 *   <li>- strings
 *   <li>- lists
 *   <li>- objects (essentially nested JSON documents)
 */
@Immutable
public final class Value implements Comparable<Value> {

  private static final Comparator<Iterable<Map.Entry<FieldName, Value>>> OBJECT_COMPARATOR =
      LexicographicalComparator.create(
          Map.Entry.<FieldName, Value>comparingByKey().thenComparing(Map.Entry.comparingByValue()));

  public static final Value NULL = new Value(null);
  public static final Value FALSE = new Value(Boolean.FALSE);
  public static final Value TRUE = new Value(Boolean.TRUE);
  public static final Value NAN = new Value(Double.NaN);
  public static final Value EMPTY_LIST = new Value(List.of());
  public static final Value EMPTY_OBJECT = new Value(ImmutableSortedMap.of());

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
    var myType = type();
    var cmp = myType.compareTo(other.type());
    if (cmp != 0) return cmp;
    return switch (myType) {
      case Null -> 0;
      case Boolean -> Boolean.compare(asBoolean(), other.asBoolean());
      case Number -> TotalOrderDoubleComparator.INSTANCE.compareDouble(
          asDouble(), other.asDouble());
      case String -> CodepointComparator.INSTANCE.compare(asString(), other.asString());
      case List -> LexicographicalComparator.<Value>naturalOrder()
          .compare(asList(), other.asList());
      case Object -> OBJECT_COMPARATOR.compare(asObject().entrySet(), other.asObject().entrySet());
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
