package com.esatan.shortvideo.model.repository

import com.esatan.shortvideo.model.data.getResponse
import com.esatan.shortvideo.model.service.VideoService

class VideoRepository(private val videoService: VideoService) {
    suspend fun queryVideoArray(auth: String) = getResponse { videoService.updateVideoList(auth) }
}