package com.yuki312.backoff.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.yuki312.backoff.rxjava2.RxBackoff
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Observable.error<Any>(Exception("sample"))
                .doOnError { android.util.Log.e("test", "error $it") }
                .retryWhen(RxBackoff.exponential(2.0, 3))
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }
}
