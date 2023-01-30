package com.rockwotj.syllabusdb.core.encoding.value;

/**
 * An exception thrown when decoding and we run into an invalid encoding. This situation can happen
 * when either `IndexEncoder` produces buggy input, `IndexDecoder` incorrectly decodes something, or
 * if there is some sort of data corruption.
 */
public class InvalidEncodingException extends RuntimeException {
  public InvalidEncodingException(String message) {
    super(message);
  }
}
