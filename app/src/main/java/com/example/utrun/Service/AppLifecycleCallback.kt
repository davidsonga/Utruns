package com.example.utrun.Service

import android.app.Activity
import android.app.Application
import android.os.Bundle

class AppLifecycleCallback : Application.ActivityLifecycleCallbacks {

    private var appInForeground = false

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Not used, but you can implement if needed
    }

    override fun onActivityStarted(activity: Activity) {
        // Not used, but you can implement if needed
    }

    override fun onActivityResumed(activity: Activity) {
        // App is in the foreground
        appInForeground = true
    }

    override fun onActivityPaused(activity: Activity) {
        // App is going into the background
        appInForeground = false
    }

    override fun onActivityStopped(activity: Activity) {
        // Not used, but you can implement if needed
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // Not used, but you can implement if needed
    }

    override fun onActivityDestroyed(activity: Activity) {
        // Not used, but you can implement if needed
    }

    fun isAppInForeground(): Boolean {
        return appInForeground
    }
}