package com.rockwotj.syllabusdb.core.util.concurrent;

import javax.annotation.CheckReturnValue;

/** A simple RAII pattern lock. */
public interface Lock {
  /** Lock the given lock returning a lease that can release the lock. */
  @CheckReturnValue
  AcquiredLock lock() throws LockAcquisitionTimeoutException;
}
