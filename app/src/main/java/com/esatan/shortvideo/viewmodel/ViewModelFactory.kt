package com.esatan.shortvideo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.esatan.shortvideo.Application
import com.esatan.shortvideo.model.dao.VideoCommentDao
import com.esatan.shortvideo.model.database.VideoDatabase
import com.esatan.shortvideo.model.repository.VideoRepository
import com.esatan.shortvideo.model.retrofit
import com.esatan.shortvideo.model.service.VideoService

object ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    private val videoRepository: VideoRepository by lazy {
        VideoRepository(retrofit.create(VideoService::class.java))
    }

    private val commentDao by lazy {
        VideoDatabase(Application.instance).videoCommentDao()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                return modelClass
                    .getDeclaredConstructor(VideoRepository::class.java, VideoCommentDao::class.java)
                    .newInstance(videoRepository, commentDao)
            }
        }
        return super.create(modelClass)
    }
}