package com.yuki312.rxbackoff;

import static com.yuki312.rxbackoff.Backoff.DEFAULT_INTERVAL;
import static com.yuki312.rxbackoff.Backoff.DEFAULT_MAX_INTERVAL;
import static com.yuki312.rxbackoff.Backoff.DEFAULT_MULTIPLIER;
import static com.yuki312.rxbackoff.Backoff.DEFAULT_RANGE;
import static com.yuki312.rxbackoff.Backoff.NO_RANGE;
import static com.yuki312.rxbackoff.Backoff.TRACE;

/**
 * Default exponential backoff interval:
 *
 * | Interval | Random range   | *Default random range ±20%
 * | -------- | -------------- |
 * | 500      | (400..600)     | *Default interval 500ms
 * | 750      | (600..900)     | *Default multiplier x1.5
 * | 1125     | (900..1350)    |
 * | 1687     | (1349..2024)   |
 * | 2531     | (2024..3037)   |
 * | 3796     | (3036..4555)   |
 * | 5695     | (4556..6834)   |
 * | 8542     | (6833..10250)  |
 * | 12814    | (10251..15376) |
 * | 15000    | (12000..18000) | *Default max interval 15,000ms
 * | 15000    | (12000..18000) |
 * | ...      | ...            |
 */
public class ExponentialAlgorithm implements BackoffAlgorithm {

  private final long interval;
  private final double multiplier;
  private final long maxInterval;
  private final double range;

  public ExponentialAlgorithm() {
    this(DEFAULT_INTERVAL, DEFAULT_MULTIPLIER, DEFAULT_MAX_INTERVAL, DEFAULT_RANGE);
  }

  /**
   * Construct Exponential algorithm.
   *
   * @param interval Wait time milliseconds until next retry
   * @param multiplier the multiplier that increases the interval
   * @param maxInterval the maximum interval. Truncate time that exceeds
   * @param range Intervals are randomly chosen within this range. For example, if 0.2 is specified
   * for range, the interval is selected within the range of ± 20%.
   */
  public ExponentialAlgorithm(long interval, double multiplier, long maxInterval, double range) {
    if (interval < 1L) {
      throw new IllegalArgumentException("initialInterval is invalid. Must be greater than 1.");
    }

    if (maxInterval < interval) {
      throw new IllegalArgumentException(
          "maxInterval is invalid. Must be greater or equal than initialInterval.");
    }

    if (multiplier < 1.0) {
      throw new IllegalArgumentException("Multiplier is invalid. Must be greater than 1.0.");
    }

    if (range < 0.0 || 1.0 <= range) {
      throw new IllegalArgumentException(
          "Range is invalid. Must be greater or equal 0.0 and lower than 1.0.");
    }

    this.interval = interval;
    this.multiplier = multiplier;
    this.maxInterval = maxInterval;
    this.range = range;
  }

  @Override public long nextInterval(int retryCount, long elapsedTime) {
    long next = (long) Math.min(interval * Math.pow(multiplier, retryCount - 1), maxInterval);

    // calculate random range interval
    if (range != NO_RANGE) {
      long min = (long) (next - (next * range));
      long max = (long) (next + (next * range));
      long rnd = min + (int) (Math.random() * ((max - min) + 1));
      if (TRACE) trace(Math.min(next, maxInterval), min, max);
      next = Math.min(rnd, maxInterval);
    }

    return next;
  }

  private void trace(long next, long min, long max) {
    System.out.println(Math.min(next, maxInterval) + " (" + min + ".." + max + ")");
  }
}
