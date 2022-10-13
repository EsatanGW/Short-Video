package com.esatan.shortvideo.model.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.reflect.TypeToken
import com.esatan.base.extension.*
import com.esatan.shortvideo.model.gson
import retrofit2.HttpException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

@JsonAdapter(ResponseData.Deserializer::class)
class ResponseData<T>(data: T?) : ApiData<T>(0, data) {
    companion object {
        inline fun <reified T> fromJson(json: String?): ResultStatus<T> =
            gson.fromJson<ResponseData<T>>(json, object : TypeToken<ResponseData<T>>() {}.type)
                .toResult<T, ResponseError>()
    }

    inner class Deserializer : JsonDeserializer<ResponseData<T>> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): ResponseData<T>? {
            json?.asJsonObject?.also {
                val childType = (typeOfT as? ParameterizedType)?.actualTypeArguments?.get(0)
                val data =
                    try {
                        it.get("p")?.let { json ->
                            if (json.isJsonNull) null
                            else gson.fromJson<T>(json, childType)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                return ResponseData(data)
            }
            return null
        }
    }
}

suspend inline fun <reified T> createRequest(
    noinline job: suspend () -> ApiData<T>,
    isShowLoading: Boolean = true
): ResultStatus<T> =
    runCatching {
        createLoadingJob<T, ResponseError>(job, isShowLoading)
    }.getOrElse {
        when (it) {
            is HttpException -> {
                runCatching {
                    ResponseData.fromJson<T>(it.response()?.errorBody()?.source()?.readUtf8())
                }.getOrElse { _ ->
                    ResultStatus.Error.NetworkError(it)
                }
            }
            else -> ResultStatus.Error.NetworkError(it)
        }
    }

suspend inline fun <reified T> getResponse(
    isShowLoading: Boolean = true,
    noinline job: suspend () -> ApiData<T>
): T? = createRequest(job, isShowLoading).getResponseData()

sealed class ResponseError(code: Int, exception: Exception) : ResultStatus.Error(code, exception)