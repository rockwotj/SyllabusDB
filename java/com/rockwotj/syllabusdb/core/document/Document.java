package com.rockwotj.syllabusdb.core.document;

import javax.annotation.Nonnull;
import java.util.Map;

public record Document(@Nonnull DocId id, @Nonnull CollectionId collection, @Nonnull Map<FieldName, Value> fields) {
    public Document {
        fields = Map.copyOf(fields);
    }
    public Document(@Nonnull Path path, @Nonnull Map<FieldName, Value> fields) {
        this(path.id(), path.collection(), fields);
    }

    public Path path() {
        return new Path(collection, id);
    }
}