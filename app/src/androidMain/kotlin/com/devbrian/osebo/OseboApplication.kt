package com.devbrian.osebo

import android.app.Application
import com.devbrian.osebo.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class OseboApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@OseboApplication)
            modules(appModules)
        }
    }
}
