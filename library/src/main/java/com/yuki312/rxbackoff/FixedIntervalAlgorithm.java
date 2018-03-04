package com.yuki312.rxbackoff;

import java.util.concurrent.TimeUnit;

import static com.yuki312.rxbackoff.Backoff.DEFAULT_INTERVAL;

/**
 * Default fixed backoff interval:
 *
 * | Interval |
 * | -------- |
 * | 500      | *Default interval 500ms
 * | 500      |
 * | 500      |
 * | ...      |
 */
public class FixedIntervalAlgorithm implements BackoffAlgorithm {

  private final long interval; // millisecond

  /**
   * Construct fixed interval algorithm.
   *
   * @see Backoff#DEFAULT_INTERVAL
   */
  public FixedIntervalAlgorithm() {
    this.interval = DEFAULT_INTERVAL;
  }

  /**
   * Construct fixed interval algorithm.
   *
   * @param interval Wait time milliseconds until next retry
   * @param unit the units of time that {@code delayTime} is expressed in
   */
  public FixedIntervalAlgorithm(long interval, TimeUnit unit) {
    long t = unit.toMillis(interval);
    if (t < 1L) {
      throw new IllegalArgumentException("Interval is invalid. Must be greater than 1.");
    }
    this.interval = t;
  }

  @Override public long nextInterval(int retryCount, long elapsedTime) {
    return interval;
  }
}
