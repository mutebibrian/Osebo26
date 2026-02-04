package com.devbrian.osebo.di

import com.devbrian.osebo.data.remote.api.OseboApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://your-api-url.com/" // Replace with your URL

    // 1. ADD THIS METHOD: This tells Hilt how to build the Retrofit object
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 2. This method uses the Retrofit object provided above
    @Provides
    @Singleton
    fun provideOseboApiService(retrofit: Retrofit): OseboApiService {
        return retrofit.create(OseboApiService::class.java)
    }
}