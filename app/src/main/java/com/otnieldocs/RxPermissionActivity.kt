package com.otnieldocs

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.otnieldocs.rxutilities.Loading
import com.otnieldocs.rxutilities.RxPermission
import com.otnieldocs.rxutilities.RxPermissionRequest
import com.otnieldocs.rxutilities.Success
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class RxPermissionActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_permission)

        val requestPermission = RxPermissionRequest(
            permission = Manifest.permission.CAMERA,
            rationaleMessage = "You need to allow camera permission to use this feature"
        )

        val permission = RxPermission()
        val subscribed =
            permission.singleRequest(requestPermission, this)
                .subscribeOn(Schedulers.io())
                .subscribe { result ->
                    when(result) {
                        is Loading -> {
                            Log.d("RX_PERMISSION", "Requesting permission...")
                        }
                        is Success -> {
                            Log.d("RX_PERMISSION", "The result is ${result.data}")
                        }
                        else -> {
                            Log.d("RX_PERMISSION", "Permission denied")
                        }
                    }
                }

        disposable.add(subscribed)
    }
}