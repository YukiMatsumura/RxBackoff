package com.yuki312.rxbackoff;

/**
 * Default binary exponential backoff interval:
 *
 * | Interval | Random range   | *Default random range ±20%
 * | -------- | -------------- |
 * | 500      | (400, 600)     | *Default interval 500ms
 * | 1000     | (800, 1200)    | *Default multiplier x2.0
 * | 2000     | (1600, 2400)   |
 * | 4000     | (3200, 4800)   |
 * | 8000     | (6400, 9600)   |
 * | 15000    | (12000, 15000) | *Default max interval 15,000ms
 * | 15000    | (12000..15000) |
 * | ...      | ...            |
 */
public class BinaryExponentialAlgorithm extends ExponentialAlgorithm {

  /**
   * Construct Binary exponential algorithm.
   *
   * @see #DEFAULT_INTERVAL
   * @see #DEFAULT_MAX_INTERVAL
   * @see #DEFAULT_RANGE
   */
  public BinaryExponentialAlgorithm() {
    super(DEFAULT_INTERVAL, 2.0, DEFAULT_MAX_INTERVAL, DEFAULT_RANGE);
  }

  /**
   * Construct Binary exponential algorithm.
   *
   * @param interval Wait time milliseconds until next retry
   * @see #DEFAULT_MAX_INTERVAL
   * @see #DEFAULT_RANGE
   */
  public BinaryExponentialAlgorithm(long interval) {
    super(interval, 2.0, DEFAULT_MAX_INTERVAL, DEFAULT_RANGE);
  }

  /**
   * Construct Binary exponential algorithm.
   *
   * @param interval Wait time milliseconds until next retry
   * @param maxInterval the maximum interval. Truncate time that exceeds
   * @see #DEFAULT_RANGE
   */
  public BinaryExponentialAlgorithm(long interval, long maxInterval) {
    super(interval, 2.0, maxInterval, DEFAULT_RANGE);
  }

  /**
   * Construct Binary exponential algorithm.
   *
   * @param interval Wait time milliseconds until next retry
   * @param maxInterval the maximum interval. Truncate time that exceeds
   * @param range Intervals are randomly chosen within this range. For example, if 0.2 is specified
   * for range, the interval is selected within the range of ± 20%.
   */
  public BinaryExponentialAlgorithm(long interval, long maxInterval, double range) {
    super(interval, 2.0, maxInterval, range);
  }
}
