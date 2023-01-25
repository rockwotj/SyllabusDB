package com.rockwotj.syllabusdb.core.document;

import static com.google.common.truth.Truth.assertThat;

import com.rockwotj.syllabusdb.core.document.testdata.Values;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ValueTest {

  @Test
  public void comparison() {
    assertThat(Values.TOTAL_ORDER).isInStrictOrder();
  }
}
