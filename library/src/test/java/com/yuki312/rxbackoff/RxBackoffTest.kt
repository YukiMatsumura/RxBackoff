package com.yuki312.rxbackoff.example

import com.yuki312.rxbackoff.RxBackoff
import io.reactivex.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

@RunWith(JUnit4.class)
class RxBackoffTest {
  @Test
  fun fixedInterval() {
    var i = 0
    Observable.fromCallable {
      ++i
      println("call ${time()} $i")
      when (i) {
        in 1..3 -> throw Exception("error $i")
        else -> i
      }
    }
        .retryWhen(
            RxBackoff(3, 2000L)
                .doOnRetry { _, cnt -> println("retry $cnt") }
                .doOnGiveUp { println("give up") })
        .test()
        .run {
          awaitTerminalEvent()
          assertValueCount(1)
          assertNoErrors()
        }
  }

  @Test
  fun exponential() {
    var i = 0
    Observable.fromCallable {
      ++i
      println("call ${time()} $i")
      when (i) {
        in 1..4 -> throw Exception("error $i")
        else -> i
      }
    }
        .retryWhen(
            RxBackoff(5, { retry ->
              val e = retry.coerceAtMost(5)

              2F.pow(retry - 1).toLong().times(1000L).coerceAtMost(5000L)
            })
                .doOnRetry { _, cnt -> println("retry $cnt") }
                .doOnGiveUp { println("give up") })
        .test()
        .run {
          awaitTerminalEvent()
          assertValueCount(1)
          assertNoErrors()
        }
  }

  @Test
  fun filter() {
    var i = 0
    Observable.fromCallable {
      ++i
      println("call ${time()} $i")
      when (i) {
        in 1..2 -> throw IOException("io error $i")
        3 -> throw RuntimeException("runtime error $i")
        else -> i
      }
    }
        .retryWhen(
            RxBackoff(3, 1000L)
                .filter { it is IOException }
                .doOnRetry { _, cnt -> println("retry $cnt") })
        .test()
        .run {
          awaitTerminalEvent()
          assertNoValues()
          assertError { it is RuntimeException }
        }
  }

  private fun time(): String {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("mm:ss"))
  }
}
