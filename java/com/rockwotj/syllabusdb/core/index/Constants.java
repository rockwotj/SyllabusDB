package com.rockwotj.syllabusdb.core.index;

/**
 * Our index format requires the ability to write bytes, as well as additional sentinel MIN/MAX
 * values that come before/after all other bytes.
 *
 * <p>In order to do this we need at least 258 symbols. To do this each byte 0x01 - 0xFE is mapped
 * to itself. This leaves us with four symbols left: 0x00, 0xFF, MIN_VALUE, MAX_VALUE. We encode
 * them as the following sequences:
 *
 * <ul>
 *   <li>MIN_VALUE -> 0x00 0x00
 *   <li>0x00 -> 0x00 0xFF
 *   <li>0xFF -> 0xFF 0x01
 *   <li>MAX_VALUE -> 0xFF 0xFF
 * </ul>
 *
 * <p>There are more 2 byte sequences we could encode with the 0x00 and 0xFF prefixes, but those
 * slots are unused.
 *
 * <p>This is essentially the exact same encoding used as OrderedCode. The implementation of which
 * is open sourced from Google under an Apache 2.0 License.
 *
 * <p>See the following for the original implementation in C++
 *
 * <p><a
 * href="https://github.com/firebase/firebase-ios-sdk/blob/4171a3b5/Firestore/core/src/util/ordered_code.h">ordered_code.h</a>
 * <a
 * href="https://github.com/firebase/firebase-ios-sdk/blob/4171a3b5/Firestore/core/src/util/ordered_code.cc">ordered_code.cc</a>
 */
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
