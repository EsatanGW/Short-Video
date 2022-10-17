package com.esatan.shortvideo.view

import android.annotation.SuppressLint
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.esatan.base.extension.showDialog
import com.esatan.base.extension.viewBinding
import com.esatan.base.extension.viewModel
import com.esatan.base.view.BaseActivity
import com.esatan.base.view.initSubmit
import com.esatan.shortvideo.databinding.ActivityMainBinding
import com.esatan.shortvideo.model.data.VideoData
import com.esatan.shortvideo.view.dialog.VideoChatBottomDialogFragment
import com.esatan.shortvideo.viewmodel.MainViewModel
import com.esatan.shortvideo.viewmodel.ViewModelFactory

@UnstableApi
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val mainViewModel: MainViewModel by viewModel(ViewModelFactory)

    override val binding: ActivityMainBinding by viewBinding()

    override fun ActivityMainBinding.initView() {
        val helper = PagerSnapHelper()

        binding.rvVideo.apply {
            layoutManager = object : LinearLayoutManager(context, RecyclerView.VERTICAL, false) {
                override fun onAttachedToWindow(view: RecyclerView?) {
                    super.onAttachedToWindow(view)
                    helper.attachToRecyclerView(view)
                }

                override fun onScrollStateChanged(state: Int) {
                    super.onScrollStateChanged(state)
                    when (state) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            helper.findSnapView(this) ?: return
                        }
                    }
                }
            }
            adapter = VideoDetailAdapter(
                mainViewModel,
                onChat = ::onChat,
                onLike = ::onLike
            ).apply {
                lifecycleScope.launchWhenStarted {
                    initSubmit(lifecycle) { currentPage ->
                        mainViewModel.queryVideoArray("0123456789#0#shortvideoId")
                    }
                }
            }
        }

        mainViewModel.keyboardMaskVisibleLivedata.observe(this@MainActivity) {
            viewCommentMask.isVisible = it
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        (binding.rvVideo.adapter as VideoDetailAdapter).currentHolder?.play()
    }

    override fun onPause() {
        super.onPause()
        (binding.rvVideo.adapter as VideoDetailAdapter).currentHolder?.pause()
    }

    private fun onChat(view: View, position: Int, data: VideoData?) {
        binding.viewCommentMask.isVisible = true
        (binding.rvVideo.adapter as VideoDetailAdapter).currentHolder?.pause()
        showDialog<VideoChatBottomDialogFragment>(
            bundle = bundleOf(VideoChatBottomDialogFragment.ARGUMENTS_VIDEO_DATA to data)
        ).onDismiss = {
            binding.viewCommentMask.isVisible = false
            (binding.rvVideo.adapter as VideoDetailAdapter).currentHolder?.play()
        }
    }

    private fun onLike(view: View, position: Int, data: VideoData?) {
        view.isSelected = !view.isSelected
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}