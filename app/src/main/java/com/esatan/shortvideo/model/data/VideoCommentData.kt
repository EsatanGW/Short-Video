package com.esatan.shortvideo.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_comments")
data class VideoCommentData(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name = "video_id")
    val videoId: String,
    val nickname: String,
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,
    val comment: String,
    val timestamp: Long
)