package com.devbrian.osebo.data.repository


import com.devbrian.osebo.models.SalesDataPoint
import com.devbrian.osebo.models.TopProduct
import com.devbrian.osebo.ui.viewmodels.StatisticsViewModel
import com.devbrian.osebo.utils.Resource

class StatisticsRepositoryImpl() : StatisticsRepository {

    override suspend fun getStatisticsSummary(shopId: String, period: String): Resource<StatisticsViewModel.StatisticsSummary> {
        
        return Resource.Error("Not implemented")
    }

    override suspend fun getSalesChartData(shopId: String, period: String): Resource<List<SalesDataPoint>> {
        
        return Resource.Error("Not implemented")
    }

    override suspend fun getTopProducts(shopId: String, period: String): Resource<List<TopProduct>> {
        
        return Resource.Error("Not implemented")
    }
}
