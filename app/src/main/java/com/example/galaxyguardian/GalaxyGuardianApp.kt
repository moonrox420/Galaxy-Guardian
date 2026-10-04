package com.example.galaxyguardian

import android.app.Application
import android.util.Log

class GalaxyGuardianApp : Application() {

    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()
        Log.i("GalaxyGuardian", "GalaxyGuardianApp initialized successfully")

        // Install global uncaught exception handler to prevent silent crashes and log full diagnostics
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("GalaxyGuardian", "FATAL UNCAUGHT EXCEPTION on thread ${thread.name} [ID: ${thread.id}]", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
