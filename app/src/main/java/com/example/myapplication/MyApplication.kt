package com.example.myapplication

import android.app.Application
import com.google.android.libraries.places.api.Places
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializePlaces()
    }

    private fun initializePlaces() {
        val apiKey = BuildConfig.PLACES_API_KEY

        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No api key")
            return
        }

        Places.initialize(applicationContext, apiKey)
    }
}