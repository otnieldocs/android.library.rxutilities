package com.otnieldocs.rxutilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

class RxPermission {

    fun request(
        requests: List<RxPermissionRequest>,
        activity: AppCompatActivity
    ): Observable<RxResult<String>> {
        val fragmentManager = activity.supportFragmentManager
        val fragment = HeadlessFragment.newInstance(requests)
        fragmentManager.beginTransaction().add(fragment, HeadlessFragment::class.java.simpleName)
            .commitNow()
        return fragment.getPublisher()
    }

    class HeadlessFragment(requests: List<RxPermissionRequest>) : Fragment() {
        private val requestedPermissions = mutableListOf<RxPermissionRequest>()

        private val publisher = ReplaySubject.create<RxResult<String>>()

        private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
                for (request in requestedPermissions) {
                    val isGranted = results[request.permission] ?: false

                    if (isGranted) publisher.onNext(Success(GRANTED))
                    else publisher.onNext(Failed(RxPermissionException(DENIED)))
                }

                publisher.onComplete()
            }

        init {
            with(requestedPermissions) {
                clear()
                addAll(requests)
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            context?.let { permission(it) }
        }

        fun getPublisher() = publisher

        private fun permission(context: Context) {

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    permissionSdk23Above(context)
                }
                else -> permissionSdk23Below(context)
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun permissionSdk23Above(context: Context) {
            when {
                requestedPermissions.isEmpty() -> publisher.onError(RxPermissionException(INVALID))

                ContextCompat.checkSelfPermission(
                    context,
                    requestedPermissions.first().permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    with(publisher) {
                        onNext(Success(GRANTED))
                        onComplete()
                    }
                }

                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    // show yes no dialog. If user choose yes, then call requestPermissionLauncher.launch(permission)
                    // otherwise, show to the user popup info that explain the effect to the apps by rejecting them

                    AlertDialog.Builder(context).apply {
                        setMessage(requestedPermissions.first().rationaleMessage)
                        setPositiveButton(
                            "Yes"
                        ) { _, _ -> launchPermissionLauncher() }
                    }.show()
                }

                else -> {
                    launchPermissionLauncher()
                }
            }
        }

        private fun permissionSdk23Below(context: Context) {
            when {
                requestedPermissions.isEmpty() -> publisher.onError(RxPermissionException(INVALID))

                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    with(publisher) {
                        onNext(Success(GRANTED))
                        onComplete()
                    }
                }

                else -> {
                    launchPermissionLauncher()
                }
            }
        }

        private fun launchPermissionLauncher() {
            requestPermissionLauncher.launch(
                requestedPermissions.map {
                    it.permission
                }.toTypedArray()
            )
        }

        companion object {
            const val GRANTED = "GRANTED"
            const val DENIED = "DENIED"
            const val INVALID = "INVALID"

            @JvmStatic
            fun newInstance(
                requests: List<RxPermissionRequest>
            ): HeadlessFragment =
                HeadlessFragment(requests)
        }
    }
}