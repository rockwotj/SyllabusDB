package com.rockwotj.syllabusdb.core.document.converter;

import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import com.rockwotj.syllabusdb.core.document.CollectionId;
import com.rockwotj.syllabusdb.core.document.DocId;
import com.rockwotj.syllabusdb.core.document.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IdConverter {
    private IdConverter() {}

    public static ByteArray toBytes(@Nonnull CollectionId id) {
        return ByteArray.copyUtf8(id.raw());
    }

    public static ByteArray toBytes(@Nonnull DocId id) {
        return ByteArray.copyUtf8(id.raw());
    }

    public static ByteArray toBytes(@Nonnull Path path) {
        return ByteArray.copyUtf8(toString(path));
    }
    public static String toString(@Nonnull Path path) {
        return path.collection().raw() + "/" + path.id().raw();
    }

    @Nullable
    public static CollectionId collectionFromBytes(ByteArray bytes) {
        String raw = bytes.toUtf8();
        try {
            return new CollectionId(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    @Nullable
    public static DocId docIdFromBytes(ByteArray bytes) {
        String raw = bytes.toUtf8();
        try {
            return new DocId(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    @Nullable
    public static Path pathFromBytes(ByteArray bytes) {
        return pathFromString(bytes.toUtf8());
    }
    @Nullable
    public static Path pathFromString(String raw) {
        int idx = raw.indexOf('/');
        if (idx == -1) return null;
        try {
            CollectionId collection = new CollectionId(raw.substring(0, idx));
            DocId id = new DocId(raw.substring(idx + 1));
            return new Path(collection, id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
