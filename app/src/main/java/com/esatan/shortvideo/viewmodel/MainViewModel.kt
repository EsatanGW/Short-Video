package com.esatan.shortvideo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esatan.shortvideo.model.dao.VideoCommentDao
import com.esatan.shortvideo.model.data.VideoCommentData
import com.esatan.shortvideo.model.repository.VideoRepository

class MainViewModel(private val videoRepository: VideoRepository, private val commentDao: VideoCommentDao) : ViewModel() {

    var videoContentWidth: Int = 0

    private val _keyboardMaskVisibleLivedata = MutableLiveData<Boolean>()
    val keyboardMaskVisibleLivedata: LiveData<Boolean> = _keyboardMaskVisibleLivedata

    fun setKeyboardMaskVisible(isVisible: Boolean) {
        _keyboardMaskVisibleLivedata.value = isVisible
    }

    suspend fun queryVideoArray(auth: String) = videoRepository.queryVideoArray(auth)

    fun queryVideoComments(videoId: String) = commentDao.queryComments(videoId)

    suspend fun addVideoComments(videoId: String, message: String, timeStamp: Long = System.currentTimeMillis()) =
        commentDao.addComment(
            VideoCommentData(
                videoId = videoId,
                nickname = "Guest",
                avatarUrl = null,
                comment = message,
                timestamp = timeStamp
            )
        )
}