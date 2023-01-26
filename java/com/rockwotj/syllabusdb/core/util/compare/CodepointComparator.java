package com.rockwotj.syllabusdb.core.util.compare;

import java.util.Comparator;

/**
 * The JVM works in UTF-16 codepoints, which doesn't always give you the same sort order as UTF-32
 * (AKA raw codepoints).
 *
 * <p>See: https://icu-project.org/docs/papers/utf16_code_point_order.html
 *
 * <p>This comparator uses codepoints directly to compare for the correct ordering.
 */
public final class CodepointComparator implements Comparator<String> {
  public static CodepointComparator INSTANCE = new CodepointComparator();

  private CodepointComparator() {}

  @Override
  public int compare(String a, String b) {
    var aIt = a.codePoints().iterator();
    var bIt = b.codePoints().iterator();
    while (aIt.hasNext() && bIt.hasNext()) {
      var aElem = aIt.nextInt();
      var bElem = bIt.nextInt();
      var cmp = Integer.compareUnsigned(aElem, bElem);
      if (cmp != 0) return cmp;
    }
    return Boolean.compare(aIt.hasNext(), bIt.hasNext());
  }
}
