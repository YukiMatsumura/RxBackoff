package com.yuki312.backoff.core;

import static java.lang.Math.pow;

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
 * | 12814    | (10251..15000) |
 * | 15000    | (12000..15000) | *Default max interval 15,000ms
 * | 15000    | (12000..15000) |
 * | ...      | ...            |
 */
public class ExponentialAlgorithm implements BackoffAlgorithm {

  /**
   * the default interval
   */
  public static final long DEFAULT_INTERVAL = 500L;

  /**
   * the default multiplier (increases the interval by 50%)
   */
  public static final double DEFAULT_MULTIPLIER = 1.5;

  /**
   * the default maximum interval. Truncate time that exceeds 15 seconds.
   */
  public static final long DEFAULT_MAX_INTERVAL = 15_000L;

  /**
   * the default random range. choose randomly within the range of ± 20% of the interval value.
   */
  public static final double DEFAULT_RANGE = 0.2;

  /**
   * No dispersion of intervals.
   */
  public static final double NO_RANGE = 0.0;

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
      throw new IllegalArgumentException("Interval is invalid. Must be greater than 1.");
    }

    if (maxInterval < interval) {
      throw new IllegalArgumentException(
          "maxInterval is invalid. Must be greater or equal than Interval.");
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

  @Override public long interval(int retryCount, long elapsedTime) {
    long next = (long) Math.min(interval * pow(multiplier, retryCount - 1), maxInterval);

    // calculate random range interval
    if (range != NO_RANGE) {
      long low = (long) Math.max(next - (next * range), 1);
      long high = (long) Math.min(next + (next * range), maxInterval);
      long rnd = low + (int) (Math.random() * ((high - low) + 1));
      if (Backoff.TRACE) Backoff.trace(next, low, high);
      next = rnd;
    }

    return next;
  }
}
