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
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject

class RxPermission {


    fun singleRequest(
        request: RxPermissionRequest,
        activity: AppCompatActivity,
        bindRationaleDialog: (( doRequest: () -> Unit ) -> Unit)? = null
    ): Observable<RxResult<String>> {
        val fragmentManager = activity.supportFragmentManager
        val fragment = HeadlessFragment.newInstance(request, bindRationaleDialog)
        fragmentManager.beginTransaction().add(fragment, HeadlessFragment::class.java.simpleName)
            .commitNow()
        return fragment.getPublisher()
    }

    class HeadlessFragment : Fragment {
        private val requestedPermissions = mutableListOf<RxPermissionRequest>()
        private var bindRationaleDialog: (( doRequest: () -> Unit ) -> Unit)? = null

        @JvmOverloads
        constructor(
            request: RxPermissionRequest,
            bindRationaleDialog: (( doRequest: () -> Unit ) -> Unit)? = null
        ) {
            with(requestedPermissions) {
                clear()
                add(request)
            }

            this.bindRationaleDialog = bindRationaleDialog
        }

        @JvmOverloads
        constructor(
            requests: List<RxPermissionRequest>,
            bindRationaleDialog: (( doRequest: () -> Unit ) -> Unit)? = null
        ) {
            with(requestedPermissions) {
                clear()
                addAll(requests)
            }

            this.bindRationaleDialog = bindRationaleDialog
        }

        private val publisher = ReplaySubject.create<RxResult<String>>()

        private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    publisher.onNext(Success(GRANTED))
                } else {
                    publisher.onNext(Failed(RxPermissionException(DENIED)))
                }

                publisher.onComplete()
            }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            context?.let { singlePermission(it) }
        }

        fun getPublisher() = publisher

        private fun singlePermission(context: Context) {

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    singlePermissionSdk23Above(context)
                }
                else -> singlePermissionSdk23Below(context)
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun singlePermissionSdk23Above(context: Context) {
            when {
                requestedPermissions.isEmpty() -> publisher.onError(RxPermissionException("Invalid request"))

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

                    if (bindRationaleDialog == null) {
                        AlertDialog.Builder(context).apply {
                            setMessage(requestedPermissions.first().rationaleMessage)
                            setPositiveButton(
                                "Yes"
                            ) { _, _ -> launchSinglePermissionLauncher() }
                        }.show()
                    }
                    else {
                        bindRationaleDialog?.invoke { launchSinglePermissionLauncher() }
                    }
                }

                else -> {
                    launchSinglePermissionLauncher()
                }
            }
        }

        private fun singlePermissionSdk23Below(context: Context) {
            when {
                requestedPermissions.isEmpty() -> publisher.onError(RxPermissionException("Invalid request"))

                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    with(publisher) {
                        timeInterval()
                        onNext(Success(GRANTED))
                        onComplete()
                    }
                }

                else -> {
                    launchSinglePermissionLauncher()
                }
            }
        }

        private fun launchSinglePermissionLauncher() {
            requestPermissionLauncher.launch(
                requestedPermissions.first().permission
            )
        }

        companion object {
            const val GRANTED = "GRANTED"
            const val DENIED = "DENIED"

            @JvmStatic
            fun newInstance(request: RxPermissionRequest, bindRationaleDialog: (( doRequest: () -> Unit ) -> Unit)? = null): HeadlessFragment =
                HeadlessFragment(request, bindRationaleDialog)

            @JvmStatic
            fun newInstance(requests: List<RxPermissionRequest>, bindRationaleDialog: (( doRequest: () -> Unit ) -> Unit)? = null): HeadlessFragment =
                HeadlessFragment(requests, bindRationaleDialog)
        }
    }
}