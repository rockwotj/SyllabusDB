package com.rockwotj.syllabusdb.core.util.concurrent;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/** Exception thrown when there was a timeout trying to acquire a lock. */
public class LockAcquisitionTimeoutException extends TimeoutException {
  public LockAcquisitionTimeoutException(Duration timeout) {
    super("Failed to acquire lock after " + timeout);
  }
}
