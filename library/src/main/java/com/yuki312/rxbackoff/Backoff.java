package com.yuki312.rxbackoff;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import java.util.concurrent.TimeUnit;

public class Backoff {

  /**
   * Give up on retry
   */
  public static final long ABORT = 0;

  /**
   * Debug trace
   */
  static final boolean TRACE = true;

  private final BackoffAlgorithm algorithm;
  private final int maxRetryCount;
  private final long maxElapsedTime; // milliseconds

  private int retryCount = 0;
  private long elapsedTime = 0L;

  private Backoff(@NonNull BackoffAlgorithm algorithm, int maxRetryCount, long maxElapsedTime) {
    this.algorithm = algorithm;
    this.maxRetryCount = maxRetryCount;
    this.maxElapsedTime = maxElapsedTime;
  }

  /**
   * @return interval until the next retry
   */
  public long interval() {
    retryCount++;
    if (retryCount > maxRetryCount) {
      return ABORT;
    }

    long next = algorithm.interval(retryCount, elapsedTime);
    if (next == ABORT) {
      return ABORT;
    }
    if (next < 0) {
      throw new IllegalStateException("Interval is invalid. Must be greater than 0.");
    }

    elapsedTime += next;
    if (elapsedTime > maxElapsedTime) {
      return ABORT;
    }

    return next;
  }

  /**
   * @return number of retries
   */
  public int getRetryCount() {
    return retryCount;
  }

  /**
   * @return elapsed time (milliseconds)
   */
  public long getElapsedTime() {
    return elapsedTime;
  }

  public static class Builder {

    public static final int DEFAULT_MAX_RETRY_COUNT = 10;

    public static final long DEFAULT_MAX_ELAPSED_TIME = 60_000L;

    private int maxRetryCount = DEFAULT_MAX_RETRY_COUNT;
    private long maxElapsedTime = DEFAULT_MAX_ELAPSED_TIME;
    private BackoffAlgorithm algorithm = new ExponentialAlgorithm();

    /**
     * Set backoff algorithm.
     * You can choose from the following algorithms or you can set your own algorithm.
     *
     * <ul>
     * <li>{@link FixedIntervalAlgorithm}</li>
     * <li>{@link ExponentialAlgorithm}</li>
     * </ul>
     *
     * or e.g.
     * <pre><code>
     *   public long interval(int retryCount, long elapsedTime) {
     *     2F.pow(retry - 1).toLong().times(1000L).coerceAtMost(5000L)
     *   }
     * </code></pre>
     *
     * When you want to force stop retrying process, return ABORT.
     *
     * @param algorithm Algorithm for calculating the interval time until the next retry
     * @return Backoff builder
     * @see BackoffAlgorithm
     */
    public Builder setAlgorithm(@NonNull BackoffAlgorithm algorithm) {
      this.algorithm = algorithm;
      return this;
    }

    /**
     * Set the maximum count of retry.
     * The retry is aborted when either the maximum count or the maximum elapsed time is satisfied.
     *
     * The count must be greater or equal 0.
     *
     * @param count maximum count of retry
     * @return Backoff builder
     * @see #DEFAULT_MAX_RETRY_COUNT
     * @see #setUnlimitedRetryCount()
     */
    public Builder setMaxRetryCount(@IntRange(from = 0) int count) {
      if (count < 0) {
        throw new IllegalArgumentException("MaxRetryCount is invalid. Must be greater or equal 0.");
      }

      this.maxRetryCount = count;
      return this;
    }

    public Builder setUnlimitedRetryCount() {
      setMaxRetryCount(Integer.MAX_VALUE);
      return this;
    }

    /**
     * Set the maximum elapsed time to retry milliseconds time.
     * The retry is aborted when either the maximum count or the maximum elapsed time is satisfied.
     *
     * The elapsed time must be greater or equal 0.
     *
     * @param elapsedTime maximum elapsed time in milliseconds
     * @param unit the units of time that {@code elapsedTime} is expressed in
     * @return Backoff builder
     * @see #DEFAULT_MAX_ELAPSED_TIME
     * @see #setUnlimitedElapsedTime()
     */
    public Builder setMaxElapsedTime(@IntRange(from = 0) long elapsedTime, TimeUnit unit) {
      long t = unit.toMillis(elapsedTime);
      if (t < 0) {
        throw new IllegalArgumentException("MaxElapsedTime is invalid. Must be greater or equal 0");
      }

      this.maxElapsedTime = t;
      return this;
    }

    public Builder setUnlimitedElapsedTime() {
      setMaxElapsedTime(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      return this;
    }

    @NonNull public Backoff build() {
      return new Backoff(algorithm, maxRetryCount, maxElapsedTime);
    }
  }

  static void trace(long next, long low, long high) {
    System.out.println(next + " (" + low + ".." + high + ")");
  }
}
