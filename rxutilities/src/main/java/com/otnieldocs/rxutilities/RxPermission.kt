package com.otnieldocs.rxutilities

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
    ): Observable<RxResult<List<String>>> {
        val fragmentManager = activity.supportFragmentManager
        val fragment = HeadlessFragment.newInstance(requests)
        fragmentManager.beginTransaction().add(fragment, HeadlessFragment::class.java.simpleName)
            .commitNow()
        return fragment.getPublisher()
    }

    class HeadlessFragment(requests: List<RxPermissionRequest>) : Fragment() {
        private val requestedPermissions = mutableListOf<RxPermissionRequest>()
        private val requestRationale = mutableListOf<RxPermissionRequest>()
        private val permissionSuccess = mutableListOf<String>()
        private val permissionDenied = mutableListOf<String>()

        private val publisher = ReplaySubject.create<RxResult<List<String>>>()

        private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
                for (request in requestedPermissions) {
                    val isGranted = results[request.permission] ?: false

                    if (isGranted) permissionSuccess.add(request.permission)
                    else permissionDenied.add(request.permission)
                }

                if (permissionSuccess.isNotEmpty()) {
                    publisher.onNext(Granted(permissionSuccess))
                }

                if (permissionDenied.isNotEmpty()) {
                    publisher.onNext(Denied(RxPermissionException(DENIED, permissionDenied)))
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
                    recheckPermissionSdk23Above(context, requestedPermissions)
                }
                else -> recheckPermissionSdk23Below(context)
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun recheckPermissionSdk23Above(context: Context, requests: List<RxPermissionRequest>) {
            permissionSuccess.clear()
            requestRationale.clear()

            val shouldRequested = mutableListOf<RxPermissionRequest>()
            for (request in requests) {
                when {
                    requests.isEmpty() -> publisher.onError(RxPermissionException(INVALID))

                    ContextCompat.checkSelfPermission(
                        context,
                        request.permission
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        permissionSuccess.add(request.permission)
                    }

                    shouldShowRequestPermissionRationale(request.permission) -> {
                        requestRationale.add(request)
                    }

                    else -> {
                        shouldRequested.add(request)
                    }
                }
            }

            if (permissionSuccess.isNotEmpty()) {
                publisher.onNext(Granted(permissionSuccess))
                publisher.onComplete()
            }

            if (requestRationale.isNotEmpty()) {
                val rationaleMessage = getRationaleMessage(requestRationale)

                AlertDialog.Builder(context).apply {
                    setMessage(getString(R.string.rxutilities_permission_text_rationale, rationaleMessage))
                    setPositiveButton(
                        getString(R.string.rxutilities_permission_action_yes)
                    ) { _, _ ->
                        launchPermissionLauncher(requestedPermissions)
                    }
                }.show()
            }

            if (shouldRequested.isNotEmpty()) {
                launchPermissionLauncher(shouldRequested)
            }
        }

        private fun getRationaleMessage(rationaleRequests: List<RxPermissionRequest>): String {
            var message = ""

            for (rationale in rationaleRequests) {
                message = message.plus("${rationale.permission}\n")
            }

            return message
        }

        private fun recheckPermissionSdk23Below(context: Context) {
            val shouldRequested = mutableListOf<RxPermissionRequest>()
            for (request in requestedPermissions) {
                when {
                    requestedPermissions.isEmpty() -> publisher.onError(RxPermissionException(INVALID))

                    ContextCompat.checkSelfPermission(
                        context,
                        request.permission
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        permissionSuccess.add(request.permission)
                    }

                    else -> {
                        shouldRequested.add(request)
                    }
                }
            }

            if (permissionSuccess.isNotEmpty()) {
                publisher.onNext(Granted(permissionSuccess))
            }

            publisher.onComplete()

            if (shouldRequested.isNotEmpty()) {
                launchPermissionLauncher(shouldRequested)
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