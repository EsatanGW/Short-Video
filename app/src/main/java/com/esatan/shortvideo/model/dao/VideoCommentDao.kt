package com.esatan.shortvideo.model.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.esatan.shortvideo.model.data.VideoCommentData

@Dao
interface VideoCommentDao {
    @Query("SELECT * FROM video_comments WHERE video_id = :videoId ORDER BY id DESC")
    fun queryComments(videoId: String) : PagingSource<Int, VideoCommentData>

    @Insert
    fun addComment(data: VideoCommentData): Long
}