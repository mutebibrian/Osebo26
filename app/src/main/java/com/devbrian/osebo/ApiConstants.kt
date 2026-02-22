package com.devbrian.osebo.utils

object ApiConstants {

    // BASE URL - Update this!
    const val BASE_URL = "https://dev-api.osebo.ai/"

    // Endpoints (check if these match your DTOs)
    object Endpoints {
        const val LOGIN = "api/auth/signin"
        const val REGISTER = "api/auth/signup"
        const val LOGOUT = "api/auth/logout"
        const val SHOPS = "api/shops"
        const val SALES = "api/sales"
        const val EMPLOYEES = "api/users" // Check user collection
        const val CUSTOMERS = "api/customers"
        const val DASHBOARD = "api/analytics/shop-summary"
        const val FINANCE = "api/analytics/shop-financial-statement"
    }

    // Headers
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_SHOP_ID = "x-shop"
    const val BEARER_PREFIX = "Bearer "
}