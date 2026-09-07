package com.devbrian.osebo.di

import com.devbrian.osebo.ui.AccountViewModel
import com.devbrian.osebo.ui.viewmodels.AddExpenseViewModel
import com.devbrian.osebo.ui.viewmodels.CustomerViewModel
import com.devbrian.osebo.ui.viewmodels.DashboardViewModel
import com.devbrian.osebo.ui.viewmodels.ExpenseCategoriesViewModel
import com.devbrian.osebo.ui.viewmodels.FinanceViewModel
import com.devbrian.osebo.ui.viewmodels.FinancialStatementViewModel
import com.devbrian.osebo.ui.viewmodels.InventoryViewModel
import com.devbrian.osebo.ui.viewmodels.LoginViewModel
import com.devbrian.osebo.ui.viewmodels.ReportsViewModel
import com.devbrian.osebo.ui.viewmodels.SalesViewModel
import com.devbrian.osebo.ui.viewmodels.ShopViewModel
import com.devbrian.osebo.ui.viewmodels.StatisticsViewModel
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.ui.viewmodels.TransactionViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AccountViewModel(get()) }
    viewModel { AddExpenseViewModel(get()) }
    viewModel { CustomerViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { ExpenseCategoriesViewModel(get()) }
    viewModel { FinanceViewModel(get(), get()) }
    viewModel { FinancialStatementViewModel(get(), get()) }
    viewModel { InventoryViewModel(get(), androidContext()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { ReportsViewModel(get(), get()) }
    viewModel { SalesViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ShopViewModel(get()) }
    viewModel { StatisticsViewModel(get()) }
    viewModel { SubscriptionViewModel(get(), androidApplication()) }
    viewModel { TransactionViewModel(get()) }
}
