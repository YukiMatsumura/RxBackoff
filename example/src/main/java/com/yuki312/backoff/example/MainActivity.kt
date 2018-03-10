package com.yuki312.backoff.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.yuki312.backoff.ktx.exponentialBackoff
import com.yuki312.backoff.rxjava2.RxBackoff
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Observable.error<Any>(Exception("sample"))
                .doOnError { Log.e("test", "error $it") }
                .retryWhen(RxBackoff.of(exponentialBackoff(), 5).observable())
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }
}
