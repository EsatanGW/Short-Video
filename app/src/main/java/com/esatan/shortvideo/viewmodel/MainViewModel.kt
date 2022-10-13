package com.esatan.shortvideo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esatan.shortvideo.Application
import com.esatan.shortvideo.model.data.VideoCommentData
import com.esatan.shortvideo.model.database.VideoDatabase
import com.esatan.shortvideo.model.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val videoRepository: VideoRepository) : ViewModel() {

    private val commentDao by lazy {
        VideoDatabase(Application.instance).videoCommentDao()
    }

    var videoContentWidth: Int = 0

    private val _keyboardMaskVisibleLivedata = MutableLiveData<Boolean>()
    val keyboardMaskVisibleLivedata: LiveData<Boolean> = _keyboardMaskVisibleLivedata

    fun setKeyboardMaskVisible(isVisible: Boolean) {
        _keyboardMaskVisibleLivedata.value = isVisible
    }

    suspend fun queryVideoArray(auth: String) =  videoRepository.queryVideoArray(auth)

    fun queryVideoComments(videoId: String) = commentDao.queryComments(videoId)

    fun addVideoComments(videoId: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            commentDao.addComment(
                VideoCommentData(
                    videoId = videoId,
                    nickname = "Guest",
                    avatarUrl = null,
                    comment = message,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}