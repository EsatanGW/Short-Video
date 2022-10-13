package com.esatan.shortvideo

class Application : android.app.Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}