package com.esatan.shortvideo.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.esatan.shortvideo.model.dao.VideoCommentDao
import com.esatan.shortvideo.model.data.VideoCommentData

@Database(entities = [VideoCommentData::class], version = 1)
abstract class VideoDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var instance: VideoDatabase? = null

        private val LOCK = Any()

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context,
                VideoDatabase::class.java,
                "Video"
            ).build()
    }

    abstract fun videoCommentDao(): VideoCommentDao
}