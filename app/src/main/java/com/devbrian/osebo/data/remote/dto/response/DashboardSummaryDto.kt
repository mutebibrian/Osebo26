package com.devbrian.osebo.data.remote.dto.response

data class DashboardSummaryDto(
    val sales: SalesSummaryDto?,
    val expenses: ExpensesSummaryDto?,
    val customers: CustomersSummaryDto?,
    val employees: EmployeesSummaryDto?
)


