package com.yuki312.backoff.core;

import static java.lang.Math.pow;

/**
 * Default random backoff interval:
 *
 * | Random range |
 * | ------------ |
 * | (500..1000)  | *Default low:500ms, high:1000ms
 * | (500..3000)  | *Default multiplier low:x1.0, high:x3.0
 * | (500..9000)  |
 * | (500..15000) | *Default max interval 15,000ms
 * | (500..15000) |
 * | ...          |
 */
public class RandomIntervalAlgorithm implements BackoffAlgorithm {

  /**
   * the default lower interval
   */
  public static final long DEFAULT_LOW_INTERVAL = 500L;

  /**
   * the default high interval
   */
  public static final long DEFAULT_HIGH_INTERVAL = 1000L;

  /**
   * the default multiplier (no increases the interval)
   */
  public static final double DEFAULT_LOW_MULTIPLIER = 1.0;

  /**
   * the default multiplier (no increases the interval)
   */
  public static final double DEFAULT_HIGH_MULTIPLIER = 3.0;

  /**
   * the default maximum interval. Truncate time that exceeds 15 seconds.
   */
  public static final long DEFAULT_MAX_INTERVAL = 15_000L;

  private final long lowInterval;
  private final long highInterval;
  private final double lowMultiplier;
  private final double highMultiplier;
  private final long maxInterval;

  public RandomIntervalAlgorithm() {
    this(DEFAULT_LOW_INTERVAL, DEFAULT_HIGH_INTERVAL,
        DEFAULT_LOW_MULTIPLIER, DEFAULT_HIGH_MULTIPLIER,
        DEFAULT_MAX_INTERVAL);
  }

  public RandomIntervalAlgorithm(long lowInterval, long highInterval,
      double lowMultiplier, double highMultiplier, long maxInterval) {
    if (lowInterval < 1L) {
      throw new IllegalArgumentException("lowInterval is invalid. Must be greater than 1.");
    }

    if (highInterval < 1L) {
      throw new IllegalArgumentException("highInterval is invalid. Must be greater than 1.");
    }

    if (highInterval <= lowInterval) {
      throw new IllegalArgumentException(
          "highInterval is invalid. Must be greater than lowInterval.");
    }

    if (maxInterval < highInterval) {
      throw new IllegalArgumentException(
          "maxInterval is invalid. Must be greater or equal than highInterval.");
    }

    if (lowMultiplier < 1.0) {
      throw new IllegalArgumentException("LowMultiplier is invalid. Must be greater than 1.0.");
    }

    if (highMultiplier < 1.0) {
      throw new IllegalArgumentException("HighMultiplier is invalid. Must be greater than 1.0.");
    }

    this.lowInterval = lowInterval;
    this.highInterval = highInterval;
    this.lowMultiplier = lowMultiplier;
    this.highMultiplier = highMultiplier;
    this.maxInterval = maxInterval;
  }

  @Override public long interval(int retryCount, long elapsedTime) {
    long low = (long) Math.max(lowInterval * pow(lowMultiplier, retryCount - 1), 1);
    long high = (long) Math.min(highInterval * pow(highMultiplier, retryCount - 1), maxInterval);

    // calculate random range interval
    long rnd = low + (int) (Math.random() * ((high - low) + 1));
    if (Backoff.TRACE) Backoff.trace(rnd, low, high);

    return rnd;
  }
}
