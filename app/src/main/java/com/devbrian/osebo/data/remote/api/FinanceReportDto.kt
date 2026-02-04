package com.devbrian.osebo.data.remote.api


import com.devbrian.osebo.data.remote.dto.request.AddCustomerRequest
import com.devbrian.osebo.data.remote.dto.request.AddEmployeeRequest
import com.devbrian.osebo.data.remote.dto.request.AddInventoryItemRequest
import com.devbrian.osebo.data.remote.dto.request.AddSupplierRequest
import com.devbrian.osebo.data.remote.dto.request.AutoRenewRequest
import com.devbrian.osebo.data.remote.dto.request.ChangePasswordRequest
import com.devbrian.osebo.data.remote.dto.request.CreateSaleRequest
import com.devbrian.osebo.data.remote.dto.request.CreateShopRequest
import com.devbrian.osebo.data.remote.dto.request.CreateSupportTicketRequest
import com.devbrian.osebo.data.remote.dto.request.CreateTransferRequest
import com.devbrian.osebo.data.remote.dto.request.CreateUserRoleRequest
import com.devbrian.osebo.data.remote.dto.request.DowngradeSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.request.ForgotPasswordRequest
import com.devbrian.osebo.data.remote.dto.request.LoginNotificationsRequest
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.request.NotificationSettingsRequest
import com.devbrian.osebo.data.remote.dto.request.RefreshTokenRequest
import com.devbrian.osebo.data.remote.dto.request.RegisterRequest
import com.devbrian.osebo.data.remote.dto.request.ResetPasswordRequest
import com.devbrian.osebo.data.remote.dto.request.SubscribeRequest
import com.devbrian.osebo.data.remote.dto.request.SystemHealthDto
import com.devbrian.osebo.data.remote.dto.request.SystemStatusDto
import com.devbrian.osebo.data.remote.dto.request.SystemVersionDto
import com.devbrian.osebo.data.remote.dto.request.TwoFactorAuthRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateAccountRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateInventoryItemRequest
import com.devbrian.osebo.data.remote.dto.request.UpdatePaymentMethodRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateProfileRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateShopRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateUserRoleRequest
import com.devbrian.osebo.data.remote.dto.request.UpgradeSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.response.AccountDto
import com.devbrian.osebo.data.remote.dto.response.ApiResponse
import com.devbrian.osebo.data.remote.dto.response.AuthResponse
import com.devbrian.osebo.data.remote.dto.response.BillingInfoDto
import com.devbrian.osebo.data.remote.dto.response.ContactInfoDto
import com.devbrian.osebo.data.remote.dto.response.InvoiceDto
import com.devbrian.osebo.data.remote.dto.response.NotificationSettingsDto
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.remote.dto.response.SubscriptionDto
import com.devbrian.osebo.data.remote.dto.response.SubscriptionReportDto
import com.devbrian.osebo.data.remote.dto.response.UserDto
import com.devbrian.osebo.data.remote.dto.response.UserRoleDto
import com.devbrian.osebo.models.SupportMessageRequest
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    // MARK: - Authentication
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse>

    // MARK: - User Management
    @GET("users/profile")
    suspend fun getCurrentUser(): Response<UserDto>

    @PUT("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserDto>

    @Multipart
    @PUT("users/profile/image")
    suspend fun uploadProfileImage(@Part image: MultipartBody.Part): Response<UserDto>

    @DELETE("users/profile/image")
    suspend fun resetProfileImage(): Response<UserDto>

    @PUT("users/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse>

    // MARK: - Dashboard
    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummaryDto>

    @GET("dashboard/analytics")
    suspend fun getDashboardAnalytics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<DashboardAnalyticsDto>

    // MARK: - Shops
    @GET("shops")
    suspend fun getShops(): Response<List<ShopDto>>

    @GET("shops/{id}")
    suspend fun getShopById(@Path("id") shopId: String): Response<ShopDto>

    @POST("shops")
    suspend fun createShop(@Body request: CreateShopRequest): Response<ShopDto>

    @PUT("shops/{id}")
    suspend fun updateShop(@Path("id") shopId: String, @Body request: UpdateShopRequest): Response<ShopDto>

    @DELETE("shops/{id}")
    suspend fun deleteShop(@Path("id") shopId: String): Response<ApiResponse>

    // MARK: - Business Operations
    // Sales
    @GET("sales")
    suspend fun getSales(
        @Query("shopId") shopId: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<SaleDto>>

    @POST("sales")
    suspend fun createSale(@Body request: CreateSaleRequest): Response<SaleDto>

    // Finance
    @GET("finance/summary")
    suspend fun getFinanceSummary(
        @Query("shopId") shopId: String? = null,
        @Query("period") period: String? = "monthly"
    ): Response<FinanceSummaryDto>

    @GET("finance/transactions")
    suspend fun getFinanceTransactions(
        @Query("shopId") shopId: String? = null,
        @Query("type") type: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<TransactionDto>>

    // Inventory
    @GET("inventory")
    suspend fun getInventory(
        @Query("shopId") shopId: String? = null,
        @Query("category") category: String? = null
    ): Response<List<InventoryItemDto>>

    @POST("inventory")
    suspend fun addInventoryItem(@Body request: AddInventoryItemRequest): Response<InventoryItemDto>

    @PUT("inventory/{id}")
    suspend fun updateInventoryItem(
        @Path("id") itemId: String,
        @Body request: UpdateInventoryItemRequest
    ): Response<InventoryItemDto>

    // Transfers
    @GET("transfers")
    suspend fun getTransfers(
        @Query("shopId") shopId: String? = null,
        @Query("status") status: String? = null
    ): Response<List<TransferDto>>

    @POST("transfers")
    suspend fun createTransfer(@Body request: CreateTransferRequest): Response<TransferDto>

    // MARK: - Management
    // Employees
    @GET("employees")
    suspend fun getEmployees(@Query("shopId") shopId: String? = null): Response<List<EmployeeDto>>

    @POST("employees")
    suspend fun addEmployee(@Body request: AddEmployeeRequest): Response<EmployeeDto>

    // Customers
    @GET("customers")
    suspend fun getCustomers(
        @Query("shopId") shopId: String? = null,
        @Query("search") searchQuery: String? = null
    ): Response<List<CustomerDto>>

    @POST("customers")
    suspend fun addCustomer(@Body request: AddCustomerRequest): Response<CustomerDto>

    // Suppliers
    @GET("suppliers")
    suspend fun getSuppliers(@Query("shopId") shopId: String? = null): Response<List<SupplierDto>>

    @POST("suppliers")
    suspend fun addSupplier(@Body request: AddSupplierRequest): Response<SupplierDto>

    // MARK: - Subscription & Billing
    @GET("subscription/current")
    suspend fun getCurrentSubscription(): Response<SubscriptionDto>

    @GET("subscription/plans")
    suspend fun getSubscriptionPlans(): Response<List<SubscriptionPlanDto>>

    @POST("subscription/subscribe")
    suspend fun subscribeToPlan(@Body request: SubscribeRequest): Response<SubscriptionDto>

    @DELETE("subscription/cancel")
    suspend fun cancelSubscription(): Response<ApiResponse>

    @GET("subscription/history")
    suspend fun getSubscriptionHistory(): Response<List<SubscriptionHistoryDto>>

    @PUT("subscription/auto-renew")
    suspend fun updateAutoRenew(@Body request: AutoRenewRequest): Response<ApiResponse>

    @GET("subscription/invoices")
    suspend fun getInvoices(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<InvoiceDto>>

    @GET("subscription/invoices/{id}")
    suspend fun getInvoiceById(@Path("id") invoiceId: String): Response<InvoiceDto>

    @POST("subscription/upgrade")
    suspend fun upgradeSubscription(@Body request: UpgradeSubscriptionRequest): Response<SubscriptionDto>

    @POST("subscription/downgrade")
    suspend fun downgradeSubscription(@Body request: DowngradeSubscriptionRequest): Response<SubscriptionDto>

    @POST("subscription/payment-method")
    suspend fun updatePaymentMethod(@Body request: UpdatePaymentMethodRequest): Response<ApiResponse>

    @GET("subscription/billing-info")
    suspend fun getBillingInfo(): Response<BillingInfoDto>

    // MARK: - User Roles & Permissions
    @GET("user-roles")
    suspend fun getUserRoles(): Response<List<UserRoleDto>>

    @POST("user-roles")
    suspend fun createUserRole(@Body request: CreateUserRoleRequest): Response<UserRoleDto>

    @PUT("user-roles/{id}")
    suspend fun updateUserRole(
        @Path("id") roleId: String,
        @Body request: UpdateUserRoleRequest
    ): Response<UserRoleDto>

    @DELETE("user-roles/{id}")
    suspend fun deleteUserRole(@Path("id") roleId: String): Response<ApiResponse>

    // MARK: - Account Settings
    @GET("account")
    suspend fun getAccountDetails(): Response<AccountDto>

    @PUT("account")
    suspend fun updateAccount(@Body request: UpdateAccountRequest): Response<AccountDto>

    @PUT("account/two-factor")
    suspend fun updateTwoFactorAuth(@Body request: TwoFactorAuthRequest): Response<AccountDto>

    @PUT("account/notifications")
    suspend fun updateLoginNotifications(@Body request: LoginNotificationsRequest): Response<AccountDto>

    @POST("account/deactivate")
    suspend fun deactivateAccount(): Response<ApiResponse>

    @DELETE("account")
    suspend fun deleteAccount(): Response<ApiResponse>

    @GET("account/sessions")
    suspend fun getActiveSessions(): Response<List<SessionDto>>

    @DELETE("account/sessions/{id}")
    suspend fun terminateSession(@Path("id") sessionId: String): Response<ApiResponse>

    // MARK: - Contact Us & Support
    @GET("contact")
    suspend fun getContactInfo(): Response<ContactInfoDto>

    @POST("support/tickets")
    suspend fun createSupportTicket(@Body request: CreateSupportTicketRequest): Response<SupportTicketDto>

    @POST("support/messages")
    suspend fun sendSupportMessage(@Body request: SupportMessageRequest): Response<ApiResponse>

    @GET("support/faqs")
    suspend fun getFaqs(): Response<List<FaqDto>>

    @GET("support/tickets")
    suspend fun getSupportTickets(
        @Query("status") status: String? = null
    ): Response<List<SupportTicketDto>>

    // MARK: - Reports
    @GET("reports/sales")
    suspend fun getSalesReport(
        @Query("shopId") shopId: String? = null,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("format") format: String = "json"
    ): Response<SalesReportDto>

    @GET("reports/inventory")
    suspend fun getInventoryReport(
        @Query("shopId") shopId: String? = null,
        @Query("format") format: String = "json"
    ): Response<InventoryReportDto>

    @GET("reports/finance")
    suspend fun getFinanceReport(
        @Query("shopId") shopId: String? = null,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("format") format: String = "json"
    ): Response<FinanceReportDto>

    @GET("reports/subscription")
    suspend fun getSubscriptionReport(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("format") format: String = "json"
    ): Response<SubscriptionReportDto>

    // MARK: - Uploads
    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<UploadResponse>

    @Multipart
    @POST("upload/document")
    suspend fun uploadDocument(@Part document: MultipartBody.Part): Response<UploadResponse>

    // MARK: - Notifications
    @GET("notifications")
    suspend fun getNotifications(
        @Query("unreadOnly") unreadOnly: Boolean = false,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<NotificationDto>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") notificationId: String): Response<ApiResponse>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<ApiResponse>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") notificationId: String): Response<ApiResponse>

    @DELETE("notifications")
    suspend fun deleteAllNotifications(): Response<ApiResponse>

    @GET("notifications/settings")
    suspend fun getNotificationSettings(): Response<NotificationSettingsDto>

    @PUT("notifications/settings")
    suspend fun updateNotificationSettings(@Body request: NotificationSettingsRequest): Response<ApiResponse>

    // MARK: - System & Health
    @GET("system/health")
    suspend fun checkSystemHealth(): Response<SystemHealthDto>

    @GET("system/status")
    suspend fun getSystemStatus(): Response<SystemStatusDto>

    @GET("system/version")
    suspend fun getSystemVersion(): Response<SystemVersionDto>
}