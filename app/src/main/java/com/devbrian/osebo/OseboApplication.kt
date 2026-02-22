package com.devbrian.osebo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OseboApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}