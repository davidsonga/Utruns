package com.example.utrun.Service

import android.app.Application

class MyApp : Application() {
    lateinit var appLifecycleCallback: AppLifecycleCallback

    override fun onCreate() {
        super.onCreate()
        // Initialize the AppLifecycleCallback instance
        appLifecycleCallback = AppLifecycleCallback()

        // Register the AppLifecycleCallback
        registerActivityLifecycleCallbacks(appLifecycleCallback)
    }
}