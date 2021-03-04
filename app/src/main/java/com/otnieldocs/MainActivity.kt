package com.otnieldocs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

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
    }

}