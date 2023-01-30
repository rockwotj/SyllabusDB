package com.rockwotj.syllabusdb.core.document.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import com.rockwotj.syllabusdb.core.document.Document;
import com.rockwotj.syllabusdb.core.document.FieldName;
import com.rockwotj.syllabusdb.core.document.Path;
import com.rockwotj.syllabusdb.core.document.Value;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A converter to/from serialized JSON from our in memory models for a JSON object. */
public final class DocConverter {
  private static final Gson gson;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Document.class, new DocAdapter());
    builder.registerTypeAdapter(Value.class, new ValueAdapter());
    gson = builder.create();
  }

  private DocConverter() {}

  public static Document fromBytes(@Nonnull ByteArray bytes) {
    var reader = new InputStreamReader(bytes.toInputStream(), StandardCharsets.UTF_8);
    return gson.fromJson(reader, Document.class);
  }

  public static ByteArray toBytes(@Nonnull Document doc) {
    return ByteArray.copyUtf8(gson.toJson(doc));
  }

  public static ByteArray toBytes(@Nonnull Value value) {
    return ByteArray.copyUtf8(gson.toJson(value));
  }

  private static class DocAdapter extends TypeAdapter<Document> {
    private final ValueAdapter valueAdapter = new ValueAdapter();

    @Override
    public void write(JsonWriter writer, Document document) throws IOException {
      writer.beginObject();
      writer.name("_id").value(IdConverter.toString(document.path()));
      for (Map.Entry<FieldName, Value> entry : document.fields().entrySet()) {
        writer.name(entry.getKey().raw());
        valueAdapter.write(writer, entry.getValue());
      }
      writer.endObject();
    }

    @Override
    public Document read(JsonReader reader) throws IOException {
      reader.beginObject();
      Path path = null;
      var fields = new HashMap<FieldName, Value>();
      while (reader.hasNext()) {
        var name = reader.nextName();
        if (name.equals("_id")) {
          path = IdConverter.pathFromString(name);
          continue;
        }
        var field = new FieldName(name);
        var value = valueAdapter.read(reader);
        fields.put(field, value);
      }
      reader.endObject();
      if (path == null) {
        throw new IllegalArgumentException("Invalid _id");
      }
      return new Document(path, fields);
    }
  }

  private static class ValueAdapter extends TypeAdapter<Value> {
    @Override
    public void write(JsonWriter writer, Value value) throws IOException {
      switch (value.type()) {
        case Number -> writer.value(value.asDouble());
        case String -> writer.value(value.asString());
        case Boolean -> writer.value(value.asBoolean());
        case Null -> writer.nullValue();
        case List -> {
          writer.beginArray();
          for (var elem : value.asList()) {
            write(writer, elem);
          }
          writer.endArray();
        }
        case Object -> {
          writer.beginObject();
          for (var entry : value.asObject().entrySet()) {
            writer.name(entry.getKey().raw());
            write(writer, entry.getValue());
          }
          writer.endObject();
        }
      }
    }

    @Override
    @Nullable
    public Value read(JsonReader reader) throws IOException {
      return switch (reader.peek()) {
        case BEGIN_ARRAY -> {
          reader.beginArray();
          var list = new ArrayList<Value>();
          while (reader.hasNext()) {
            list.add(read(reader));
          }
          reader.endArray();
          yield Value.of(list);
        }
        case BEGIN_OBJECT -> {
          reader.beginObject();
          var map = new TreeMap<FieldName, Value>();
          while (reader.hasNext()) {
            var name = reader.nextName();
            map.put(new FieldName(name), read(reader));
          }
          reader.endObject();
          yield Value.of(map);
        }
        case STRING -> Value.of(reader.nextString());
        case NUMBER -> Value.of(reader.nextDouble());
        case BOOLEAN -> Value.of(reader.nextBoolean());
        case NULL -> {
          reader.nextNull();
          yield Value.NULL;
        }
        default -> null;
      };
    }
  }
}
