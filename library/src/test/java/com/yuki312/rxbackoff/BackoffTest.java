package com.yuki312.rxbackoff;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.yuki312.rxbackoff.Backoff.DEFAULT_INTERVAL;
import static com.yuki312.rxbackoff.Backoff.DEFAULT_MULTIPLIER;
import static com.yuki312.rxbackoff.Backoff.NO_RANGE;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class BackoffTest {

  @Test
  public void exponentialBackoff_Default() {
    /*
     * Test Default behavior:
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
    final long interval = 1000L;
    Backoff backoff = new Backoff.Builder()
        .setAlgorithm(new ExponentialAlgorithm())
        .setMaxElapsedTime(Long.MAX_VALUE)
        .setMaxRetryCount(Integer.MAX_VALUE)
        .build();
    for (int i = 1; i < 12; i++) {
      long t = (long) (interval * Math.pow(2, i - 1));
      long min = (long) (t - (t * DEFAULT_MULTIPLIER));
      long max = (long) (t + (t * DEFAULT_MULTIPLIER));
      assertThat(backoff.interval()).isBetween(min, max);
    }
  }

  @Test
  public void binaryExponentialBackoff_Default() {
    /*
     * Test Default behavior:
     *
     * | Interval | Random range   | *Default random range ±20%
     * | -------- | -------------- |
     * | 1000     | (800..1200)    | *Default interval 500ms
     * | 2000     | (1600..2400)   | *Default multiplier x1.5
     * | 4000     | (3200..4800)   |
     * | 8000     | (6400..9600)   |
     * | 15000    | (12000..18000) |
     */
    Backoff backoff = new Backoff.Builder()
        .setAlgorithm(new BinaryExponentialAlgorithm())
        .setMaxElapsedTime(Long.MAX_VALUE)
        .setMaxRetryCount(Integer.MAX_VALUE)
        .build();
    for (int i = 1; i < 12; i++) {
      long t = (long) (DEFAULT_INTERVAL * Math.pow(2, i - 1));
      long min = (long) (t - (t * DEFAULT_MULTIPLIER));
      long max = (long) (t + (t * DEFAULT_MULTIPLIER));
      assertThat(backoff.interval()).isBetween(min, max);
    }
  }

  @Test
  public void binaryExponentialBackoff_NoRange() {
    /*
     * Test no range behavior:
     *
     * | Interval |
     * | -------- |
     * | 1000     |
     * | 2000     |
     * | 4000     |
     * | 8000     |
     * | 15000    |
     */
    Backoff backoff = new Backoff.Builder()
        .setAlgorithm(new BinaryExponentialAlgorithm(DEFAULT_INTERVAL, Long.MAX_VALUE, NO_RANGE))
        .setMaxElapsedTime(Long.MAX_VALUE)
        .setMaxRetryCount(Integer.MAX_VALUE)
        .build();
    for (int i = 1; i < 12; i++) {
      long t = (long) (DEFAULT_INTERVAL * Math.pow(2, i - 1));
      assertThat(backoff.interval()).isEqualTo(t);
    }
  }

  @Test
  public void fixedDelayBackoff_default() {
    /*
     * | Interval |
     * | -------- |
     * | 500      |
     * | 500      |
     * | ...      |
     */
    Backoff backoff = new Backoff.Builder()
        .setAlgorithm(new FixedIntervalAlgorithm())
        .setMaxElapsedTime(Long.MAX_VALUE)
        .setMaxRetryCount(Integer.MAX_VALUE)
        .build();
    for (int i = 1; i < 12; i++) {
      assertThat(backoff.interval()).isEqualTo(DEFAULT_INTERVAL);
    }
  }
}
