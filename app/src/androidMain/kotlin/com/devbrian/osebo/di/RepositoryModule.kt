package com.devbrian.osebo.di

import com.devbrian.osebo.data.repository.ShopRepository
import com.devbrian.osebo.data.repository.ShopRepositoryImpl
import com.devbrian.osebo.data.repository.StatisticsRepository
import com.devbrian.osebo.data.repository.StatisticsRepositoryImpl
import com.devbrian.osebo.data.repository.SubscriptionRepository
import com.devbrian.osebo.data.repository.SubscriptionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShopRepository(
        shopRepositoryImpl: ShopRepositoryImpl
    ): ShopRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        subscriptionRepositoryImpl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(
        statisticsRepositoryImpl: StatisticsRepositoryImpl
    ): StatisticsRepository
}
