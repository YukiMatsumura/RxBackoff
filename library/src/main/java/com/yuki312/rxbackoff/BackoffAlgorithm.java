package com.yuki312.rxbackoff;

import android.support.annotation.IntRange;

public interface BackoffAlgorithm {
  /**
   * Calculate the next interval time.
   *
   * interval is must be greater or equals 1.
   * When ABORT(0) is returned, the retry process is stopped.
   *
   * @param retryCount Current retry count
   * @param elapsedTime Current elapsed time in milliseconds
   * @return next interval time or ABORT
   * @see Backoff#ABORT
   */
  @IntRange(from = 0L) long interval(int retryCount, long elapsedTime);
}
