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
            RxPermissionRequest(
                permission = Manifest.permission.CAMERA,
                rationaleMessage = "You need to allow camera permission to use this feature"
            ),
            RxPermissionRequest(
                permission = Manifest.permission.READ_CONTACTS,
                rationaleMessage = "You need to allow camera permission to use this feature"
            )
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
                            is Rationale -> {
                                Log.d("RX_PERMISSION", "You need to turn on ${result.data} permission")
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