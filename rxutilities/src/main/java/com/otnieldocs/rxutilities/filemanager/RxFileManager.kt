package com.otnieldocs.rxutilities.filemanager

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

class RxFileManager {
    fun selectFile(mimeType: String, activity: AppCompatActivity): Observable<Uri> {
        val fragmentManager = activity.supportFragmentManager
        val fragment = HeadlessFragment.newSelectImageInstance()
        fragmentManager.beginTransaction().add(fragment, HeadlessFragment::class.java.simpleName).commitNow()
        fragment.launchFileLauncher(mimeType)
        return fragment.getSelectFilePublisher()
    }

    class HeadlessFragment : Fragment() {
        private val selectFilePublisher = ReplaySubject.create<Uri>()

        private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { result: Uri? ->
            result?.let {
                selectFilePublisher.onNext(it)
            }

            selectFilePublisher.onComplete()
        }

        fun launchFileLauncher(mimeType: String) {
            selectFileLauncher.launch(mimeType)
        }

        fun getSelectFilePublisher() = selectFilePublisher

        companion object {
            @JvmStatic
            fun newSelectImageInstance() = HeadlessFragment()
        }
    }
}