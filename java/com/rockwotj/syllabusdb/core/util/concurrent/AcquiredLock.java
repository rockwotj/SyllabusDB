package com.rockwotj.syllabusdb.core.util.concurrent;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * A lock that has been required and can now be released.
 *
 * <p>Locks must be released on the same thread they were acquired, and can only be released once.
 */
public class AcquiredLock {
  private final Thread owningThread;
  private final List<Lock> locks;

  private AcquiredLock(Thread owningThread, List<Lock> locks) {
    this.owningThread = owningThread;
    this.locks = locks;
  }

  static AcquiredLock forCurrentThread(Lock lock, Lock... rest) {
    var allLocks = new ArrayList<Lock>();
    allLocks.add(lock);
    allLocks.addAll(Arrays.asList(rest));
    return new AcquiredLock(Thread.currentThread(), allLocks);
  }

  /** Release the lock. */
  public void release() {
    var callingThread = Thread.currentThread();
    checkState(
        callingThread == owningThread,
        "Attempting to release lock on thread %s, but the lock was acquired on thread %s",
        callingThread.getName(),
        owningThread.getName());
    checkState(!locks.isEmpty(), "Locks have already been released");
    Locks.unlockAll(locks);
    locks.clear();
  }
}
