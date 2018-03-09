package com.yuki312.backoff;

import com.yuki312.backoff.core.Backoff;
import com.yuki312.backoff.core.BinaryExponentialAlgorithm;
import com.yuki312.backoff.core.ExponentialAlgorithm;
import com.yuki312.backoff.core.FixedIntervalAlgorithm;
import com.yuki312.backoff.core.RandomIntervalAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.yuki312.backoff.core.ExponentialAlgorithm.DEFAULT_INTERVAL;
import static com.yuki312.backoff.core.ExponentialAlgorithm.DEFAULT_MULTIPLIER;
import static com.yuki312.backoff.core.ExponentialAlgorithm.NO_RANGE;
import static com.yuki312.backoff.core.RandomIntervalAlgorithm.DEFAULT_HIGH_INTERVAL;
import static com.yuki312.backoff.core.RandomIntervalAlgorithm.DEFAULT_HIGH_MULTIPLIER;
import static com.yuki312.backoff.core.RandomIntervalAlgorithm.DEFAULT_LOW_INTERVAL;
import static com.yuki312.backoff.core.RandomIntervalAlgorithm.DEFAULT_LOW_MULTIPLIER;
import static java.lang.Math.pow;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class BackoffTest {

  @Test
  public void exponential_Default() {
    /*
     * Test Default behavior:
     *
     * | Interval | Random range   |
     * | -------- | -------------- |
     * | 500      | (400..600)     |
     * | 750      | (600..900)     |
     * | 1125     | (900..1350)    |
     * | 1687     | (1349..2024)   |
     * | 2531     | (2024..3037)   |
     * | 3796     | (3036..4555)   |
     * | 5695     | (4556..6834)   |
     * | 8542     | (6833..10250)  |
     * | 12814    | (10251..15376) |
     * | 15000    | (12000..18000) |
     * | 15000    | (12000..18000) |
     * | ...      | ...            |
     */
    final long interval = 1000L;
    Backoff backoff = new Backoff.Builder()
        .setAlgorithm(new ExponentialAlgorithm())
        .setUnlimitedElapsedTime()
        .setUnlimitedRetryCount()
        .build();
    for (int i = 1; i < 12; i++) {
      long t = (long) (interval * pow(2, i - 1));
      long min = (long) (t - (t * DEFAULT_MULTIPLIER));
      long max = (long) (t + (t * DEFAULT_MULTIPLIER));
      assertThat(backoff.interval()).isBetween(min, max);
    }
  }

  @Test
  public void binaryExponential_Default() {
    /*
     * Test Default behavior:
     *
     * | Interval | Random range   |
     * | -------- | -------------- |
     * | 1000     | (800..1200)    |
     * | 2000     | (1600..2400)   |
     * | 4000     | (3200..4800)   |
     * | 8000     | (6400..9600)   |
     * | 15000    | (12000..18000) |
     * | ...      | ...            |
     */
    Backoff backoff = new Backoff.Builder()
        .setAlgorithm(new BinaryExponentialAlgorithm())
        .setUnlimitedElapsedTime()
        .setUnlimitedRetryCount()
        .build();
    for (int i = 1; i < 12; i++) {
      long t = (long) (DEFAULT_INTERVAL * pow(2, i - 1));
      long min = (long) (t - (t * DEFAULT_MULTIPLIER));
      long max = (long) (t + (t * DEFAULT_MULTIPLIER));
      assertThat(backoff.interval()).isBetween(min, max);
    }
  }

  @Test
  public void binaryExponential_NoRange() {
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
     * | ...      |
     */
    Backoff backoff = new Backoff.Builder()
        .setAlgorithm(new BinaryExponentialAlgorithm(DEFAULT_INTERVAL, Long.MAX_VALUE, NO_RANGE))
        .setUnlimitedElapsedTime()
        .setUnlimitedRetryCount()
        .build();
    for (int i = 1; i < 12; i++) {
      long t = (long) (DEFAULT_INTERVAL * pow(2, i - 1));
      assertThat(backoff.interval()).isEqualTo(t);
    }
  }

  @Test
  public void fixedInterval_default() {
    /*
     * | Interval |
     * | -------- |
     * | 500      |
     * | 500      |
     * | ...      |
     */
    Backoff backoff = new Backoff.Builder()
        .setAlgorithm(new FixedIntervalAlgorithm())
        .setUnlimitedElapsedTime()
        .setUnlimitedRetryCount()
        .build();
    for (int i = 1; i < 12; i++) {
      assertThat(backoff.interval()).isEqualTo(DEFAULT_INTERVAL);
    }
  }

  @Test
  public void randomInterval_default() {
    /*
     * | Random range |
     * | ------------ |
     * | (500..1000)  |
     * | (500..3000)  |
     * | (500..9000)  |
     * | (500..15000) |
     * | (500..15000) |
     * | ...          |
     */
    Backoff backoff = new Backoff.Builder()
        .setAlgorithm(new RandomIntervalAlgorithm())
        .setUnlimitedElapsedTime()
        .setUnlimitedRetryCount()
        .build();
    for (int i = 1; i < 12; i++) {
      long low = (long) (DEFAULT_LOW_INTERVAL * pow(DEFAULT_LOW_MULTIPLIER, i - 1));
      long high = (long) (DEFAULT_HIGH_INTERVAL * pow(DEFAULT_HIGH_MULTIPLIER, i - 1));
      assertThat(backoff.interval()).isBetween(low, high);
    }
  }
}
