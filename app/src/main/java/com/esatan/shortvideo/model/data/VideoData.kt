package com.esatan.shortvideo.model.data

import android.os.Parcelable
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import kotlinx.parcelize.Parcelize
import java.lang.reflect.Type

/**
 * "id": "!8Y69deQVS!"
 * "source": ["!8Y69deQVS!", 1506183372853, ["!03_EzYUgNy", "songci", "宋词", 0], "0", "0"]
 */
@Parcelize
data class VideoData(
    val id: String,
    @JsonAdapter(SourceDeserializer::class)
    val source: SourceData
) : Parcelable {
    fun getVideoUrl() = "http://storage.googleapis.com/pst-framy/vdo/$id.mp4"

    fun getVideoThumbnail() = "http://storage.googleapis.com/pst-framy/stk/$id.jpg"

    fun getUserAvatar() =
        if (source.user?.userId?.isNotBlank() == true) "http://storage.googleapis.com/usr-framy/headshot/${source.user.userId}.jpg" else ""

    fun mockContent() =
        "If you only tuned into the opening of Apple’s iPhone event this week, you might have wondered whether you were watching an emergency first responder training session.\n" +
        "\n" +
        "Apple CEO Tim Cook kicked off the annual event on Wednesday with a three-minute video depicting how the Apple Watch has saved lives by calling for help. One man described how he was skating on a frozen river when the ice gave out. Another survived a plane crash in a remote area in the middle of winter. And a high school student escaped a bear encounter.\n" +
        "\n" +
        "Another shortvideople from the event focused on a 27-year old high school teacher went to the emergency room after her Apple Watch detected an abnormally high heart rate. According to the teacher, “My doctor said, ‘It was your watch that saved your life.”"
}

@Parcelize
data class SourceData(
    val postId: String,
    val createdAt: Long,
    val user: UserData?,
    val isFeature: Boolean,
    val canReuse: Boolean
) : Parcelable

@Parcelize
data class UserData(
    val userId: String,
    val uid: String,
    val name: String,
    val isFollowing: Boolean
) : Parcelable

class SourceDeserializer : JsonDeserializer<SourceData> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SourceData? {
        return json?.asJsonArray?.let {
            val userData = it.get(2).asJsonArray?.let { userArray ->
                UserData(
                    userId = userArray.get(0).asString,
                    uid = userArray.get(1).asString,
                    name = userArray.get(2).asString,
                    isFollowing = userArray.get(3).asBoolean
                )
            }

            SourceData(
                postId = it.get(0).asString,
                createdAt = it.get(1).asLong,
                user = userData,
                isFeature = it.get(3).asBoolean,
                canReuse = it.get(4).asBoolean
            )
        }
    }
}