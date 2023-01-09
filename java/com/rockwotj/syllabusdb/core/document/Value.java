package com.rockwotj.syllabusdb.core.document;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Value {

    public enum Type {
        Number,
        String,
        Boolean,
        Null,
        List,
        Object
    }

    private final @Nullable Object data;

    private Value(@Nullable Object data) {
        this.data = data;
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

    public static Value NULL = new Value(null);

    public static Value of(boolean val) {
        return new Value(val);
    }
    public static Value of(double val) {
        return new Value(val);
    }
    public static Value of(String val) {
        return new Value(val);
    }
    public static Value of(List<Value> val) {
        return new Value(List.copyOf(val));
    }
    public static Value of(Map<FieldName, Value> val) {
        return new Value(Map.copyOf(val));
    }

    public boolean asBoolean() {
        return (Boolean) data;
    }
    public double asDouble() {
        return (double) data;
    }
    public String asString() {
        return (String) data;
    }
    public Map<FieldName, Value> asMap() {
        return (Map<FieldName, Value>) data;
    }
    public List<Value> asList() {
        return (List<Value>) data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return Objects.equals(data, value.data);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data);
    }
}
