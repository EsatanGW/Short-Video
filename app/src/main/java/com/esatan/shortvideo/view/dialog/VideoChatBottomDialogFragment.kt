package com.esatan.shortvideo.view.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.esatan.base.extension.getScreenHeight
import com.esatan.base.extension.viewBinding
import com.esatan.base.extension.viewModel
import com.esatan.shortvideo.R
import com.esatan.shortvideo.databinding.DialogFragmentVideoChatBinding
import com.esatan.shortvideo.databinding.ItemVideoCommentBinding
import com.esatan.shortvideo.model.data.VideoData
import com.esatan.base.view.BaseBottomSheetDialogFragment
import com.esatan.base.view.initSubmit
import com.esatan.base.view.viewbinding.BasePagingDataAdapter
import com.esatan.base.view.viewbinding.BaseViewHolder
import com.esatan.shortvideo.model.data.VideoCommentData
import com.esatan.shortvideo.parsedTime
import com.esatan.shortvideo.viewmodel.MainViewModel
import com.esatan.shortvideo.viewmodel.ViewModelFactory
import kotlinx.coroutines.*

class VideoChatBottomDialogFragment :
    BaseBottomSheetDialogFragment<DialogFragmentVideoChatBinding>() {

    companion object {
        const val ARGUMENTS_VIDEO_DATA = "video_data"
    }

    private val mainViewModel: MainViewModel by viewModel(ViewModelFactory)

    private val data by lazy { arguments?.getParcelable<VideoData>(ARGUMENTS_VIDEO_DATA) }

    var onDismiss: (() -> Unit)? = null

    override val binding: DialogFragmentVideoChatBinding by viewBinding()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    // do nothing
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    mainViewModel.setKeyboardMaskVisible(slideOffset == 0F)
                }
            })
        }
    }

    override fun DialogFragmentVideoChatBinding.initView() {
        if (data == null) {
            dismiss()
            return
        }

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding.rvComment.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = VideoCommentAdapter().apply {
                lifecycleScope.launchWhenStarted {
                    initSubmit(
                        lifecycle,
                        pagingSourceFactory = {
                            mainViewModel.queryVideoComments(data!!.id).apply {
                                postDelayed({ smoothScrollToPosition(0) }, 100)
                            }
                        }
                    )
                }
            }
            setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        etInput.closeKeyboard()
                    }
                    MotionEvent.ACTION_UP -> view.performClick()
                }
                false
            }
        }

        initFunctionView()
    }

    private fun DialogFragmentVideoChatBinding.initFunctionView() {
        etInput.run {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                    onSend(etInput.text.toString())
                    return@setOnEditorActionListener true
                }
                false
            }
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    closeKeyboard()
                }
            }
        }
        btnSend.setOnClickListener {
            onSend(etInput.text.toString())
        }
    }

    private fun DialogFragmentVideoChatBinding.onSend(message: String) {
        if (message.isBlank()) return

        lifecycleScope.launch(Dispatchers.IO) {
            val commentId = mainViewModel.addVideoComments(data!!.id, message)
            if (commentId < 1) return@launch

            withContext(Dispatchers.Main) {
                etInput.apply {
                    setText("")
                    clearFocus()
                }
                rvComment.apply {
                    (adapter as VideoCommentAdapter).refresh()
                    postDelayed({ smoothScrollToPosition(0) }, 100)
                }
            }
        }
    }

    private fun EditText.closeKeyboard() {
        clearFocus()
        context.getSystemService(InputMethodManager::class.java)
            .hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onStart() {
        super.onStart()
        setFullScreen()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss?.invoke()
    }

    private fun setFullScreen() {
        val root = requireView().parent as View
        root.layoutParams = root.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val behaviour = BottomSheetBehavior.from(root)
        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        behaviour.peekHeight = getScreenHeight()
    }
}

class VideoCommentAdapter : BasePagingDataAdapter<VideoCommentData, ItemVideoCommentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, attachToParent: Boolean, viewType: Int) -> ItemVideoCommentBinding
        get() = { layoutInflater, viewGroup, attachToParent, _ ->
            ItemVideoCommentBinding.inflate(layoutInflater, viewGroup, attachToParent)
        }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ItemVideoCommentBinding>,
        position: Int
    ) {
        holder.binding.apply {
            val data = getItem(position)
            Glide.with(ivAvatar)
                .load(data?.avatarUrl)
                .thumbnail(Glide.with(ivAvatar).load(R.mipmap.ic_launcher_round).circleCrop())
                .circleCrop()
                .transform()
                .into(ivAvatar)
            tvComment.text = data?.handleMessage()
            tvDate.text = data?.timestamp?.parsedTime(tvDate.context)
        }
    }

    private fun VideoCommentData.handleMessage(): SpannableStringBuilder? {
        return SpannableStringBuilder()
            .append(
                SpannableStringBuilder(nickname).apply {
                    setSpan(
                        StyleSpan(Typeface.BOLD),
                        0,
                        length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            )
            .append("\t\t")
            .append(SpannableStringBuilder(comment))
    }
}