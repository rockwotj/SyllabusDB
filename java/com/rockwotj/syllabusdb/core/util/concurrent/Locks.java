package com.rockwotj.syllabusdb.core.util.concurrent;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

class Locks {
  private Locks() {}

  /** Lock the lock with a timeout, translating exceptions appropriately. */
  public static void lockWithTimeout(Lock lock, Duration timeout)
      throws LockAcquisitionTimeoutException {
    try {
      if (!lock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
        throw new LockAcquisitionTimeoutException(timeout);
      }
    } catch (InterruptedException e) {
      throw new UnexpectedInterruptedException(e);
    }
  }

  /** Unlocks all locks in reverse order. */
  public static void unlockAll(List<Lock> locks) {
    for (int i = locks.size() - 1; i >= 0; --i) {
      Lock lock = locks.get(i);
      lock.unlock();
    }
  }
}
