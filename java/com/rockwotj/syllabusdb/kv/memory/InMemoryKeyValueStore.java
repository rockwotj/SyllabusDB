package com.rockwotj.syllabusdb.kv.memory;

import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import com.rockwotj.syllabusdb.kv.api.KeyValueStore;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A simple copy on write in-memory key value store giving us the semantics we need. */
public final class InMemoryKeyValueStore implements KeyValueStore {
  @Nonnull private NavigableMap<ByteArray, ByteArray> data = new TreeMap<>();

  @Override
  public synchronized Cursor cursor() {
    return new InMemoryCursor(this.data);
  }

  @Override
  public synchronized void write(List<Write> batch) {
    var copy = new TreeMap<>(this.data);
    for (var write : batch) {
      if (write.value() == null) {
        copy.remove(write.key());
      } else {
        copy.put(write.key(), write.value());
      }
    }
    this.data = copy;
  }
}

final class InMemoryCursor implements KeyValueStore.Cursor {
  @Nonnull private final NavigableMap<ByteArray, ByteArray> data;

  @Nullable private Map.Entry<ByteArray, ByteArray> current;

  InMemoryCursor(@Nonnull NavigableMap<ByteArray, ByteArray> data) {
    this.data = data;
  }

  @Override
  public void next() {
    if (this.current == null) {
      throw new IllegalStateException();
    }
    this.current = this.data.higherEntry(this.current.getKey());
  }

  @Override
  public void seekToStart() {
    this.current = this.data.firstEntry();
  }

  @Override
  public void previous() {
    if (this.current == null) {
      throw new IllegalStateException();
    }
    this.current = this.data.lowerEntry(this.current.getKey());
  }

  @Override
  public void seek(@Nonnull ByteArray key) {
    this.current = this.data.ceilingEntry(key);
  }

  @Override
  public void seekToEnd() {
    this.current = this.data.lastEntry();
  }

  @Override
  public boolean valid() {
    return this.current != null;
  }

  @Nonnull
  @Override
  public ByteArray key() {
    if (this.current == null) {
      throw new IllegalStateException();
    }
    return this.current.getKey();
  }

  @Nonnull
  @Override
  public ByteArray value() {
    if (this.current == null) {
      throw new IllegalStateException();
    }
    return this.current.getValue();
  }

  @Override
  public void close() {
    // Nothing to do for now.
  }
}
