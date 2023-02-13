package com.rockwotj.syllabusdb.core.util.concurrent;

/**
 * When an interrupt exception happens, we don't currently do any interrupts, so these would be
 * unexpected.
 */
public class UnexpectedInterruptedException extends RuntimeException {
  public UnexpectedInterruptedException(InterruptedException e) {
    super(e);
  }
}
