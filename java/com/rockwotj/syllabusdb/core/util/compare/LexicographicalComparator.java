package com.rockwotj.syllabusdb.core.util.compare;

import java.util.Comparator;

/** Compare 2 lists lexicographically. */
public final class LexicographicalComparator<T> implements Comparator<Iterable<T>> {

  private static final LexicographicalComparator<Comparable<Object>> NATURAL_ORDER_INSTANCE =
      new LexicographicalComparator<Comparable<Object>>(Comparator.naturalOrder());

  public static <T extends Comparable<? super T>> Comparator<Iterable<T>> naturalOrder() {
    return (LexicographicalComparator<T>) NATURAL_ORDER_INSTANCE;
  }

  public static <T> Comparator<Iterable<T>> create(Comparator<T> comparator) {
    return new LexicographicalComparator<T>(comparator);
  }

  private final Comparator<T> elementComparator;

  private LexicographicalComparator(Comparator<T> elementComparator) {
    this.elementComparator = elementComparator;
  }

  @Override
  public int compare(Iterable<T> a, Iterable<T> b) {
    var aIt = a.iterator();
    var bIt = b.iterator();
    while (aIt.hasNext() && bIt.hasNext()) {
      var aElem = aIt.next();
      var bElem = bIt.next();
      var cmp = elementComparator.compare(aElem, bElem);
      if (cmp != 0) return cmp;
    }
    return Boolean.compare(aIt.hasNext(), bIt.hasNext());
  }
}
