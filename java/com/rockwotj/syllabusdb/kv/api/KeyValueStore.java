package com.rockwotj.syllabusdb.kv.api;

import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Key-Value Store in a similar interface as LevelDB.
 *
 * <p>The key value store need to provide two main features:
 *
 * <ul>
 *   <li>A cursor that operates on a snapshot of the data.
 *   <li>An atomic batch write operation.
 * </ul>
 */
public interface KeyValueStore {

  /**
   * Returning a cursor over a snapshot of the key/value store. This cursor will not be updated as
   * writes happen.
   */
  Cursor cursor();

  /** Perform a series of writes atomically. */
  void write(List<Write> batch);

  default void write(Write... batch) {
    write(List.of(batch));
  }

  /**
   * A bidirectional iterator over a key/value store.
   *
   * <p>* Supports efficient skipping to a specific key.
   */
  interface Cursor extends AutoCloseable {
    /** Move the cursor to the next record. Return true if not pointing after the end. */
    void next();

    /**
     * Position at the first key in the source. The cursor is valid() after this call iff the source
     * is not empty.
     */
    void seekToStart();
    /**
     * Moves to the previous entry in the source. After this call, valid() is true iff the cursor
     * was not positioned at the first entry in source.
     */
    void previous();
    /**
     * Position at the first key in the source that is at or past target. The cursor is valid()
     * after this call iff the source contains an entry that comes at or past target.
     */
    void seek(@Nonnull ByteArray key);
    /**
     * Position at the last key in the source. The cursor is valid() after this call iff the source
     * is not empty.
     */
    void seekToEnd();
    /**
     * An iterator is either positioned at a key/value pair, or not valid. This method returns true
     * iff the iterator is valid.
     */
    boolean valid();

    /**
     * The current key pointed at by the cursor.
     *
     * @throws IllegalStateException if the cursor is at the beginning or end.
     */
    @Nonnull
    ByteArray key();

    /**
     * The current value pointed at by the cursor.
     *
     * @throws IllegalStateException if the cursor is at the beginning or end.
     */
    @Nonnull
    ByteArray value();

    @Override
    public void close();
  }

  /** The record for a write. If value is null, it's a delete operation. */
  record Write(@Nonnull ByteArray key, @Nullable ByteArray value) {
    public Write {
      Objects.requireNonNull(key);
    }

    public static Write put(@Nonnull ByteArray key, @Nonnull ByteArray value) {
      return new Write(key, value);
    }

    public static Write delete(@Nonnull ByteArray key) {
      return new Write(key, null);
    }
  }
}
