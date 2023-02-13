package com.rockwotj.syllabusdb.core.util.concurrent;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.collect.Collections2;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("CheckReturnValue")
public class UpgradableLockTest {

  private UpgradableLock lock;
  private ExecutorService fooThread;
  private ExecutorService barThread;

  @Before
  public void setup() {
    lock = UpgradableLock.createWithTimeout(Duration.ZERO);
    fooThread = Executors.newSingleThreadExecutor();
    barThread = Executors.newSingleThreadExecutor();
  }

  @After
  public void cleanup() {
    fooThread.shutdown();
    barThread.shutdown();
  }

  @Test
  public void canUpgradeReadLockToWriteLock() throws Exception {
    var acquiredReadLock = lock.readLock().lock();
    var acquiredWriteLock = lock.writeLock().lock();
    // Doesn't throw
    acquiredWriteLock.release();
    acquiredReadLock.release();
  }

  @Test
  public void canUpgradeReadLockToWriteLock_releaseInReverseOrder() throws Exception {
    var acquiredReadLock = lock.readLock().lock();
    var acquiredWriteLock = lock.writeLock().lock();
    // Doesn't throw
    acquiredReadLock.release();
    acquiredWriteLock.release();
  }

  @Test
  public void canDowngradeWriteLock() throws Exception {
    var acquiredWriteLock = lock.writeLock().lock();
    var acquiredReadLock = lock.readLock().lock();
    // Doesn't throw
    acquiredWriteLock.release();
    acquiredReadLock.release();
  }

  @Test
  public void isReentrant() throws Exception {
    var testCases =
        Collections2.permutations(
            List.of(lock.readLock(), lock.readLock(), lock.writeLock(), lock.writeLock()));
    for (var testCase : testCases) {
      var acquiredLocks = new ArrayList<AcquiredLock>();
      for (var lock : testCase) {
        acquiredLocks.add(lock.lock());
      }
      for (var acquired : acquiredLocks) {
        acquired.release();
      }
    }
  }

  @Test
  public void waitsForReadLocksToRelease() throws Exception {
    var fooThreadAcquiredReadLock = runInFooThread(() -> lock.readLock().lock());
    runInBarThread(() -> assertLocked(lock.writeLock()));
    runInFooThread(() -> fooThreadAcquiredReadLock.release());
    runInBarThread(() -> lock.writeLock().lock());
  }

  @Test(expected = IllegalStateException.class)
  public void cannotReleaseInAnotherThread() throws Exception {
    var fooThreadLock = runInFooThread(() -> lock.writeLock().lock());
    fooThreadLock.release();
  }

  private <T> Future<T> submitInFooThread(Callable<T> action) {
    return fooThread.submit(action);
  }

  private <T> T runInFooThread(Callable<T> action) throws Exception {
    return submitInFooThread(action).get(1, TimeUnit.SECONDS);
  }

  private void runInFooThread(ThrowingRunnable action) throws Exception {
    submitInFooThread(
            () -> {
              action.run();
              return null;
            })
            .get(1, TimeUnit.SECONDS);
  }

  private <T> Future<T> submitInBarThread(Callable<T> action) {
    return barThread.submit(action);
  }

  private <T> T runInBarThread(Callable<T> action) throws Exception {
    return submitInBarThread(action).get(1, TimeUnit.SECONDS);
  }

  private void runInBarThread(ThrowingRunnable action) throws Exception {
    submitInBarThread(
            () -> {
              action.run();
              return null;
            })
        .get(1, TimeUnit.SECONDS);
  }

  private void assertLocked(Lock lock) throws Exception {
    try {
      lock.lock();
    } catch (TimeoutException e) {
      // Success!
      return;
    }
    assertWithMessage("Expected lock to be locked").fail();
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
