package com.yuki312.rxbackoff;

import android.support.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.concurrent.TimeUnit;

public class RxBackoff implements Function<Observable<Throwable>, ObservableSource<?>> {

  private final int maxRetry;
  private final Function<Integer, Long> delayFunction;

  private int retryCount = 0;

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

  private Consumer<Throwable> onGiveUp = new Consumer<Throwable>() {
    @Override public void accept(Throwable throwable) throws Exception {
      // no-op
    }
  };

  public RxBackoff(int maxRetry, final long delayMs) {
    this(maxRetry, new Function<Integer, Long>() {
      @Override public Long apply(Integer integer) throws Exception {
        return delayMs;
      }
    });
  }

  public RxBackoff(int maxRetry, Function<Integer, Long> delayFunction) {
    this.maxRetry = maxRetry;
    this.delayFunction = delayFunction;
  }

  @Override public ObservableSource apply(Observable<Throwable> attempts) throws Exception {
    return attempts.flatMap(new Function<Throwable, ObservableSource<?>>() {
      @Override public ObservableSource<?> apply(Throwable throwable) throws Exception {
        if (!filter.test(throwable)) {
          return Observable.error(throwable);
        }

        if (++retryCount <= maxRetry) {
          onRetry.accept(throwable, retryCount);
          return Observable.timer(delayFunction.apply(retryCount), TimeUnit.MILLISECONDS);
        } else {
          onGiveUp.accept(throwable);
          return Observable.error(throwable);
        }
      }
    });
  }

  public RxBackoff filter(@NonNull Predicate<Throwable> predicate) {
    this.filter = predicate;
    return this;
  }

  public RxBackoff doOnRetry(@NonNull BiConsumer<Throwable, Integer> onRetry) {
    this.onRetry = onRetry;
    return this;
  }

  public RxBackoff doOnGiveUp(@NonNull Consumer<Throwable> onGiveUp) {
    this.onGiveUp = onGiveUp;
    return this;
  }
}
