package com.devbrian.osebo.data.remote.api

import com.devbrian.osebo.data.remote.dto.request.*
import com.devbrian.osebo.data.remote.dto.response.*
import com.devbrian.osebo.models.ApiResponse
import com.devbrian.osebo.models.SupportMessageRequest

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class FinanceReportDto(
    @SerializedName("period")
    val period: String,

    @SerializedName("total_revenue")
    val totalRevenue: Double,

    @SerializedName("total_expenses")
    val totalExpenses: Double,

    @SerializedName("net_profit")
    val netProfit: Double
)

interface OseboApiService {

    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>  

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>  

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>  

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<AuthResponse>>  

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>  

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>  

    
    @GET("users/profile")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>  

    @PUT("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserDto>>  

    @Multipart
    @PUT("users/profile/image")
    suspend fun uploadProfileImage(@Part image: MultipartBody.Part): Response<ApiResponse<UserDto>>  

    @DELETE("users/profile/image")
    suspend fun resetProfileImage(): Response<ApiResponse<Unit>>  

    @PUT("users/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>  

    
    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): Response<ApiResponse<DashboardSummaryDto>>  

    @GET("dashboard/analytics")
    suspend fun getDashboardAnalytics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<DashboardAnalyticsDto>>  

    
    @GET("shops")
    suspend fun getShops(): Response<ApiResponse<List<ShopDto>>>

    @GET("shops/{id}")
    suspend fun getShopById(@Path("id") shopId: String): Response<ApiResponse<ShopDto>>  

    @POST("shops")
    suspend fun createShop(@Body request: CreateShopRequest): Response<ApiResponse<ShopDto>>  

    @PUT("shops/{id}")
    suspend fun updateShop(@Path("id") shopId: String, @Body request: UpdateShopRequest): Response<ApiResponse<ShopDto>>  

    @DELETE("shops/{id}")
    suspend fun deleteShop(@Path("id") shopId: String): Response<ApiResponse<Unit>>  

    
    
    @GET("sales")
    suspend fun getSales(
        @Query("shopId") shopId: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<List<SaleDto>>>  

    @POST("sales")
    suspend fun createSale(@Body request: CreateSaleRequest): Response<ApiResponse<SaleDto>>  

    
    @GET("finance/summary")
    suspend fun getFinanceSummary(
        @Query("shopId") shopId: String? = null,
        @Query("period") period: String? = "monthly"
    ): Response<ApiResponse<FinanceSummaryDto>>  

    @GET("finance/transactions")
    suspend fun getFinanceTransactions(
        @Query("shopId") shopId: String? = null,
        @Query("type") type: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<List<TransactionDto>>>  

    
    @GET("inventory")
    suspend fun getInventory(
        @Query("shopId") shopId: String? = null,
        @Query("category") category: String? = null
    ): Response<ApiResponse<List<InventoryItemDto>>>  

    @POST("inventory")
    suspend fun addInventoryItem(@Body request: AddInventoryItemRequest): Response<ApiResponse<InventoryItemDto>>  

    @PUT("inventory/{id}")
    suspend fun updateInventoryItem(
        @Path("id") itemId: String,
        @Body request: UpdateInventoryItemRequest
    ): Response<ApiResponse<InventoryItemDto>>  

    
    @GET("transfers")
    suspend fun getTransfers(
        @Query("shopId") shopId: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<TransferDto>>>  

    @POST("transfers")
    suspend fun createTransfer(@Body request: CreateTransferRequest): Response<ApiResponse<TransferDto>>  

    
    
    @GET("employees")
    suspend fun getEmployees(@Query("shopId") shopId: String? = null): Response<ApiResponse<List<EmployeeDto>>>  

    @POST("employees")
    suspend fun addEmployee(@Body request: AddEmployeeRequest): Response<ApiResponse<EmployeeDto>>  

    
    @GET("customers")
    suspend fun getCustomers(
        @Query("shopId") shopId: String? = null,
        @Query("search") searchQuery: String? = null
    ): Response<ApiResponse<List<CustomerDto>>>  

    @POST("customers")
    suspend fun addCustomer(@Body request: AddCustomerRequest): Response<ApiResponse<CustomerDto>>  

    
    @GET("suppliers")
    suspend fun getSuppliers(@Query("shopId") shopId: String? = null): Response<ApiResponse<List<SupplierDto>>>  

    @POST("suppliers")
    suspend fun addSupplier(@Body request: AddSupplierRequest): Response<ApiResponse<SupplierDto>>  

    
    @GET("subscription/current")
    suspend fun getCurrentSubscription(): Response<ApiResponse<SubscriptionDto>>  

    @GET("subscription/plans")
    suspend fun getSubscriptionPlans(): Response<ApiResponse<List<SubscriptionPlanDto>>>  

    @POST("subscription/subscribe")
    suspend fun subscribeToPlan(@Body request: SubscribeRequest): Response<ApiResponse<SubscriptionDto>>  

    @DELETE("subscription/cancel")
    suspend fun cancelSubscription(): Response<ApiResponse<Unit>>  

    @GET("subscription/history")
    suspend fun getSubscriptionHistory(): Response<ApiResponse<List<SubscriptionHistoryDto>>>  

    @PUT("subscription/auto-renew")
    suspend fun updateAutoRenew(@Body request: AutoRenewRequest): Response<ApiResponse<Unit>>  

    @GET("subscription/invoices")
    suspend fun getInvoices(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<InvoiceDto>>>  

    @GET("subscription/invoices/{id}")
    suspend fun getInvoiceById(@Path("id") invoiceId: String): Response<ApiResponse<InvoiceDto>>  

    @POST("subscription/upgrade")
    suspend fun upgradeSubscription(@Body request: UpgradeSubscriptionRequest): Response<ApiResponse<SubscriptionDto>>  

    @POST("subscription/downgrade")
    suspend fun downgradeSubscription(@Body request: DowngradeSubscriptionRequest): Response<ApiResponse<SubscriptionDto>>  

    @POST("subscription/payment-method")
    suspend fun updatePaymentMethod(@Body request: UpdatePaymentMethodRequest): Response<ApiResponse<Unit>>  

    @GET("subscription/billing-info")
    suspend fun getBillingInfo(): Response<ApiResponse<BillingInfoDto>>  

    
    @GET("user-roles")
    suspend fun getUserRoles(): Response<ApiResponse<List<UserRoleDto>>>  

    @POST("user-roles")
    suspend fun createUserRole(@Body request: CreateUserRoleRequest): Response<ApiResponse<UserRoleDto>>  

    @PUT("user-roles/{id}")
    suspend fun updateUserRole(
        @Path("id") roleId: String,
        @Body request: UpdateUserRoleRequest
    ): Response<ApiResponse<UserRoleDto>>  

    @DELETE("user-roles/{id}")
    suspend fun deleteUserRole(@Path("id") roleId: String): Response<ApiResponse<Unit>>  

    
    @GET("account")
    suspend fun getAccountDetails(): Response<ApiResponse<AccountDto>>  

    @PUT("account")
    suspend fun updateAccount(@Body request: UpdateAccountRequest): Response<ApiResponse<AccountDto>>  

    @PUT("account/two-factor")
    suspend fun updateTwoFactorAuth(@Body request: TwoFactorAuthRequest): Response<ApiResponse<AccountDto>>  

    @PUT("account/notifications")
    suspend fun updateLoginNotifications(@Body request: LoginNotificationsRequest): Response<ApiResponse<AccountDto>>  

    @POST("account/deactivate")
    suspend fun deactivateAccount(): Response<ApiResponse<Unit>>  

    @DELETE("account")
    suspend fun deleteAccount(): Response<ApiResponse<Unit>>  

    @GET("account/sessions")
    suspend fun getActiveSessions(): Response<ApiResponse<List<SessionDto>>>  

    @DELETE("account/sessions/{id}")
    suspend fun terminateSession(@Path("id") sessionId: String): Response<ApiResponse<Unit>>  

    
    @GET("contact")
    suspend fun getContactInfo(): Response<ApiResponse<ContactInfoDto>>  

    @POST("support/tickets")
    suspend fun createSupportTicket(@Body request: CreateSupportTicketRequest): Response<ApiResponse<SupportTicketDto>>  

    @POST("support/messages")
    suspend fun sendSupportMessage(@Body request: SupportMessageRequest): Response<ApiResponse<Unit>>  

    @GET("support/faqs")
    suspend fun getFaqs(): Response<ApiResponse<List<FaqDto>>>  

    @GET("support/tickets")
    suspend fun getSupportTickets(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<SupportTicketDto>>>  

    
    @GET("reports/sales")
    suspend fun getSalesReport(
        @Query("shopId") shopId: String? = null,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("format") format: String = "json"
    ): Response<ApiResponse<SalesReportDto>>  

    @GET("reports/inventory")
    suspend fun getInventoryReport(
        @Query("shopId") shopId: String? = null,
        @Query("format") format: String = "json"
    ): Response<ApiResponse<InventoryReportDto>>  

    @GET("reports/finance")
    suspend fun getFinanceReport(
        @Query("shopId") shopId: String? = null,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("format") format: String = "json"
    ): Response<ApiResponse<FinanceReportDto>>  

    @GET("reports/subscription")
    suspend fun getSubscriptionReport(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("format") format: String = "json"
    ): Response<ApiResponse<SubscriptionReportDto>>  

    
    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<ApiResponse<UploadResponse>>  

    @Multipart
    @POST("upload/document")
    suspend fun uploadDocument(@Part document: MultipartBody.Part): Response<ApiResponse<UploadResponse>>  

    
    @GET("notifications")
    suspend fun getNotifications(
        @Query("unreadOnly") unreadOnly: Boolean = false,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<NotificationDto>>>  

    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") notificationId: String): Response<ApiResponse<Unit>>  

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<ApiResponse<Unit>>  

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") notificationId: String): Response<ApiResponse<Unit>>  

    @DELETE("notifications")
    suspend fun deleteAllNotifications(): Response<ApiResponse<Unit>>  

    @GET("notifications/settings")
    suspend fun getNotificationSettings(): Response<ApiResponse<NotificationSettingsDto>>  

    @PUT("notifications/settings")
    suspend fun updateNotificationSettings(@Body request: NotificationSettingsRequest): Response<ApiResponse<Unit>>  

    
    @GET("system/health")
    suspend fun checkSystemHealth(): Response<ApiResponse<SystemHealthDto>>  

    @GET("system/status")
    suspend fun getSystemStatus(): Response<ApiResponse<SystemStatusDto>>  

    @GET("system/version")
    suspend fun getSystemVersion(): Response<ApiResponse<SystemVersionDto>>  
}


