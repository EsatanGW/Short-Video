package com.esatan.shortvideo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.esatan.shortvideo.model.repository.VideoRepository
import com.esatan.shortvideo.model.retrofit
import com.esatan.shortvideo.model.service.VideoService

object ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    private val videoRepository: VideoRepository by lazy {
        VideoRepository(retrofit.create(VideoService::class.java))
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                return modelClass
                    .getDeclaredConstructor(VideoRepository::class.java)
                    .newInstance(videoRepository)
            }
        }
        return super.create(modelClass)
    }
}