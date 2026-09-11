package com.devbrian.osebo.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.dsl.module

val gsonModule = module {
    single<Gson> {
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setPrettyPrinting()
            .create()
    }
}
