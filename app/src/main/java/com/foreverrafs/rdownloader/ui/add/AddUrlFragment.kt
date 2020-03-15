package com.foreverrafs.rdownloader.ui.add

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.foreverrafs.downloader.extractor.FacebookExtractor
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.downloader.model.FacebookFile
import com.foreverrafs.rdownloader.MainViewModel
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.util.disable
import com.foreverrafs.rdownloader.util.enable
import com.foreverrafs.rdownloader.util.showToast
import kotlinx.android.synthetic.main.fragment_addurl.*
import org.joda.time.DateTime
import timber.log.Timber

class AddUrlFragment : Fragment() {
    private lateinit var clipboardText: String
    private var clipBoardData: ClipData? = null
    private val viewModel: MainViewModel by activityViewModels()

    private var mDownloadInfo: DownloadInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_addurl, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeViews()
        initializeClipboard()
    }

    private fun initializeViews() {
        btnPaste.setOnClickListener {
            if (clipBoardData != null) {
                clipboardText = clipBoardData?.getItemAt(0)?.text.toString()
                etFacebookUrl.setText(clipboardText)
            }
        }

        //add the download job to the download list when the button is clicked. We don't start downloading
        //immediately. We wait for the user to interract with it in the downloads section before we download.
        btnAddToDownloads.setOnClickListener {
            extractVideo(etFacebookUrl.text.toString())

            btnAddToDownloads.text = getString(R.string.extracting)
            btnAddToDownloads.disable()
            etFacebookUrl.disable()
        }
    }

    private fun extractVideo(videoURL: String) {
        viewModel.extractVideoDownloadUrl(
            videoURL,
            object : FacebookExtractor.ExtractionEvents {
                override fun onComplete(facebookFile: FacebookFile) {
                    mDownloadInfo = DownloadInfo(
                        facebookFile.url,
                        0,
                        facebookFile.author,
                        facebookFile.duration
                    )

                    viewModel.downloadsAdapter.addDownload(mDownloadInfo!!)

                    Timber.d("Download URL extraction complete: Adding to List: $mDownloadInfo")
                    showToast("Video added to download queue...")
                    btnPaste.text = getString(R.string.paste_link)
                    etFacebookUrl.text?.clear()

                    btnAddToDownloads.text = getString(R.string.add_to_downloads)
                    btnAddToDownloads.enable()
                    etFacebookUrl.enable()
                    btnAddToDownloads.enable()
                }

                override fun onError(exception: Exception) {
                    Timber.e(exception)
                    showToast("Error loading video from link")
                    textInputLayout.isErrorEnabled = true

                    btnAddToDownloads.text = getString(R.string.add_to_downloads)
                    btnAddToDownloads.enable()
                    etFacebookUrl.enable()
                }
            })
    }

    private fun initializeClipboard() {
        val clipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipBoardData = clipboardManager.primaryClip

        clipboardManager.addPrimaryClipChangedListener {
            clipBoardData = clipboardManager.primaryClip
            val clipText = clipBoardData?.getItemAt(0)?.text

            if (clipText!!.contains("facebook"))
                etFacebookUrl.setText(clipText.toString())
        }
    }
}