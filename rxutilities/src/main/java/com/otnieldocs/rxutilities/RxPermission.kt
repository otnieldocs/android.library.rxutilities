package com.otnieldocs.rxutilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
                    else publisher.onNext(Denied(RxPermissionException(DENIED)))
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

            context?.let { recheckPermission(it) }
        }

        fun getPublisher() = publisher

        private fun recheckPermission(context: Context) {

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    recheckPermissionSdk23Above(context)
                }
                else -> recheckPermissionSdk23Below(context)
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun recheckPermissionSdk23Above(context: Context) {
            val shouldRequested = mutableListOf<RxPermissionRequest>()
            for (request in requestedPermissions) {
                when {
                    requestedPermissions.isEmpty() -> publisher.onError(RxPermissionException(INVALID))

                    ContextCompat.checkSelfPermission(
                        context,
                        request.permission
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        publisher.onNext(Success(request.permission))
                    }

                    shouldShowRequestPermissionRationale(request.permission) -> {
                        // show yes no dialog. If user choose yes, then call requestPermissionLauncher.launch(permission)
                        // otherwise, show to the user popup info that explain the effect to the apps by rejecting them

//                        AlertDialog.Builder(context).apply {
//                            setMessage(requestedPermissions.first().rationaleMessage)
//                            setPositiveButton(
//                                "Yes"
//                            ) { _, _ -> launchPermissionLauncher() }
//                        }.show()
                        publisher.onNext(Rationale(request.permission))
                    }

                    else -> {
                        shouldRequested.add(request)
                    }
                }
            }

            publisher.onComplete()

            if (shouldRequested.isNotEmpty()) {
                launchPermissionLauncher(shouldRequested)
            }
        }

        private fun recheckPermissionSdk23Below(context: Context) {
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
                    launchPermissionLauncher(requestedPermissions)
                }
            }
        }

        private fun launchPermissionLauncher(requests: List<RxPermissionRequest>) {
            requestPermissionLauncher.launch(
                requests.map {
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