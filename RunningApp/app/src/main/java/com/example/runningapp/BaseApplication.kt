package com.example.runningapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

// Dependency Injection(Dagger-Hilt használata a dependency injection-höz)
@HiltAndroidApp
class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}