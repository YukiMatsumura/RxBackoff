package com.yuki312.rxbackoff;

import android.support.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;

import static com.yuki312.rxbackoff.Backoff.ABORT;

public class RxBackoff implements Function<Observable<Throwable>, ObservableSource<?>> {

  @NonNull private final Backoff backoff;
  @NonNull private final Scheduler intervalScheduler;

  private Predicate<Throwable> filter = new Predicate<Throwable>() {
    @Override public boolean test(Throwable throwable) throws Exception {
      return true;
    }
  };

  private BiConsumer<Throwable, Integer> onRetry = new BiConsumer<Throwable, Integer>() {
    @Override public void accept(Throwable e, Integer retry) throws Exception {
      // no-op
    }
  };

  private Consumer<Throwable> onAbort = new Consumer<Throwable>() {
    @Override public void accept(Throwable throwable) throws Exception {
      // no-op
    }
  };

  public static RxBackoff exponential(int maxRetryCount) {
    return new RxBackoff(new Backoff.Builder()
        .setAlgorithm(new BinaryExponentialAlgorithm())
        .setMaxRetryCount(maxRetryCount)
        .build());
  }

  public static RxBackoff fixed(long interval, int maxRetryCount) {
    return new RxBackoff(new Backoff.Builder()
        .setAlgorithm(new FixedIntervalAlgorithm(interval, TimeUnit.MILLISECONDS))
        .setMaxRetryCount(maxRetryCount)
        .build());
  }

  public static RxBackoff of(BackoffAlgorithm algorithm, int maxRetryCount) {
    return new RxBackoff(new Backoff.Builder()
        .setAlgorithm(algorithm)
        .setMaxRetryCount(maxRetryCount)
        .build());
  }

  /**
   * Construct RxBackoff.
   *
   * @param backoff Backoff object with algorithm specified. You can build the back-off object
   * using the {@link Backoff.Builder}.
   */
  public RxBackoff(@NonNull Backoff backoff) {
    this(backoff, Schedulers.computation());
  }

  /**
   * Construct RxBackoff.
   *
   * @param backoff Backoff object with algorithm specified. You can build the back-off object
   * using the {@link Backoff.Builder}.
   * @param intervalScheduler Scheduler used in backoff interval
   */
  public RxBackoff(@NonNull Backoff backoff, @NonNull Scheduler intervalScheduler) {
    this.backoff = backoff;
    this.intervalScheduler = intervalScheduler;
  }

  @Override public ObservableSource apply(Observable<Throwable> attempts) throws Exception {
    return attempts.flatMap(new Function<Throwable, ObservableSource<?>>() {
      @Override public ObservableSource<?> apply(Throwable throwable) throws Exception {
        if (!filter.test(throwable)) {
          return Observable.error(throwable);
        }

        long interval = backoff.interval();
        if (interval != ABORT) {
          onRetry.accept(throwable, backoff.getRetryCount());
          return Observable.timer(interval, TimeUnit.MILLISECONDS, intervalScheduler);
        } else {
          onAbort.accept(throwable);
          return Observable.error(throwable);
        }
      }
    });
  }

  /**
   * Filters errors emitted by an ObservableSource by only emitting those that satisfy a specified
   * predicate.
   *
   * @param predicate a function that evaluates each error emitted by the source ObservableSource,
   * returning {@code true} if it passes the filter
   */
  public RxBackoff filter(@NonNull Predicate<Throwable> predicate) {
    this.filter = predicate;
    return this;
  }

  /**
   * Set callback function called every time before retry processing
   */
  public RxBackoff doOnRetry(@NonNull BiConsumer<Throwable, Integer> onRetry) {
    this.onRetry = onRetry;
    return this;
  }

  /**
   * Set callback function called when abort retry
   */
  public RxBackoff doOnAbort(@NonNull Consumer<Throwable> onAbort) {
    this.onAbort = onAbort;
    return this;
  }
}
