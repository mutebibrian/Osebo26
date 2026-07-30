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

    @POST("api/auth/signin")
    suspend fun signIn(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/verify-2fa")
    suspend fun verifyTwoFactor(@Body request: VerifyTwoFactorRequest): Response<AuthResponse>

    // Fix 1: Refresh token should accept RefreshTokenRequest object
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: String): Response<AuthResponse>

    @GET("api/shop-type")
    suspend fun getShopTypes(): Response<BaseResponse<List<ShopType>>>

    @POST("api/shops")
    suspend fun createShop(@Body request: CreateShopRequest): Response<BaseResponse<Shop>>

    // Fix 2: Sign out should accept RefreshTokenRequest object
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

    @POST("api/auth/resend-verification")
    suspend fun resendVerificationCode(@Query("email") email: String): Response<ApiResponse<Unit>>

    @POST("api/auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): Response<AuthResponse>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    @POST("api/auth/send-otp")   // change to the correct path
    suspend fun generateOtp(@Body request: Map<String, String>): Response<BaseResponse<Unit>>

    // OTP endpoints (add these)
    @POST("api/auth/resend-otp")
    suspend fun sendOtp(@Body request: Map<String, String>): Response<BaseResponse<Unit>>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: Map<String, String>): Response<BaseResponse<Unit>>

    // Only if your backend requires a phone → userId step:
    @POST("api/auth/phone-password-reset")
    suspend fun getUserIdByPhone(@Body request: Map<String, String>): Response<BaseResponse<ResetResponse>>

    // ==================== USER ENDPOINTS ====================

    @GET("users/profile")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    @PUT("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserDto>>

    @PUT("users/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    @GET("api/account")
    suspend fun getAccountDetails(@Header("Authorization") token: String): Response<ApiResponse<AccountDto>>

    @PUT("api/account/two-factor")
    suspend fun updateTwoFactorAuth(
        @Header("Authorization") token: String,
        @Body request: TwoFactorAuthRequest
    ): Response<ApiResponse<AccountDto>>

    @PUT("api/account/login-notifications")
    suspend fun updateLoginNotifications(
        @Header("Authorization") token: String,
        @Body request: LoginNotificationsRequest
    ): Response<ApiResponse<AccountDto>>

    @POST("api/account/deactivate")
    suspend fun deactivateAccount(@Header("Authorization") token: String): Response<ApiResponse<Unit>>

    @DELETE("api/account")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<ApiResponse<Unit>>

    // ==================== EMPLOYEE ENDPOINTS ====================

    @POST("api/users")
    suspend fun createEmployee(
        @Header("Authorization") token: String,
        @Body employee: EmployeeRequest
    ): Response<CreateEmployeeResponse>

    @GET("api/users")
    suspend fun getEmployees(@Header("Authorization") token: String): Response<EmployeeResponse>

    @PUT("api/users/{userId}")
    suspend fun updateEmployee(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body employee: EmployeeRequest
    ): Response<EmployeeResponse>

    @DELETE("api/users/{userId}")
    suspend fun deleteEmployee(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Any>>

    // ==================== ROLE & PERMISSION ENDPOINTS ====================

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

    @GET("api/permissions")
    suspend fun getPermissions(@Header("Authorization") token: String): Response<ApiResponse<List<Permission>>>

    // ==================== SHOP ENDPOINTS ====================

    @GET("api/shops")
    suspend fun getShops(): Response<ApiResponse<List<ShopDto>>>

    @GET("api/shops")
    suspend fun getShopsWithAuth(@Header("Authorization") token: String): Response<ApiResponse<List<ShopDto>>>

    @DELETE("shops/{id}")
    suspend fun deleteShop(@Header("Authorization") token: String, @Path("id") shopId: String): Response<ApiResponse<Unit>>

    @PUT("shops/{id}")
    suspend fun updateShop(@Path("id") shopId: String, @Body request: UpdateShopRequest): Response<ApiResponse<ShopDto>>

    @GET("api/shops/{shopId}/settings")
    suspend fun getFinanceSettings(
        @Header("X-Shop") shopId: String,
        @Path("shopId") shopIdPath: String
    ): Response<ApiResponse<FinanceSettingsDto>>

    @PUT("api/shops/{shopId}/settings")
    suspend fun updateFinanceSettings(
        @Header("X-Shop") shopId: String,
        @Path("shopId") shopIdPath: String,
        @Body request: UpdateFinanceSettingsRequest
    ): Response<ApiResponse<FinanceSettingsDto>>

    // ==================== PRODUCT ENDPOINTS ====================

    @GET("api/stock-item")
    suspend fun getProducts(@Header("X-Shop") shopUuid: String): Response<ApiResponse<List<ProductDto>>>

    @Multipart
    @POST("api/stock-item/single")
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

    @PUT("api/stock/{productId}")
    suspend fun updateProduct(
        @Header("X-Shop") shopId: String,
        @Path("productId") productId: String,
        @Body request: UpdateProductRequest
    ): Response<ApiResponse<ProductDto>>

    @DELETE("api/stock/{productId}")
    suspend fun deleteProduct(
        @Header("X-Shop") shopId: String,
        @Path("productId") productId: String
    ): Response<ApiResponse<Unit>>

    @GET("api/stock-category")
    suspend fun getCategories(@Header("X-Shop") shopId: String): Response<ApiResponse<List<StockCategoryDto>>>

    @GET("api/stock-item/search/barcode")
    suspend fun searchProductByBarcode(
        @Header("Authorization") token: String,
        @Header("X-Shop") shopId: String,
        @Query("barcode") barcode: String
    ): Response<ApiResponse<Product>>

    @GET("api/stock-item/search")
    suspend fun searchProducts(
        @Header("Authorization") token: String,
        @Header("X-Shop") shopId: String,
        @Query("q") query: String
    ): Response<ApiResponse<List<Product>>>

    // ==================== CUSTOMER ENDPOINTS ====================

    @GET("api/customer")
    suspend fun getCustomers(@Header("X-Shop") shopId: String): Response<ApiResponse<List<CustomerDto>>>

    @GET("api/customer/{customerId}")
    suspend fun getCustomer(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String
    ): Response<ApiResponse<CustomerDto>>

    @POST("api/customer")
    suspend fun createCustomer(
        @Header("X-Shop") shopId: String,
        @Body request: CreateCustomerRequest
    ): Response<ApiResponse<CustomerDto>>

    @PUT("api/customer/{customerId}")
    suspend fun updateCustomer(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String,
        @Body request: UpdateCustomerRequest
    ): Response<ApiResponse<CustomerDto>>

    @DELETE("api/customer/{customerId}")
    suspend fun deleteCustomer(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String
    ): Response<ApiResponse<Unit>>

    // ==================== SALE ENDPOINTS ====================

    @POST("api/sale")
    suspend fun createSale(
        @Header("X-Shop") shopId: String,
        @Body request: SaleRequest
    ): Response<SaleApiResponse>

    @GET("api/sale/{saleId}")
    suspend fun getSale(
        @Header("X-Shop") shopId: String,
        @Path("saleId") saleId: String
    ): Response<SaleApiResponse>

    @GET("api/sale/customer/{customerId}")
    suspend fun getCustomerSales(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String,
        @Query("type") type: String? = null
    ): Response<SaleListApiResponse>

    @GET("api/sale/customer-sale-history/{customerId}")
    suspend fun getCustomerSaleHistory(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String
    ): Response<SaleListApiResponse>

    @POST("api/sale/record-payment")
    suspend fun recordPayment(
        @Header("x-shop-id") shopId: String,
        @Body request: PaymentRequest
    ): Response<ApiResponse<PaymentData>>

    // ==================== EXPENSE ENDPOINTS ====================

    @GET("api/expense")
    suspend fun getExpenses(@Header("X-Shop") shopId: String): Response<ApiResponse<List<Expense>>>

    @GET("api/expense")
    suspend fun getAllExpenses(
        @Header("X-Shop") shopId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<List<Expense>>>

    @POST("api/expense")
    suspend fun createExpense(
        @Header("X-Shop") shopId: String,
        @Body request: CreateExpenseRequest
    ): Response<ApiResponse<Expense>>

    @GET("api/expense/{expenseId}")
    suspend fun getExpense(
        @Header("X-Shop") shopId: String,
        @Path("expenseId") expenseId: String
    ): Response<ApiResponse<Expense>>

    @PUT("api/expense/{expenseId}")
    suspend fun updateExpense(
        @Header("X-Shop") shopId: String,
        @Path("expenseId") expenseId: String,
        @Body request: UpdateExpenseRequest
    ): Response<ApiResponse<Expense>>

    @DELETE("api/expense/{expenseId}")
    suspend fun deleteExpense(
        @Header("X-Shop") shopId: String,
        @Path("expenseId") expenseId: String
    ): Response<ApiResponse<Unit>>

    @GET("api/expense-category")
    suspend fun getExpenseCategories(@Header("X-Shop") shopId: String): Response<ApiResponse<List<ExpenseCategory>>>

    @POST("api/expense-category")
    suspend fun createExpenseCategory(
        @Header("X-Shop") shopId: String,
        @Body request: CreateExpenseCategoryRequest
    ): Response<ApiResponse<ExpenseCategory>>

    @PUT("api/expense-category/{categoryId}")
    suspend fun updateExpenseCategory(
        @Header("X-Shop") shopId: String,
        @Path("categoryId") categoryId: String,
        @Body request: UpdateExpenseCategoryRequest
    ): Response<ApiResponse<ExpenseCategory>>

    @DELETE("api/expense-category/{categoryId}")
    suspend fun deleteExpenseCategory(
        @Header("X-Shop") shopId: String,
        @Path("categoryId") categoryId: String
    ): Response<ApiResponse<Unit>>

    // ==================== ANALYTICS & REPORT ENDPOINTS ====================

    @GET("api/analytics/top-stock-items")
    suspend fun getTopStockItems(@Header("X-Shop") shopId: String): Response<ApiResponse<TopStockItemsDto>>

    @GET("api/analytics/shop-financial-statement")
    suspend fun getFinancialStatement(
        @Header("X-Shop") shopId: String,
        @Query("range") range: String = "yearly"
    ): Response<ApiResponse<FinancialStatementDto>>

    @GET("api/analytics/sales-comparison")
    suspend fun getSalesComparison(
        @Header("X-Shop") shopId: String,
        @Query("period") period: String = "monthly"
    ): Response<ApiResponse<SalesComparisonDto>>

    @GET("api/analytics/shop-summary")
    suspend fun getShopSummary(@Header("X-Shop") shopId: String): Response<ApiResponse<ShopSummaryDto>>

    @GET("api/analytics/totals")
    suspend fun getShopTotals(@Header("X-Shop") shopId: String): Response<ApiResponse<ShopTotalsDto>>

    @GET("api/analytics/time-series")
    suspend fun getTimeSeries(
        @Header("X-Shop") shopId: String,
        @Query("range") range: String = "monthly"
    ): Response<ApiResponse<TimeSeriesApiResponse>>

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

    @GET("api/general-ledger")
    suspend fun getGeneralLedger(
        @Header("X-Shop") shopId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("period") period: String = "monthly"
    ): Response<ApiResponse<FinancialStatement>>

    @GET("api/general-ledger/customer/{customerId}")
    suspend fun getCustomerLedger(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String
    ): Response<ApiResponse<FinancialStatement>>

    // ==================== SUBSCRIPTION & PAYMENT ENDPOINTS (updated to match v1 backend) ====================

    @GET("api/package")
    suspend fun getSubscriptionPackages(): Response<ApiResponse<List<PackageDto>>>

    @POST("api/subscription")
    suspend fun createSubscription(
        @Header("X-Shop") shopId: String,
        @Body request: CreateSubscriptionRequest
    ): Response<ApiResponse<SubscriptionResponse>>

    @POST("api/subscription/renew")
    suspend fun renewSubscription(
        @Header("X-Shop") shopId: String,
        @Body request: RenewSubscriptionRequest
    ): Response<ApiResponse<SubscriptionResponse>>

    @PATCH("api/subscription/{subscriptionId}/cancel")
    suspend fun cancelSubscription(
        @Header("X-Shop") shopId: String,
        @Path("subscriptionId") subscriptionId: String
    ): Response<ApiResponse<Unit>>

    @DELETE("api/subscription/{subscriptionId}/packages/{packageId}")
    suspend fun cancelPackage(
        @Header("X-Shop") shopId: String,
        @Path("subscriptionId") subscriptionId: String,
        @Path("packageId") packageId: String
    ): Response<ApiResponse<Unit>>

    @GET("api/subscription/shop/active")
    suspend fun checkShopSubscription(
        @Header("X-Shop") shopId: String
    ): Response<ApiResponse<ShopSubscriptionStatusResponse>>

    @GET("api/subscription/shop")
    suspend fun getShopSubscriptions(
        @Header("X-Shop") shopId: String
    ): Response<ApiResponse<List<Subscription>>>

    @GET("api/subscription/payments")
    suspend fun getSubscriptionPayments(
        @Header("X-Shop") shopId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("provider") provider: String? = null
    ): Response<ApiResponse<List<PaymentDto>>>

    @GET("api/subscription/user/shops")
    suspend fun getUserShopsSubscriptions(): Response<ApiResponse<List<Subscription>>>

    @GET("api/subscription/{subscriptionId}")
    suspend fun getSubscriptionDetails(
        @Path("subscriptionId") subscriptionId: String
    ): Response<ApiResponse<Subscription>>

    @GET("api/subscription/payment/{paymentId}")
    suspend fun findSubscriptionByPaymentId(
        @Path("paymentId") paymentId: String
    ): Response<ApiResponse<Subscription>>

    // ==================== NOTIFICATION ENDPOINTS ====================

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

    @POST("support/tickets")
    suspend fun createSupportTicket(@Body request: CreateSupportTicketRequest): Response<ApiResponse<SupportTicketDto>>

    @GET("support/faqs")
    suspend fun getFaqs(): Response<ApiResponse<List<FaqDto>>>

    @POST("api/auth/select-account")
    suspend fun selectAccount(@Body request: SelectAccountRequest): Response<AuthResponse>

    @POST("api/auth/switch-account")
    suspend fun switchAccount(@Header("Authorization") token: String, @Body request: SwitchAccountRequest): Response<AuthResponse>

    @POST("auth/accounts")
    suspend fun getAccountsByPhone(@Body request: AccountsByPhoneRequest): Response<ApiResponse<List<AccountInfo>>>
}