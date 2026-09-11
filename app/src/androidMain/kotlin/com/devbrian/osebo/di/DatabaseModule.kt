package com.devbrian.osebo.di

import com.devbrian.osebo.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }
}
