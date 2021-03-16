package com.otnieldocs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.otnieldocs.rxutilities.event.clickDebounce
import com.otnieldocs.rxutilities.event.clickThrottle
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_normal_permission.setOnClickListener {
            startActivity(Intent(this, NormalPermissionActivity::class.java))
        }

        btn_rx_permission.setOnClickListener {
            startActivity(Intent(this, RxPermissionActivity::class.java))
        }

        btn_rx_throttle.clickThrottle {
            Log.d("EVENT_CLICK", "Clicked emitted at ${Date().time}")
        }

        btn_rx_file.setOnClickListener {
            startActivity(Intent(this, RxFileActivity::class.java))
        }
    }

}