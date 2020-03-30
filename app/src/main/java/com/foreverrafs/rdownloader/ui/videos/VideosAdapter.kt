package com.foreverrafs.rdownloader.ui.videos

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.model.FacebookVideo
import com.foreverrafs.rdownloader.util.getDurationString
import com.foreverrafs.rdownloader.util.load
import com.foreverrafs.rdownloader.util.shareFile
import kotlinx.android.synthetic.main.item_video__.view.*
import kotlinx.android.synthetic.main.list_empty.view.tvTitle
import java.io.File
import kotlin.math.abs

class VideosAdapter(private val context: Context) :
    ListAdapter<FacebookVideo, VideosAdapter.VideosViewHolder>(object :
        DiffUtil.ItemCallback<FacebookVideo>() {
        override fun areItemsTheSame(oldItem: FacebookVideo, newItem: FacebookVideo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: FacebookVideo, newItem: FacebookVideo): Boolean {
            return oldItem.path == newItem.path
        }
    }) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video__, parent, false)
        return VideosViewHolder(view)
    }



    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(facebookVideo: FacebookVideo) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(facebookVideo.path)

            itemView.imageCover.load(retriever.frameAtTime)

            itemView.tvTitle.text = Html.fromHtml(
                if (facebookVideo.title.isEmpty()) "Facebook Video - ${abs(facebookVideo.hashCode())}" else facebookVideo.title
            )

            itemView.tvDuration.text = getDurationString(facebookVideo.duration)

            itemView.btnPlay.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(facebookVideo.path), "video/*")

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
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
}