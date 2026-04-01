package com.devbrian.osebo.di


import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.repository.FinanceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FinanceModule {

    @Provides
    @Singleton
    fun provideFinanceRepository(
        apiService: ApiService,
        preferenceManager: PreferenceManager
    ): FinanceRepository {
        return FinanceRepository(apiService, preferenceManager)
    }
}