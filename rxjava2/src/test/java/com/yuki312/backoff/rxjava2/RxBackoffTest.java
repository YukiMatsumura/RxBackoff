package com.yuki312.backoff.rxjava2;

import com.yuki312.backoff.core.Backoff;
import com.yuki312.backoff.core.FixedIntervalAlgorithm;
import com.yuki312.backoff.core.RandomIntervalAlgorithm;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RxBackoffTest {

  @Test
  public void fixed() {
    final AtomicInteger count = new AtomicInteger(0);
    final TestScheduler scheduler = new TestScheduler();
    final RxBackoff backoff = new RxBackoff(
        new Backoff.Builder()
            .setAlgorithm(new FixedIntervalAlgorithm())
            .setMaxRetryCount(5)
            .build(),
        scheduler);
    final TestObserver observer = Observable
        .fromCallable(new Callable<Integer>() {
          @Override public Integer call() throws Exception {
            throw new Exception("error " + count.incrementAndGet());
          }
        })
        .retryWhen(backoff.observable())
        .subscribeOn(scheduler)
        .test();

    scheduler.advanceTimeTo(90_0000L, TimeUnit.MILLISECONDS);
    scheduler.triggerActions();
    observer.awaitTerminalEvent(1000L, TimeUnit.MILLISECONDS);
    observer.assertError(Exception.class);
    assertThat(count.get()).isEqualTo(6); // try(1 time) + retry(5 times)
  }

  @Test
  public void random() {
    final AtomicInteger count = new AtomicInteger(0);
    final TestScheduler scheduler = new TestScheduler();
    final RxBackoff backoff = new RxBackoff(new Backoff.Builder()
        .setAlgorithm(new RandomIntervalAlgorithm(
            1,
            5000,
            1.0,
            1.0,
            5000))
        .setMaxRetryCount(3)
        .build(),
        scheduler);
    final TestObserver observer = Observable
        .fromCallable(new Callable<Integer>() {
          @Override public Integer call() throws Exception {
            throw new Exception("error " + count.incrementAndGet());
          }
        })
        .retryWhen(backoff.observable())
        .subscribeOn(scheduler)
        .test();

    scheduler.advanceTimeTo(15000L, TimeUnit.MILLISECONDS);
    scheduler.triggerActions();
    observer.awaitTerminalEvent(1000L, TimeUnit.MILLISECONDS);
    observer.assertError(Exception.class);
    assertThat(count.get()).isGreaterThanOrEqualTo(4); // try(1 time) + retry(n times)
  }
}
