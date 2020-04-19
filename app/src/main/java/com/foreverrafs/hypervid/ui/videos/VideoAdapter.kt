package com.foreverrafs.hypervid.ui.videos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.model.FacebookVideo
import com.foreverrafs.hypervid.util.ItemTouchCallback
import com.foreverrafs.hypervid.util.getDurationString
import com.foreverrafs.hypervid.util.load
import com.foreverrafs.hypervid.util.shareFile
import kotlinx.android.synthetic.main.item_video__.view.*
import kotlinx.android.synthetic.main.list_empty.view.tvTitle
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.math.abs

class VideoAdapter(
        private val context: Context,
        private val callback: VideoCallback
) :
    RecyclerView.Adapter<VideoAdapter.VideosViewHolder>(),
    ItemTouchCallback.ItemTouchHelperAdapter {

    val videos = mutableListOf<FacebookVideo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video__, parent, false)
        return VideosViewHolder(view)
    }

    private fun swapItems(fromPosition: Int, toPosition: Int) {
        Collections.swap(videos, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        Timber.i("Moved from $fromPosition to $toPosition")
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    inner class VideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(facebookVideo: FacebookVideo) {
            val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
                Timber.e(throwable)
            }

            val job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(facebookVideo.path)

                withContext(Dispatchers.Main) {
                    itemView.imageCover.load(retriever.frameAtTime)
                }
            }

            job.invokeOnCompletion {
                it?.let {

                }

                itemView.tvTitle.text = Html.fromHtml(
                    if (facebookVideo.title.isEmpty()) "Facebook Video - ${abs(facebookVideo.hashCode())}" else facebookVideo.title
                )

                itemView.tvDuration.text = getDurationString(facebookVideo.duration)
            }



            itemView.btnPlay.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(facebookVideo.path), "video/*")

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "Unable to play video. Locate and play it from your gallery", Toast.LENGTH_SHORT).show()
                    Timber.e(e)
                }
            }

            itemView.btnShareWhatsapp.setOnClickListener {
                context.shareFile(facebookVideo.path, "com.whatsapp")
            }

            itemView.btnShare.setOnClickListener {
                val uri = Uri.fromFile(File(facebookVideo.path))

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    setDataAndType(Uri.parse(facebookVideo.path), "*/*")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val shareIntent = Intent.createChooser(sendIntent, "Share Facebook Video")
                shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(shareIntent)
            }
        }
    }

    fun submitList(newVideos: List<FacebookVideo>) {
        val diffCallback =
            VideoDiffCallback(
                this.videos,
                newVideos
            )

        val diffResult = DiffUtil.calculateDiff(diffCallback)

        videos.clear()
        videos.addAll(newVideos)

        diffResult.dispatchUpdatesTo(this)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) =
        swapItems(fromPosition, toPosition)

    override fun onItemDismiss(position: Int) = callback.deleteVideo(videos[position])

    override fun getItemCount(): Int {
        return videos.size
    }

    interface VideoCallback {
        fun deleteVideo(video: FacebookVideo)
    }
}