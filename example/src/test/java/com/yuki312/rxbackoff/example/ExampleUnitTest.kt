package com.yuki312.rxbackoff.example

import com.yuki312.rxbackoff.RxBackoff
import io.reactivex.Observable
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ExampleUnitTest {
  @Test
  fun test() {
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
            RxBackoff(3, 2000)
                .doOnRetry { _, cnt -> println("retry $cnt") }
                .doOnGiveUp { println("give up") })
        .test()
        .run {
          awaitTerminalEvent()
          assertValueCount(1)
          assertNoErrors()
        }
  }

  private fun time(): String {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("mm:ss"))
  }
}
