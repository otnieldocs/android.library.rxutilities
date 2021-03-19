package com.otnieldocs

import android.Manifest
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.otnieldocs.rxutilities.filemanager.RxFileManager
import com.otnieldocs.rxutilities.permission.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_rx_file.*

class RxFileActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()

    private val rxFile = RxFileManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_file)

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

        btn_rx_select_file.setOnClickListener {
            val observable = rxFile.selectFile(this)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ result: Uri? ->
                    Log.d("RX_FILE_RESULT", "Result ${result != null}")
                }, {
                    // on error
                    Log.d("RX_FILE_RESULT", "Failed to select file")
                })

            disposable.add(observable)
        }

        btn_rx_take_picture.setOnClickListener {
            val observable = rxFile.takePicture(this)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ result: Uri? ->
                    Log.d("RX_FILE_RESULT", "Result ${result != null}")
                }, {
                    // on error
                    Log.d("RX_FILE_RESULT", "Failed to take picture $it")
                })

            disposable.add(observable)
        }
    }
}