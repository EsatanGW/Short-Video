package com.esatan.shortvideo.view

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.bumptech.glide.Glide
import com.esatan.base.extension.measureListener
import com.esatan.base.view.BasePagingDataAdapter
import com.esatan.base.view.ViewHolderInjection
import com.esatan.base.view.viewbinding.BaseViewHolder
import com.esatan.shortvideo.R
import com.esatan.shortvideo.databinding.ItemVideoDetailBinding
import com.esatan.shortvideo.databinding.ViewVideoInformationBarBinding
import com.esatan.shortvideo.model.data.VideoData
import com.esatan.shortvideo.parsedTime
import com.esatan.shortvideo.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class VideoDetailAdapter(
    private val mainViewModel: MainViewModel,
    onChat: (View, Int, VideoData?) -> Unit,
    onLike: (View, Int, VideoData?) -> Unit
) : BasePagingDataAdapter<VideoData, VideoDetailAdapter.VideoDetailViewHolder, ItemVideoDetailBinding>() {

    var currentHolder: VideoDetailAdapter.VideoDetailViewHolder? = null

    override val viewHolderInjection: ViewHolderInjection<VideoDetailViewHolder, ItemVideoDetailBinding> =
        object : ViewHolderInjection<VideoDetailViewHolder, ItemVideoDetailBinding> {
            override fun getViewInject(parent: ViewGroup, viewType: Int): ItemVideoDetailBinding =
                ItemVideoDetailBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).apply {
                    root.apply {
                        layoutParams = layoutParams.apply {
                            width = ViewGroup.LayoutParams.MATCH_PARENT
                            height = ViewGroup.LayoutParams.MATCH_PARENT
                        }
                    }
                }

            override fun getViewHolder(
                r: ItemVideoDetailBinding,
                viewType: Int
            ): VideoDetailViewHolder =
                VideoDetailViewHolder(
                    binding = r,
                    onChat = onChat,
                    onLike = onLike
                )
        }

    override fun onBindViewHolder(holder: VideoDetailViewHolder, position: Int) {
        val data = getItem(position)

        holder.binding.apply {
            mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                val image =
                    Glide.with(root.context)
                        .`as`(BitmapDrawable::class.java)
                        .load(data?.getVideoThumbnail())
                        .submit()
                        .get()
                withContext(Dispatchers.Main) {
                    videoView.defaultArtwork = image
                }
            }

            ViewVideoInformationBarBinding.bind(videoView.informationBar).apply {
                Glide.with(ivAvatar)
                    .load(data?.getUserAvatar())
                    .thumbnail(Glide.with(ivAvatar).load(R.mipmap.ic_launcher_round).circleCrop())
                    .circleCrop()
                    .transform()
                    .into(ivAvatar)
                tvContent.run {
                    text = data?.mockContent()
                    if (mainViewModel.videoContentWidth == 0) {
                        measureListener {
                            mainViewModel.videoContentWidth = width
                            setLineHeight(data?.mockContent() ?: "")
                        }
                    } else {
                        setLineHeight(data?.mockContent() ?: "")
                    }
                }

                tvNickname.text = data?.source?.user?.name
                tvUploadTime.text = data?.source?.createdAt?.parsedTime(root.context)
            }
        }

        holder.videoUrl = data?.getVideoUrl()
        holder.initializePlayer()
    }

    private fun TextView.setLineHeight(content: String) {
        mainViewModel.viewModelScope.launch(Dispatchers.Default) {
            val width = mainViewModel.videoContentWidth
            val length = paint.measureText(content)
            withContext(Dispatchers.Main) {
                maxLines = if (length < width && lineCount == 1) Int.MAX_VALUE else 1
            }
        }
    }

    override fun onViewAttachedToWindow(holder: VideoDetailViewHolder) {
        super.onViewAttachedToWindow(holder)
        currentHolder = holder
        holder.binding.videoView.controllerAutoShow = false
        holder.play()
    }

    override fun onViewDetachedFromWindow(holder: VideoDetailViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.pause()
        holder.resetPosition()
        holder.releasePlayer()
    }

    inner class VideoDetailViewHolder(
        override val binding: ItemVideoDetailBinding,
        onChat: (View, Int, VideoData?) -> Unit,
        onLike: (View, Int, VideoData?) -> Unit
    ) : BaseViewHolder<ItemVideoDetailBinding>(binding) {

        init {
            ViewVideoInformationBarBinding.bind(binding.videoView.informationBar).apply {
                btnVideoComment.setOnClickListener {
                    onChat.invoke(it, bindingAdapterPosition, getItem(bindingAdapterPosition))
                }
                btnVideoLike.setOnClickListener {
                    onLike.invoke(it, bindingAdapterPosition, getItem(bindingAdapterPosition))
                }
                tvContent.setOnClickListener {
                    tvContent.apply {
//                        val width = mainViewModel.videoContentWidth
//                        val length = paint.measureText(text.toString())
                        maxLines = if (maxLines == Int.MAX_VALUE) 1 else Int.MAX_VALUE
                    }
                }
                binding.videoView.setOnClickListener {
                    tvContent.maxLines = 1
                }
                binding.videoView.informationBar.setOnClickListener {
                    tvContent.maxLines = 1
                }
            }
        }

        var videoUrl: String? = null

        private val context = binding.root.context

        private var player: ExoPlayer? = null

        fun initializePlayer() {
            player = ExoPlayer.Builder(context)
                .setTrackSelector(
                    DefaultTrackSelector(context).apply {
                        setParameters(buildUponParameters().setMaxVideoSizeSd())
                    }
                )
                .build()
                .also { exoPlayer ->
                    binding.videoView.player = exoPlayer

                    exoPlayer.setMediaItem(
                        MediaItem.fromUri(videoUrl ?: "")
                    )
                    exoPlayer.playWhenReady = false
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                    exoPlayer.addListener(playbackStateListener)
                    exoPlayer.prepare()
                }
        }

        fun releasePlayer() {
            player?.let { exoPlayer ->
                exoPlayer.removeListener(playbackStateListener)
                exoPlayer.release()
            }
            player = null
        }

        fun play() {
            if (player == null) initializePlayer()
            player?.play()
            binding.videoView.hideController()
        }

        fun pause() {
            player?.pause()
        }

        fun resetPosition() {
            player?.seekTo(0)
        }
    }
}

val playbackStateListener = object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateString: String = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
            ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
            else -> "UNKNOWN_STATE             -"
        }
        Log.d(this.javaClass.name, "changed state to $stateString")
    }
}