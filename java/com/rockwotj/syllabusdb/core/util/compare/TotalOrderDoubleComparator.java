package com.rockwotj.syllabusdb.core.util.compare;

import java.util.Comparator;

/** A comparator for doubles that results in a total order - all NANs are equal and listed first. */
public final class TotalOrderDoubleComparator implements Comparator<Double> {
  public static TotalOrderDoubleComparator INSTANCE = new TotalOrderDoubleComparator();

  private TotalOrderDoubleComparator() {}

  public int compareDouble(double a, double b) {
    var aNaN = Double.isNaN(a);
    var bNaN = Double.isNaN(b);
    if (aNaN || bNaN) {
      return -Boolean.compare(aNaN, bNaN);
    } else {
      return Double.compare(a, b);
    }
  }

  @Override
  public int compare(Double a, Double b) {
    return compareDouble(a, b);
  }
}
