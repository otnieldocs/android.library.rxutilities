package com.otnieldocs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class NormalPermissionActivity : AppCompatActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Access for this permission is granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this,
                    "You cannot use the feature that related to camera",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regular_permission)

        singlePermission()
    }

    private fun singlePermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                singlePermissionSdk23Above()
            }
            else -> singlePermissionSdk23Below()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun singlePermissionSdk23Above() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // use the api directly.
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // show yes no dialog. If user choose yes, then call requestPermissionLauncher.launch(permission)
                // otherwise, show to the user popup info that explain the effect to the apps by rejecting them
                AlertDialog.Builder(this).apply {
                    setMessage("You need to allow camera permission to use this feature")
                    setPositiveButton(
                        "Yes"
                    ) { _, _ -> requestCamera() }
                }.show()
            }
            else -> {
                requestCamera()
            }
        }
    }

    private fun singlePermissionSdk23Below() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {
                // use the api directly.
            }
            else -> {
                requestCamera()
            }
        }
    }

    private fun requestCamera() {
        requestPermissionLauncher.launch(
            Manifest.permission.CAMERA
        )
    }
}