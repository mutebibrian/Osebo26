package com.devbrian.osebo.data

import com.devbrian.osebo.data.remote.api.FaqDto
import com.devbrian.osebo.data.remote.dto.request.UpdateProductRequest
import com.devbrian.osebo.data.remote.api.FinanceReportDto
import com.devbrian.osebo.data.remote.api.InventoryReportDto
import com.devbrian.osebo.data.remote.api.NotificationDto
import com.devbrian.osebo.data.remote.dto.request.UpdateCustomerRequest
import com.devbrian.osebo.data.remote.api.SalesReportDto
import com.devbrian.osebo.data.remote.api.SupportTicketDto
import com.devbrian.osebo.data.remote.dto.request.*
import com.devbrian.osebo.data.remote.dto.response.*
import com.devbrian.osebo.models.ApiResponse
import com.devbrian.osebo.models.CreateRoleRequest
import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.models.PollPaymentStatusRequest
import com.devbrian.osebo.models.RenewSubscriptionRequest
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.data.remote.dto.response.PaymentDto
import com.devbrian.osebo.models.UserRole
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.request.RegisterRequest
import com.devbrian.osebo.models.SaleData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== SUBSCRIPTION PACKAGES ====================
    @GET("api/package")
    suspend fun getSubscriptionPackages(): Response<ApiResponse<List<PackageDto>>>

    // ==================== SUBSCRIPTION ENDPOINTS ====================
    @POST("api/subscription")
    suspend fun createSubscription(
        @Header("X-Shop") shopId: String,
        @Body request: CreateSubscriptionRequest
    ): Response<ApiResponse<SubscriptionResponse>>

    @GET("api/subscription/CheckShopSubscription")
    suspend fun checkShopSubscription(
        @Query("shopId") shopId: String
    ): Response<ApiResponse<ShopSubscriptionStatusResponse>>

    @POST("api/subscription/AuthorizePayment")
    suspend fun authorizePayment(
        @Header("X-Shop") shopId: String,
        @Body request: CreateSubscriptionRequest
    ): Response<ApiResponse<SubscriptionResponse>>

    @POST("api/subscription/{paymentId}/status/check")
    suspend fun checkPaymentStatus(
        @Path("paymentId") paymentId: String,
        @Body request: CheckPaymentStatusRequest
    ): Response<ApiResponse<PaymentStatusResponse>>

    @GET("api/subscription/{subscriptionId}/payments")
    suspend fun getSubscriptionPayments(
        @Header("X-Shop") shopId: String,
        @Path("subscriptionId") subscriptionId: String
    ): Response<ApiResponse<List<PaymentDto>>>

    @POST("api/subscription/PollPaymentStatus")
    suspend fun pollPaymentStatus(
        @Header("X-Shop") shopId: String,
        @Body request: PollPaymentStatusRequest
    ): Response<ApiResponse<PaymentPollResponse>>

    @GET("api/subscription/FindByShoplid")
    suspend fun getShopSubscriptions(
        @Query("shopId") shopId: String
    ): Response<ApiResponse<List<Subscription>>>

    @GET("api/subscription/{id}")
    suspend fun getSubscriptionDetails(
        @Header("X-Shop") shopId: String,
        @Path("id") subscriptionId: String
    ): Response<ApiResponse<Subscription>>

    @POST("api/subscription/activate-trial/{shopId}")
    suspend fun activateFreeTrial(
        @Header("X-Shop") shopId: String,
        @Path("shopId") shopIdPath: String,
        @Body request: ActivateTrialRequest
    ): Response<ApiResponse<SubscriptionResponse>>

    @POST("api/subscription/renew/{subscriptionId}")
    suspend fun renewSubscription(
        @Header("X-Shop") shopId: String,
        @Path("subscriptionId") subscriptionId: String,
        @Body request: RenewSubscriptionRequest
    ): Response<ApiResponse<SubscriptionResponse>>

    @POST("api/subscription/cancel/{subscriptionId}")
    suspend fun cancelSubscription(
        @Header("X-Shop") shopId: String,
        @Path("subscriptionId") subscriptionId: String
    ): Response<ApiResponse<Unit>>

    // ==================== PAYMENT ENDPOINTS ====================
    @POST("api/payment/initiate")
    suspend fun initiatePayment(
        @Header("X-Shop") shopId: String,
        @Body request: InitiatePaymentRequest
    ): Response<ApiResponse<InitiatePaymentResponse>>

    @GET("api/payment/{transactionId}")
    suspend fun getPaymentStatus(
        @Header("X-Shop") shopId: String,
        @Path("transactionId") transactionId: String
    ): Response<ApiResponse<PaymentStatusResponse>>

    // ==================== EXISTING PAYMENT ENDPOINTS ====================
    @GET("api/payments")
    suspend fun getPayments(
        @Query("shop_id") shopId: String? = null,
        @Query("subscription_id") subscriptionId: String? = null
    ): Response<ApiResponse<List<PaymentDto>>>

    @POST("api/payments")
    suspend fun createPayment(@Body request: CreatePaymentRequest): Response<ApiResponse<PaymentDto>>

    // ==================== SHOPS ====================
    @GET("api/shops")
    suspend fun getShops(): Response<ApiResponse<List<ShopDto>>>

    @POST("shops")
    suspend fun createShop(@Body request: CreateShopRequest): Response<ApiResponse<ShopDto>>

    @PUT("shops/{id}")
    suspend fun updateShop(
        @Path("id") shopId: String,
        @Body request: UpdateShopRequest
    ): Response<ApiResponse<ShopDto>>

    @DELETE("shops/{id}")
    suspend fun deleteShop(@Path("id") shopId: String): Response<ApiResponse<Unit>>

    // ==================== AUTHENTICATION ENDPOINTS ====================
    @POST("api/auth/signin")
    suspend fun signIn(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>

    // ==================== USER MANAGEMENT ====================
    @GET("users/profile")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    @PUT("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserDto>>

    @PUT("users/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    // ==================== INVENTORY MANAGEMENT ====================
    // ADD THESE METHODS HERE (in the inventory section)
    @Multipart
    @POST("api/stock-item/single")
    suspend fun createProduct(
        @Header("X-Shop") shopId: String,
        @Part("name") name: RequestBody,
        @Part("sku") sku: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("low_quantity_mark") lowQuantityMark: RequestBody,
        @Part("purchase_price") purchasePrice: RequestBody,
        @Part("selling_price") sellingPrice: RequestBody,  // Changed from sell_price
        @Part("max_discount") maxDiscount: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part("unit_measure") unitMeasure: RequestBody,
        // Remove barcode if not in error list
        @Part("stock_category_id") stockCategoryId: RequestBody,  // Changed from key
        @Part photo: MultipartBody.Part? = null
    ): Response<ApiResponse<ProductDto>>

    @GET("api/stock-category")
    suspend fun getCategories(
        @Header("X-Shop") shopId: String
    ): Response<ApiResponse<List<StockCategoryDto>>>

    @POST("api/sale/record-payment")
    suspend fun recordPayment(
        @Header("x-shop-id") shopId: String,
        @Body request: PaymentRequest
    ): Response<ApiResponse<PaymentData>>

    @PUT("api/stock/{productId}")
    suspend fun updateProduct(
        @Header("X-Shop") shopId: String,
        @Path("productId") productId: String,
        @Body request: UpdateProductRequest  // Using the new DTO
    ): Response<ApiResponse<ProductDto>>

    @DELETE("api/stock/{productId}")
    suspend fun deleteProduct(
        @Header("X-Shop") shopId: String,
        @Path("productId") productId: String
    ): Response<ApiResponse<Unit>>



    @GET("api/stock-item")
    suspend fun getProducts(
        @Header("X-Shop") shopId: String
    ): Response<ApiResponse<List<ProductDto>>>



    // ==================== CUSTOMER ENDPOINTS ====================
    @GET("api/customer")
    suspend fun getCustomers(
        @Header("X-Shop") shopId: String
    ): Response<ApiResponse<List<CustomerDto>>>

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

    // ==================== SALES ENDPOINTS ====================
    @POST("api/sale")
    suspend fun createSale(
        @Header("X-Shop") shopId: String,
        @Body request: SaleRequest
    ): Response<ApiResponse<SaleData>>



    @GET("api/sale/{saleId}")
    suspend fun getSale(
        @Header("X-Shop") shopId: String,
        @Path("saleId") saleId: String
    ): Response<ApiResponse<SaleData>>

    @GET("api/sale/customer/{customerId}")
    suspend fun getCustomerSales(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String,
        @Query("type") type: String? = null
    ): Response<ApiResponse<List<SaleData>>>

    @GET("api/sale/customer-sale-history/{customerId}")
    suspend fun getCustomerSaleHistory(
        @Header("X-Shop") shopId: String,
        @Path("customerId") customerId: String
    ): Response<ApiResponse<List<SaleData>>>

    @GET("api/sale/{saleId}")
    suspend fun getSaleDetails(
        @Header("X-Shop") shopId: String,
        @Path("saleId") saleId: String
    ): Response<ApiResponse<SaleData>>

    // ==================== ROLES ====================
    @GET("roles")
    fun getRoles(
        @Header("Authorization") authorization: String,
        @Query("shopId") shopId: String? = null
    ): Call<ApiResponse<List<UserRole>>>

    @POST("roles")
    suspend fun createRole(
        @Header("Authorization") authorization: String,
        @Body request: CreateRoleRequest
    ): Response<ApiResponse<UserRole>>

    @PUT("roles/{id}")
    fun updateRole(
        @Header("Authorization") authorization: String,
        @Path("id") roleId: String,
        @Body request: CreateRoleRequest
    ): Call<ApiResponse<UserRole>>



    // ==================== EXISTING SUBSCRIPTION ENDPOINTS (Legacy - Keep for compatibility) ====================
    @GET("api/subscriptions")
    suspend fun getSubscriptions(): Response<ApiResponse<List<SubscriptionDto>>>

    @POST("api/subscriptions")
    suspend fun createLegacySubscription(@Body request: SubscribeRequest): Response<ApiResponse<SubscriptionDto>>

    // ==================== NOTIFICATIONS ====================
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

    // ==================== SUPPORT ====================
    @POST("support/tickets")
    suspend fun createSupportTicket(@Body request: CreateSupportTicketRequest): Response<ApiResponse<SupportTicketDto>>

    @GET("support/faqs")
    suspend fun getFaqs(): Response<ApiResponse<List<FaqDto>>>

    // ==================== REPORTS ====================
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
}