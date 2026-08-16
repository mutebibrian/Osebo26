package com.devbrian.osebo.data

import com.devbrian.osebo.data.remote.api.FaqDto
import com.devbrian.osebo.data.remote.api.FinanceReportDto
import com.devbrian.osebo.data.remote.api.InventoryReportDto
import com.devbrian.osebo.data.remote.api.NotificationDto
import com.devbrian.osebo.data.remote.api.SalesReportDto
import com.devbrian.osebo.data.remote.api.SupportTicketDto
import com.devbrian.osebo.data.remote.dto.request.*
import com.devbrian.osebo.data.remote.dto.request.UpdateCustomerRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateProductRequest
import com.devbrian.osebo.data.remote.dto.response.*
import com.devbrian.osebo.models.ApiResponse
import com.devbrian.osebo.models.CreateEmployeeResponse
import com.devbrian.osebo.models.CreateRoleRequest
import com.devbrian.osebo.models.EmployeeResponse
import com.devbrian.osebo.models.Permission
import com.devbrian.osebo.models.RenewSubscriptionRequest
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.data.models.UserRole
import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.request.RegisterRequest
import com.devbrian.osebo.data.remote.dto.response.PaymentDto
import com.devbrian.osebo.models.Expense
import com.devbrian.osebo.models.ExpenseCategory
import com.devbrian.osebo.models.FinancialStatement
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.TimeSeriesApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ENDPOINTS ====================

    @POST("api/v1/auth/signin")
    suspend fun signIn(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/verify-2fa")
    suspend fun verifyTwoFactor(@Body request: VerifyTwoFactorRequest): Response<AuthResponse>

    // Not confirmed against real backend — no "api/" prefix originally, left as-is
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: String): Response<AuthResponse>

    @GET("api/v1/shop-type")
    suspend fun getShopTypes(): Response<BaseResponse<List<ShopType>>>

    @POST("api/v1/shops")
    suspend fun createShop(@Body request: CreateShopRequest): Response<BaseResponse<Shop>>

    // Not confirmed — no "api/" prefix originally, left as-is
    @POST("auth/signout")
    suspend fun signOut(@Body request: String): Response<ApiResponse<Unit>>

    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/resend-verification")
    suspend fun resendVerificationCode(@Query("email") email: String): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): Response<AuthResponse>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    @POST("api/v1/auth/send-otp")
    suspend fun generateOtp(@Body request: Map<String, String>): Response<BaseResponse<Unit>>

    @POST("api/v1/auth/resend-otp")
    suspend fun sendOtp(@Body request: Map<String, String>): Response<BaseResponse<Unit>>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: Map<String, String>): Response<BaseResponse<Unit>>

    @POST("api/v1/auth/phone-password-reset")
    suspend fun getUserIdByPhone(@Body request: Map<String, String>): Response<BaseResponse<ResetResponse>>

    // ==================== USER ENDPOINTS ====================

    // Not confirmed — no "api/" prefix originally, left as-is
    @GET("users/profile")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    @PUT("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserDto>>

    @PUT("users/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    @GET("api/v1/account")
    suspend fun getAccountDetails(@Header("Authorization") token: String): Response<ApiResponse<AccountDto>>

    @PUT("api/v1/account/two-factor")
    suspend fun updateTwoFactorAuth(
        @Header("Authorization") token: String,
        @Body request: TwoFactorAuthRequest
    ): Response<ApiResponse<AccountDto>>

    @PUT("api/v1/account/login-notifications")
    suspend fun updateLoginNotifications(
        @Header("Authorization") token: String,
        @Body request: LoginNotificationsRequest
    ): Response<ApiResponse<AccountDto>>

    @POST("api/v1/account/deactivate")
    suspend fun deactivateAccount(@Header("Authorization") token: String): Response<ApiResponse<Unit>>

    @DELETE("api/v1/account")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<ApiResponse<Unit>>

    // ==================== EMPLOYEE ENDPOINTS ====================

    @POST("api/v1/users")
    suspend fun createEmployee(
        @Header("Authorization") token: String,
        @Body employee: EmployeeRequest
    ): Response<CreateEmployeeResponse>

    @GET("api/v1/users")
    suspend fun getEmployees(@Header("Authorization") token: String): Response<EmployeeResponse>

    @PUT("api/v1/users/{userId}")
    suspend fun updateEmployee(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body employee: EmployeeRequest
    ): Response<EmployeeResponse>

    @DELETE("api/v1/users/{userId}")
    suspend fun deleteEmployee(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Any>>

    // ==================== ROLE & PERMISSION ENDPOINTS ====================

    // Not confirmed — no "api/" prefix originally, left as-is
    @GET("roles")
    suspend fun getRoles(
        @Header("Authorization") token: String,
        @Query("shopId") shopId: String? = null
    ): Response<ApiResponse<List<UserRole>>>

    @POST("roles")
    suspend fun createRole(
        @Header("Authorization") token: String,
        @Body request: CreateRoleRequest
    ): Response<ApiResponse<UserRole>>

    @PUT("roles/{id}")
    suspend fun updateRole(
        @Header("Authorization") token: String,
        @Path("id") roleId: String,
        @Body request: CreateRoleRequest
    ): Response<ApiResponse<UserRole>>

    @GET("api/v1/permissions")
    suspend fun getPermissions(@Header("Authorization") token: String): Response<ApiResponse<List<Permission>>>

    // ==================== SHOP ENDPOINTS ====================

    @GET("api/v1/shops")
    suspend fun getShops(): Response<ApiResponse<List<ShopDto>>>

    @GET("api/v1/shops")
    suspend fun getShopsWithAuth(@Header("Authorization") token: String): Response<ApiResponse<List<ShopDto>>>

    // Not confirmed — no "api/" prefix originally, left as-is
    @DELETE("shops/{id}")
    suspend fun deleteShop(@Header("Authorization") token: String, @Path("id") shopId: String): Response<ApiResponse<Unit>>

    @PUT("shops/{id}")
    suspend fun updateShop(@Path("id") shopId: String, @Body request: UpdateShopRequest): Response<ApiResponse<ShopDto>>

    @GET("api/v1/shops/{shopId}/settings")
    suspend fun getFinanceSettings(
        @Header("X-Shop") shopId: String,
        @Path("shopId") shopIdPath: String
    ): Response<ApiResponse<FinanceSettingsDto>>

    @PUT("api/v1/shops/{shopId}/settings")
    suspend fun updateFinanceSettings(
        @Header("X-Shop") shopId: String,
        @Path("shopId") shopIdPath: String,
        @Body request: UpdateFinanceSettingsRequest
    ): Response<ApiResponse<FinanceSettingsDto>>

    // ==================== PRODUCT ENDPOINTS ====================

    @GET("api/v1/stock-item")
    suspend fun getProducts(@Header("X-Shop") shopUuid: String): Response<ApiResponse<List<ProductDto>>>

    @Multipart
    @POST("api/v1/stock-item/single")
    suspend fun createProduct(
        @Header("X-Shop") shopId: String,
        @Part("name") name: RequestBody,
        @Part("sku") sku: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("low_quantity_mark") lowQuantityMark: RequestBody,
        @Part("purchase_price") purchasePrice: RequestBody,
        @Part("selling_price") sellingPrice: RequestBody,
        @Part("max_discount") maxDiscount: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part("unit_measure") unitMeasure: RequestBody,
        @Part("stock_category_id") stockCategoryId: RequestBody,
        @Part photo: MultipartBody.Part? = null
    ): Response<ApiResponse<ProductDto>>

    @PUT("api/v1/stock/{productId}")
    suspend fun updateProduct(
        @Header("X-Shop") shopId: String,
        @Path("productId") productId: String,
        @Body request: UpdateProductRequest
    ): Response<ApiResponse<ProductDto>>

    @DELETE("api/v1/stock/{productId}")
    suspend fun deleteProduct(
        @Header("X-Shop") shopId: String,
        @Path("productId") productId: String
    ): Response<ApiResponse<Unit>>

    @GET("api/v1/stock-category")
    suspend fun getCategories(@Header("X-Shop") shopId: String): Response<ApiResponse<List<StockCategoryDto>>>

    @GET("api/v1/stock-item/search/barcode")
    suspend fun searchProductByBarcode(
        @Header("Authorization") token: String,
        @Header("X-Shop") shopId: String,
        @Query("barcode") barcode: String
    ): Response<ApiResponse<Product>>

    @GET("api/v1/stock-item/search")
    suspend fun searchProducts(
        @Header("Authorization") token: String,
        @Header("X-Shop") shopId: String,
        @Query("q") query: String
    ): Response<ApiResponse<List<Product>>>

    // ==================== CUSTOMER ENDPOINTS ====================

    @GET("api/v1/customer")
    suspend fun getCustomers(@Header("X-Shop") shopId: String): Response<ApiResponse<List<CustomerDto>>>

    @GET("api/v1/customer/{customerId}")
    suspend fun getCustomer(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String
    ): Response<ApiResponse<CustomerDto>>

    @POST("api/v1/customer")
    suspend fun createCustomer(
        @Header("X-Shop") shopId: String,
        @Body request: CreateCustomerRequest
    ): Response<ApiResponse<CustomerDto>>

    @PUT("api/v1/customer/{customerId}")
    suspend fun updateCustomer(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String,
        @Body request: UpdateCustomerRequest
    ): Response<ApiResponse<CustomerDto>>

    @DELETE("api/v1/customer/{customerId}")
    suspend fun deleteCustomer(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String
    ): Response<ApiResponse<Unit>>

    // ==================== SALE ENDPOINTS ====================

    @POST("api/v1/sale")
    suspend fun createSale(
        @Header("X-Shop") shopId: String,
        @Body request: SaleRequest
    ): Response<SaleApiResponse>

    @GET("api/v1/sale/{saleId}")
    suspend fun getSale(
        @Header("X-Shop") shopId: String,
        @Path("saleId") saleId: String
    ): Response<SaleApiResponse>

    @GET("api/v1/sale/customer/{customerId}")
    suspend fun getCustomerSales(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String,
        @Query("type") type: String? = null
    ): Response<SaleListApiResponse>

    @GET("api/v1/sale/customer-sale-history/{customerId}")
    suspend fun getCustomerSaleHistory(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String
    ): Response<SaleListApiResponse>

    @POST("api/v1/sale/record-payment")
    suspend fun recordPayment(
        @Header("x-shop-id") shopId: String,
        @Body request: PaymentRequest
    ): Response<ApiResponse<PaymentData>>

    // ==================== EXPENSE ENDPOINTS ====================

    @GET("api/v1/expense")
    suspend fun getExpenses(@Header("X-Shop") shopId: String): Response<ApiResponse<List<Expense>>>

    @GET("api/v1/expense")
    suspend fun getAllExpenses(
        @Header("X-Shop") shopId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<List<Expense>>>

    @POST("api/v1/expense")
    suspend fun createExpense(
        @Header("X-Shop") shopId: String,
        @Body request: CreateExpenseRequest
    ): Response<ApiResponse<Expense>>

    @GET("api/v1/expense/{expenseId}")
    suspend fun getExpense(
        @Header("X-Shop") shopId: String,
        @Path("expenseId") expenseId: String
    ): Response<ApiResponse<Expense>>

    @PUT("api/v1/expense/{expenseId}")
    suspend fun updateExpense(
        @Header("X-Shop") shopId: String,
        @Path("expenseId") expenseId: String,
        @Body request: UpdateExpenseRequest
    ): Response<ApiResponse<Expense>>

    @DELETE("api/v1/expense/{expenseId}")
    suspend fun deleteExpense(
        @Header("X-Shop") shopId: String,
        @Path("expenseId") expenseId: String
    ): Response<ApiResponse<Unit>>

    @GET("api/v1/expense-category")
    suspend fun getExpenseCategories(@Header("X-Shop") shopId: String): Response<ApiResponse<List<ExpenseCategory>>>

    @POST("api/v1/expense-category")
    suspend fun createExpenseCategory(
        @Header("X-Shop") shopId: String,
        @Body request: CreateExpenseCategoryRequest
    ): Response<ApiResponse<ExpenseCategory>>

    @PUT("api/v1/expense-category/{categoryId}")
    suspend fun updateExpenseCategory(
        @Header("X-Shop") shopId: String,
        @Path("categoryId") categoryId: String,
        @Body request: UpdateExpenseCategoryRequest
    ): Response<ApiResponse<ExpenseCategory>>

    @DELETE("api/v1/expense-category/{categoryId}")
    suspend fun deleteExpenseCategory(
        @Header("X-Shop") shopId: String,
        @Path("categoryId") categoryId: String
    ): Response<ApiResponse<Unit>>

    // ==================== ANALYTICS & REPORT ENDPOINTS ====================

    @GET("api/v1/analytics/top-stock-items")
    suspend fun getTopStockItems(@Header("X-Shop") shopId: String): Response<ApiResponse<TopStockItemsDto>>

    @GET("api/v1/analytics/shop-financial-statement")
    suspend fun getFinancialStatement(
        @Header("X-Shop") shopId: String,
        @Query("range") range: String = "yearly"
    ): Response<ApiResponse<FinancialStatementDto>>

    @GET("api/v1/analytics/sales-comparison")
    suspend fun getSalesComparison(
        @Header("X-Shop") shopId: String,
        @Query("period") period: String = "monthly"
    ): Response<ApiResponse<SalesComparisonDto>>

    @GET("api/v1/analytics/shop-summary")
    suspend fun getShopSummary(@Header("X-Shop") shopId: String): Response<ApiResponse<ShopSummaryDto>>

    @GET("api/v1/analytics/totals")
    suspend fun getShopTotals(@Header("X-Shop") shopId: String): Response<ApiResponse<ShopTotalsDto>>

    @GET("api/v1/analytics/time-series")
    suspend fun getTimeSeries(
        @Header("X-Shop") shopId: String,
        @Query("range") range: String = "monthly"
    ): Response<ApiResponse<TimeSeriesApiResponse>>

    // Not confirmed — no "api/" prefix originally, left as-is
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

    // ==================== LEDGER ENDPOINTS ====================

    @GET("api/v1/general-ledger")
    suspend fun getGeneralLedger(
        @Header("X-Shop") shopId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("period") period: String = "monthly"
    ): Response<ApiResponse<FinancialStatement>>

    @GET("api/v1/general-ledger/customer/{customerId}")
    suspend fun getCustomerLedger(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String
    ): Response<ApiResponse<FinancialStatement>>

    // ==================== SUBSCRIPTION & PAYMENT ENDPOINTS ====================

    @GET("api/v1/package")
    suspend fun getSubscriptionPackages(): Response<ApiResponse<List<PackageDto>>>

    @POST("api/v1/subscription")
    suspend fun createSubscription(
        @Header("X-Shop") shopId: String,
        @Body request: CreateSubscriptionRequest
    ): Response<ApiResponse<SubscriptionResponse>>

    @POST("api/v1/subscription/renew")
    suspend fun renewSubscription(
        @Header("X-Shop") shopId: String,
        @Body request: RenewSubscriptionRequest
    ): Response<ApiResponse<SubscriptionResponse>>

    @PATCH("api/v1/subscription/{subscriptionId}/cancel")
    suspend fun cancelSubscription(
        @Header("X-Shop") shopId: String,
        @Path("subscriptionId") subscriptionId: String
    ): Response<ApiResponse<Unit>>

    @DELETE("api/v1/subscription/{subscriptionId}/packages/{packageId}")
    suspend fun cancelPackage(
        @Header("X-Shop") shopId: String,
        @Path("subscriptionId") subscriptionId: String,
        @Path("packageId") packageId: String
    ): Response<ApiResponse<Unit>>

    @GET("api/v1/subscription/shop/active")
    suspend fun checkShopSubscription(
        @Header("X-Shop") shopId: String
    ): Response<ApiResponse<ShopSubscriptionStatusResponse>>

    @GET("api/v1/subscription/shop")
    suspend fun getShopSubscriptions(
        @Header("X-Shop") shopId: String
    ): Response<ApiResponse<List<Subscription>>>

    @GET("api/v1/subscription/payments")
    suspend fun getSubscriptionPayments(
        @Header("X-Shop") shopId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("provider") provider: String? = null
    ): Response<ApiResponse<List<PaymentDto>>>

    @GET("api/v1/subscription/user/shops")
    suspend fun getUserShopsSubscriptions(): Response<ApiResponse<List<Subscription>>>

    @GET("api/v1/subscription/{subscriptionId}")
    suspend fun getSubscriptionDetails(
        @Path("subscriptionId") subscriptionId: String
    ): Response<ApiResponse<Subscription>>

    @GET("api/v1/subscription/payment/{paymentId}")
    suspend fun findSubscriptionByPaymentId(
        @Path("paymentId") paymentId: String
    ): Response<ApiResponse<PaymentCheckResponse>>

    // ==================== NOTIFICATION ENDPOINTS ====================

    // Not confirmed — no "api/" prefix originally, left as-is
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

    // ==================== SUPPORT ENDPOINTS ====================

    // Not confirmed — no "api/" prefix originally, left as-is
    @POST("support/tickets")
    suspend fun createSupportTicket(@Body request: CreateSupportTicketRequest): Response<ApiResponse<SupportTicketDto>>

    @GET("support/faqs")
    suspend fun getFaqs(): Response<ApiResponse<List<FaqDto>>>

    @POST("api/v1/auth/select-account")
    suspend fun selectAccount(@Body request: SelectAccountRequest): Response<AuthResponse>

    @POST("api/v1/auth/switch-account")
    suspend fun switchAccount(@Header("Authorization") token: String, @Body request: SwitchAccountRequest): Response<AuthResponse>

    // Not confirmed — no "api/" prefix originally, left as-is
    @POST("auth/accounts")
    suspend fun getAccountsByPhone(@Body request: AccountsByPhoneRequest): Response<ApiResponse<List<AccountInfo>>>
}