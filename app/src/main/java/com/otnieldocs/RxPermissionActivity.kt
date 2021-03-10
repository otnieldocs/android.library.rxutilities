package com.otnieldocs

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.otnieldocs.rxutilities.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class RxPermissionActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_permission)

        val requestPermission = listOf(
            RxPermissionRequest(Manifest.permission.CAMERA),
            RxPermissionRequest(Manifest.permission.READ_CONTACTS)
        )

        val permission = RxPermission()
        val subscribed =
            permission.request(requestPermission, this)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        when (result) {
                            is Granted -> {
                                Log.d("RX_PERMISSION", "The result is ${result.data}")
                            }
                            is Denied -> {
                                Log.d(
                                    "RX_PERMISSION",
                                    "Permission denied ${result.exception.message}"
                                )
                            }
                            is Failed -> {
                                Log.d("RX_PERMISSION", "Some error occurred")
                            }
                        }
                    }, {
                        Log.d("RX_PERMISSION", "Throws ${it.message}")
                    })

        disposable.add(subscribed)
    }
}