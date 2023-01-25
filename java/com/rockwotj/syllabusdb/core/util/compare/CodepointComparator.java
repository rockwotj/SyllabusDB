package com.rockwotj.syllabusdb.core.util.compare;

import java.util.Comparator;

/** Compare by codepoints not UTF-16 characters */
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
