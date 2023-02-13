package com.rockwotj.syllabusdb.core.util.concurrent;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.CheckReturnValue;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Allows for upgrading a read lock into a write lock, which the standard JDK does not support.
 *
 * <p>Inspired by <a href="https://github.com/npgall/concurrent-locks">Niall Gallagher's work on
 * ReentrantReadWriteUpdateLock</a>
 */
public final class UpgradableLock {
  final ReentrantReadWriteLock readWriteLock;
  private final ReentrantLock updateMutex;

  private final Read readLock;
  private final Write writeLock;
  private final Duration timeout;

  private UpgradableLock(Duration timeout) {
    this.timeout = timeout;
    readWriteLock = new ReentrantReadWriteLock(true);
    updateMutex = new ReentrantLock();
    writeLock = new Write();
    readLock = new Read();
  }

  public static UpgradableLock createWithTimeout(Duration timeout) {
    checkArgument(!timeout.isNegative(), "Lock timeout must be >= 0");
    return new UpgradableLock(timeout);
  }

  /**
   * Read lock that allow for shared access to a resource.
   *
   * <p>Can be acquired while holding a write lock to allow for lock downgrades.
   */
  public Read readLock() {
    return readLock;
  }

  /**
   * Write lock that allows for exclusive access to a resource.
   *
   * <p>Can be acquired while holding a read lock to allow for lock upgrades.
   */
  public Write writeLock() {
    return writeLock;
  }

  public class Read implements Lock {
    Read() {}

    @Override
    @CheckReturnValue
    public AcquiredLock lock() throws LockAcquisitionTimeoutException {
      // It's always valid to grab a read lock.
      Locks.lockWithTimeout(readWriteLock.readLock(), timeout);
      return AcquiredLock.forCurrentThread(readWriteLock.readLock());
    }
  }

  public class Write implements Lock {
    Write() {}

    @Override
    @CheckReturnValue
    public AcquiredLock lock() throws LockAcquisitionTimeoutException {
      var readLocksHeld = readWriteLock.getReadHoldCount();
      // If we currently have a read lock, this is a slightly more complex interaction.
      // The JDK doesn't allow upgrading a read lock, so we do via the update mutex.
      var startTime = System.nanoTime();
      Locks.lockWithTimeout(updateMutex, timeout);
      // Unlock the current read locks so we can grab the write lock.
      for (var i = 0; i < readLocksHeld; ++i) {
        readWriteLock.readLock().unlock();
      }
      // Grab the write lock adjusting the timeout for the time we spent acquiring the updateMutex.
      try {
        Locks.lockWithTimeout(
            readWriteLock.writeLock(), timeout.minusNanos(System.nanoTime() - startTime));
      } catch (RuntimeException | TimeoutException e) {
        updateMutex.unlock();
        throw e;
      }
      // Relock the read locks now that we have the write lock.
      for (var i = 0; i < readLocksHeld; ++i) {
        Locks.lockWithTimeout(readWriteLock.readLock(), Duration.ZERO);
      }
      return AcquiredLock.forCurrentThread(updateMutex, readWriteLock.writeLock());
    }
  }
}
