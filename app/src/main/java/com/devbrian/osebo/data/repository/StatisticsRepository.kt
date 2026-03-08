package com.devbrian.osebo.data.repository


import com.devbrian.osebo.models.SalesDataPoint
import com.devbrian.osebo.models.TopProduct
import com.devbrian.osebo.ui.viewmodels.StatisticsViewModel
import com.devbrian.osebo.utils.Resource

interface StatisticsRepository {
    suspend fun getStatisticsSummary(shopId: String, period: String): Resource<StatisticsViewModel.StatisticsSummary>
    suspend fun getSalesChartData(shopId: String, period: String): Resource<List<SalesDataPoint>>
    suspend fun getTopProducts(shopId: String, period: String): Resource<List<TopProduct>>
}