package com.rockwotj.syllabusdb.core.index;

final class Constants {

  public static final byte MAX_UNSIGNED_BYTE = -1;

  public static byte[] ENCODED_MIN_VALUE = new byte[] {0x0, 0x1};
  public static byte[] ESCAPED_NULL = new byte[] {0x0, MAX_UNSIGNED_BYTE};
  public static byte[] ESCAPED_FF =
      new byte[] {
        MAX_UNSIGNED_BYTE, 0x0,
      };
  public static byte[] ENCODED_MAX_VALUE = new byte[] {MAX_UNSIGNED_BYTE, MAX_UNSIGNED_BYTE};

  private Constants() {}
}
