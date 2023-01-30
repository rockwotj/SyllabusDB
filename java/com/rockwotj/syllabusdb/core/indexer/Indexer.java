package com.rockwotj.syllabusdb.core.indexer;

import com.rockwotj.syllabusdb.core.document.CollectionId;
import com.rockwotj.syllabusdb.core.document.Document;
import com.rockwotj.syllabusdb.core.document.Value;
import com.rockwotj.syllabusdb.core.index.Index;
import com.rockwotj.syllabusdb.core.index.IndexEntry;
import com.rockwotj.syllabusdb.core.index.IndexField;
import com.rockwotj.syllabusdb.core.index.IndexValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Given all the indexes for a database create the index entries for a document. */
@Immutable
public class Indexer {
  private final Map<CollectionId, List<Index>> indexes = new HashMap<>();

  public Indexer(@Nonnull List<Index> indexes) {
    for (Index index : indexes) {
      var collectionIndexes =
          this.indexes.computeIfAbsent(index.collection(), (collection) -> new ArrayList<>());
      collectionIndexes.add(index);
    }
  }

  public List<IndexEntry> index(Document document) {
    List<IndexEntry> entries = new ArrayList<>();
    for (Index index : indexes.getOrDefault(document.collection(), List.of())) {
      IndexEntry entry = indexOne(index, document);
      // We support sparse indexes - if the field isn't in the supplied document, then we don't
      // write an
      // entry in the index.
      if (entry == null) continue;
      entries.add(entry);
    }
    return entries;
  }

  @Nullable
  private IndexEntry indexOne(Index index, Document document) {
    List<IndexValue> values = new ArrayList<>();
    for (IndexField field : index.fields()) {
      Value value = document.get(field.path());
      if (value == null) return null;
      values.add(new IndexValue(value, field.direction()));
    }
    return new IndexEntry(index.name(), values);
  }
}
