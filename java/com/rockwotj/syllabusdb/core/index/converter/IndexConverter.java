package com.rockwotj.syllabusdb.core.index.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import com.rockwotj.syllabusdb.core.document.CollectionId;
import com.rockwotj.syllabusdb.core.document.FieldName;
import com.rockwotj.syllabusdb.core.document.FieldPath;
import com.rockwotj.syllabusdb.core.document.Value;
import com.rockwotj.syllabusdb.core.index.Direction;
import com.rockwotj.syllabusdb.core.index.Index;
import com.rockwotj.syllabusdb.core.index.IndexField;
import com.rockwotj.syllabusdb.core.index.IndexName;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A converter to/from serialized JSON from our in memory models for a JSON object. */
public final class IndexConverter {
  private static final Gson gson;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Index.class, new IndexAdapter());
    gson = builder.create();
  }

  private IndexConverter() {}

  public static Index fromBytes(@Nonnull ByteArray bytes) {
    var reader = new InputStreamReader(bytes.toInputStream(), StandardCharsets.UTF_8);
    return gson.fromJson(reader, Index.class);
  }

  public static ByteArray toBytes(@Nonnull Index doc) {
    return ByteArray.copyUtf8(gson.toJson(doc));
  }

  private static class IndexAdapter extends TypeAdapter<Index> {
    private final ValueAdapter valueAdapter = new ValueAdapter();

    @Override
    public void write(JsonWriter writer, Index index) throws IOException {
      writer.beginObject();
      writer.name("name").value(index.name().raw());
      writer.name("collection").value(index.collection().raw());
      writer.name("fields");
      writer.beginArray();
      for (IndexField field : index.fields()) {
        writer
            .beginObject()
            .name("type")
            .value(field.direction().isAsc() ? "asc" : "desc")
            .name("field")
            .value(
                field.path().segments().stream()
                    .map(FieldName::raw)
                    .collect(Collectors.joining(".")))
            .endObject();
      }
      writer.endArray();
      writer.endObject();
    }

    @Override
    public Index read(JsonReader reader) throws IOException {
      reader.beginObject();
      IndexName indexName = null;
      CollectionId collection = null;
      List<IndexField> fields = null;
      var name = reader.nextName();
      switch (name) {
        case "name" -> indexName = new IndexName(reader.nextString());
        case "collection" -> collection = new CollectionId(reader.nextString());
        case "fields" -> {
          fields = new ArrayList<>();
          reader.beginArray();
          while (reader.peek() != JsonToken.END_ARRAY) {
            reader.beginObject();
            Direction direction = null;
            FieldPath path = null;
            while (reader.peek() != JsonToken.END_OBJECT) {
              name = reader.nextName();
              switch (name) {
                case "type" -> {
                  name = reader.nextString();
                  direction =
                      switch (name) {
                        case "asc" -> Direction.ASCENDING;
                        case "desc" -> Direction.DESCENDING;
                        default -> throw new IllegalArgumentException("Unknown direction: " + name);
                      };
                }
                case "field" -> {
                  var raw = reader.nextString();
                  path =
                      new FieldPath(
                          Arrays.stream(raw.split(Pattern.quote(".")))
                              .map(FieldName::new)
                              .toList());
                }
                default -> throw new IllegalArgumentException("Unknown field: " + name);
              }
            }
            fields.add(new IndexField(path, direction));
            reader.endObject();
          }
          reader.endArray();
        }
        default -> throw new IllegalArgumentException("Unknown field: " + name);
      }
      reader.endObject();
      return new Index(indexName, collection, fields);
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
