package com.esatan.shortvideo.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

var DOMAIN = "https://srv0api-dev-v2-dot-framy-stage.uc.r.appspot.com"

val gson: Gson = GsonBuilder().create()

val okHttpClient by lazy {
    OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
}

val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(DOMAIN)
        .build()
}