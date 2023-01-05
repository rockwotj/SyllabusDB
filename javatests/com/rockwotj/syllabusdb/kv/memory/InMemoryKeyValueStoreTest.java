package com.rockwotj.syllabusdb.kv.memory;

import static com.google.common.truth.Truth.assertThat;

import com.rockwotj.syllabusdb.core.bytes.ByteArray;
import com.rockwotj.syllabusdb.kv.api.KeyValueStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InMemoryKeyValueStoreTest {
  private KeyValueStore store;

  @Before
  public void setup() {
    store = new InMemoryKeyValueStore();
  }

  private static final ByteArray BAZ = ByteArray.copyUtf8("baz");
  private static final ByteArray FIZZ = ByteArray.copyUtf8("fizz");
  private static final ByteArray FOO = ByteArray.copyUtf8("foo");
  private static final ByteArray BAR = ByteArray.copyUtf8("bar");
  private static final ByteArray QUX = ByteArray.copyUtf8("qux");
  private static final ByteArray THUD = ByteArray.copyUtf8("thud");

  @Test
  public void canWriteAndRead() {
    store.write(
        KeyValueStore.Write.put(BAZ, FIZZ),
        KeyValueStore.Write.put(FOO, BAR),
        KeyValueStore.Write.put(QUX, THUD));
    assertThat(fullScan())
        .containsExactly(Map.entry(BAZ, FIZZ), Map.entry(FOO, BAR), Map.entry(QUX, THUD))
        .inOrder();
  }

  @Test
  public void canDelete() {
    store.write(
        KeyValueStore.Write.put(BAZ, FIZZ),
        KeyValueStore.Write.put(FOO, BAR),
        KeyValueStore.Write.put(QUX, THUD));
    assertThat(fullScan())
        .containsExactly(Map.entry(BAZ, FIZZ), Map.entry(FOO, BAR), Map.entry(QUX, THUD))
        .inOrder();
    store.write(KeyValueStore.Write.delete(FOO));
    assertThat(fullScan()).containsExactly(Map.entry(BAZ, FIZZ), Map.entry(QUX, THUD)).inOrder();
  }

  @Test
  public void keepsSnapshot() {
    store.write(KeyValueStore.Write.put(BAZ, FIZZ), KeyValueStore.Write.put(QUX, THUD));
    var snapshot = store.cursor();
    store.write(KeyValueStore.Write.put(FOO, BAR));
    assertThat(fullScan(snapshot))
        .containsExactly(Map.entry(BAZ, FIZZ), Map.entry(QUX, THUD))
        .inOrder();
    // Now everything shows up.
    assertThat(fullScan())
        .containsExactly(Map.entry(BAZ, FIZZ), Map.entry(FOO, BAR), Map.entry(QUX, THUD))
        .inOrder();
  }

  private List<Map.Entry<ByteArray, ByteArray>> fullScan() {
    return fullScan(store.cursor());
  }

  private List<Map.Entry<ByteArray, ByteArray>> fullScan(KeyValueStore.Cursor cursor) {
    var results = new ArrayList<Map.Entry<ByteArray, ByteArray>>();
    for (cursor.seekToStart(); cursor.valid(); cursor.next()) {
      results.add(Map.entry(cursor.key(), cursor.value()));
    }
    return results;
  }
}
