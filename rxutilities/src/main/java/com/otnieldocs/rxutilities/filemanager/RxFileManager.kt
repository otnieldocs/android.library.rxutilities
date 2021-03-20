package com.otnieldocs.rxutilities.filemanager

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.otnieldocs.rxutilities.filemanager.RxFileManager.HeadlessFragment.Companion.MIME_CAMERA
import com.otnieldocs.rxutilities.filemanager.RxFileManager.HeadlessFragment.Companion.MIME_IMAGE
import com.otnieldocs.rxutilities.filemanager.RxFileManager.HeadlessFragment.Companion.REQ_SELECT_FILE
import com.otnieldocs.rxutilities.filemanager.RxFileManager.HeadlessFragment.Companion.REQ_TAKE_PICTURE
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RxFileManager {
    fun selectFile(activity: AppCompatActivity): Observable<Uri> {
        val fragment = buildFragment(activity, REQ_SELECT_FILE, MIME_IMAGE)
        return fragment.getUriPublisher()
    }

    fun takePicture(activity: AppCompatActivity): Observable<Uri> {
        val fragment = buildFragment(activity, REQ_TAKE_PICTURE, MIME_CAMERA)
        return fragment.getUriPublisher()
    }

    private fun buildFragment(
        activity: AppCompatActivity,
        requestType: String, mimeType: String = ""
    ): HeadlessFragment {
        val fragmentManager = activity.supportFragmentManager
        val fragment = HeadlessFragment.newInstance(requestType, mimeType)
        fragmentManager.beginTransaction().add(fragment, HeadlessFragment::class.java.simpleName)
            .commitNow()

        return fragment
    }

    class HeadlessFragment : Fragment() {
        private val uriPublisher = ReplaySubject.create<Uri>()

        private val requestType: String by lazy {
            arguments?.getString(REQUEST_TYPE) ?: ""
        }

        private val mimeType: String by lazy {
            arguments?.getString(MIME_TYPE) ?: ""
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            when (requestType) {
                REQ_SELECT_FILE -> launchFileLauncher(mimeType)
                REQ_TAKE_PICTURE -> context?.let { launchCameraLauncher(it) }
                else -> {
                }
            }
        }

        private fun launchFileLauncher(mimeType: String) {
            val launcher =
                registerForActivityResult(ActivityResultContracts.GetContent()) { result: Uri? ->
                    result?.let {
                        uriPublisher.onNext(it)
                    }

                    uriPublisher.onComplete()
                }
            launcher.launch(mimeType)
        }

        private fun launchCameraLauncher(context: Context) {
            try {
                val uri = createContentUri(context)
                val launcher =
                    registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                        if (isSuccess) {
                            uri?.let { uriPublisher.onNext(it) }
                        } else {
                            uriPublisher.onError(Exception("Failed to take picture"))
                        }

                        uriPublisher.onComplete()
                    }

                launcher.launch(uri)
            } catch (e: IOException) {
                with(uriPublisher) {
                    onError(e)
                    onComplete()
                }
            }
        }

        private fun createContentUri(context: Context): Uri? {
            val timeStamp: String =
                SimpleDateFormat(TIMESTAMP_PATTERN, Locale.getDefault()).format(Date())

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "pict-$timeStamp")
                put(MediaStore.MediaColumns.MIME_TYPE, MIME_IMAGE)
            }
            val resolver = context.contentResolver
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            return resolver.insert(contentUri, contentValues)
        }

        fun getUriPublisher() = uriPublisher

        companion object {
            const val REQUEST_TYPE = "request_type"
            const val MIME_TYPE = "mime_type"
            const val REQ_TAKE_PICTURE = "take_picture"
            const val REQ_SELECT_FILE = "select_file"
            const val MIME_IMAGE = "image/*"
            const val MIME_CAMERA = "image/jpg"
            const val TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss"

            @JvmStatic
            fun newInstance(type: String, mimeType: String = "") = HeadlessFragment().apply {
                arguments = Bundle().apply {
                    putString(REQUEST_TYPE, type)
                    putString(MIME_TYPE, mimeType)
                }
            }
        }
    }
}