package com.otnieldocs

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.otnieldocs.rxutilities.filemanager.RxFileManager
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_rx_file.*

class RxFileActivity : AppCompatActivity() {
    private val rxFile = RxFileManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_file)

        btn_rx_select_file.setOnClickListener {
            rxFile.selectFile("image/*", this)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { result: Uri? ->
                    Log.d("RX_FILE_RESULT", "Result ${result == null}")
                }
        }
    }
}