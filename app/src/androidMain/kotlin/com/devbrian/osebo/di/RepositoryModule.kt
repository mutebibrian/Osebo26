package com.devbrian.osebo.di

import com.devbrian.osebo.data.mapper.AccountMapper
import com.devbrian.osebo.data.mapper.SubscriptionMapper
import com.devbrian.osebo.data.repository.AccountRepository
import com.devbrian.osebo.data.repository.AiRepository
import com.devbrian.osebo.data.repository.CustomerRepository
import com.devbrian.osebo.data.repository.DashboardRepository
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.data.repository.InventoryRepository
import com.devbrian.osebo.data.repository.ProductRepository
import com.devbrian.osebo.data.repository.SalesRepository
import com.devbrian.osebo.data.repository.ShopRepository
import com.devbrian.osebo.data.repository.ShopRepositoryImpl
import com.devbrian.osebo.data.repository.StatisticsRepository
import com.devbrian.osebo.data.repository.StatisticsRepositoryImpl
import com.devbrian.osebo.data.repository.SubscriptionRepository
import com.devbrian.osebo.data.repository.SubscriptionRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {

    // Mappers
    single { AccountMapper() }
    single { SubscriptionMapper() }

    // Concrete repositories (each has exactly one owning single — the previous
    // Hilt setup declared these both via @Provides *and* via @Inject
    // constructor, which is a duplicate binding Dagger would have rejected)
    single { AiRepository(get()) }
    single { FinanceRepository(get(), get()) }
    single { ProductRepository(get(), get(), get()) }
    single { SalesRepository(get(), get(), get(), get()) }
    single { InventoryRepository(get(), get(), get()) }
    single { CustomerRepository(get(), get(), get(), get()) }
    single { AccountRepository(get(), get(), get()) }
    single { DashboardRepository(get(), get(), get()) }

    // Interface-bound repositories
    single<ShopRepository> { ShopRepositoryImpl(get(), get(), get()) }
    single<SubscriptionRepository> { SubscriptionRepositoryImpl(get()) }
    single<StatisticsRepository> { StatisticsRepositoryImpl() }
}
