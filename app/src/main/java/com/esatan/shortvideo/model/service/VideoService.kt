package com.esatan.shortvideo.model.service

import com.esatan.shortvideo.model.data.ResponseData
import com.esatan.shortvideo.model.data.VideoData
import retrofit2.http.*

interface VideoService {
    @GET("test1.0/backstage/exm1")
    suspend fun updateVideoList(@Header("Authorization") auth: String): ResponseData<List<VideoData>>
}