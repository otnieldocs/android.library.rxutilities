package com.otnieldocs.rxutilities.event

import android.view.View
import com.jakewharton.rxbinding4.view.clicks
import java.util.concurrent.TimeUnit

fun View.clickThrottle(windowDuration: Long = 500, event:()->Unit) {
    this.clicks().throttleFirst(windowDuration, TimeUnit.MILLISECONDS).subscribe {
        event.invoke()
    }
}

fun View.clickDebounce(timeout: Long = 500, event:()->Unit) {
    this.clicks().debounce(timeout, TimeUnit.MILLISECONDS).subscribe {
        event.invoke()
    }
}