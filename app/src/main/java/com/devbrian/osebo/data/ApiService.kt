package com.devbrian.osebo.data

import com.devbrian.osebo.data.remote.dto.request.CreateShopRequest
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.request.RegisterRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateProfileRequest
import com.devbrian.osebo.models.*
import com.devbrian.osebo.models.contact.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ==================== AUTHENTICATION ====================
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>

    @POST("auth/logout")
    fun logout(@Header("Authorization") token: String): Call<ApiResponse>

    @POST("auth/refresh-token")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<LoginResponse>

    @POST("auth/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ApiResponse>

    @POST("auth/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ApiResponse>

    // ==================== USER PROFILE ====================
    @GET("user/profile")
    fun getUserProfile(@Header("Authorization") token: String): Call<User>

    @PUT("user/profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<User>

    @POST("user/change-password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Call<ApiResponse>

    // ==================== SHOP MANAGEMENT ====================
    @GET("shops")
    fun getUserShops(@Header("Authorization") token: String): Call<List<Shop>>

    @POST("shops")
    fun createShop(
        @Header("Authorization") token: String,
        @Body request: CreateShopRequest
    ): Call<Shop>

    @GET("shops/{shopId}")
    fun getShopDetails(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String
    ): Call<Shop>

    @PUT("shops/{shopId}")
    fun updateShop(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String,
        @Body request: UpdateShopRequest
    ): Call<Shop>

    @DELETE("shops/{shopId}")
    fun deleteShop(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String
    ): Call<ApiResponse>

    @GET("shops/{shopId}/stats")
    fun getShopStats(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String
    ): Call<ShopStats>

    // ==================== USER ROLES & PERMISSIONS ====================
    @GET("roles")
    fun getAllUserRoles(@Header("Authorization") token: String): Call<List<UserRole>>

    @GET("shops/{shopId}/roles")
    fun getUserRolesByShop(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String
    ): Call<List<UserRole>>

    @GET("roles/{roleId}")
    fun getUserRoleById(
        @Header("Authorization") token: String,
        @Path("roleId") roleId: String
    ): Call<UserRole>

    @PUT("roles/{roleId}")
    fun updateRolePermissions(
        @Header("Authorization") token: String,
        @Path("roleId") roleId: String,
        @Body request: UpdatePermissionsRequest
    ): Call<UserRole>

    @POST("roles")
    fun createRole(
        @Header("Authorization") token: String,
        @Body request: CreateRoleRequest
    ): Call<UserRole>

    @DELETE("roles/{roleId}")
    fun deleteRole(
        @Header("Authorization") token: String,
        @Path("roleId") roleId: String
    ): Call<ApiResponse>

    @GET("permissions")
    fun getAvailablePermissions(@Header("Authorization") token: String): Call<List<Permission>>

    // ==================== INVENTORY MANAGEMENT ====================
    @GET("shops/{shopId}/inventory")
    fun getInventory(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<InventoryResponse>

    @POST("inventory")
    fun addInventoryItem(
        @Header("Authorization") token: String,
        @Body request: AddInventoryRequest
    ): Call<InventoryItem>

    @PUT("inventory/{itemId}")
    fun updateInventoryItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateInventoryRequest
    ): Call<InventoryItem>

    @DELETE("inventory/{itemId}")
    fun deleteInventoryItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String
    ): Call<ApiResponse>

    @GET("inventory/categories")
    fun getInventoryCategories(@Header("Authorization") token: String): Call<List<Category>>

    @GET("inventory/low-stock")
    fun getLowStockItems(
        @Header("Authorization") token: String,
        @Query("shopId") shopId: String
    ): Call<List<InventoryItem>>

    // ==================== SALES MANAGEMENT ====================
    @GET("shops/{shopId}/sales")
    fun getSales(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<SalesResponse>

    @POST("sales")
    fun createSale(
        @Header("Authorization") token: String,
        @Body request: CreateSaleRequest
    ): Call<Sale>

    @GET("sales/{saleId}")
    fun getSaleDetails(
        @Header("Authorization") token: String,
        @Path("saleId") saleId: String
    ): Call<Sale>

    @POST("sales/{saleId}/receipt")
    fun generateReceipt(
        @Header("Authorization") token: String,
        @Path("saleId") saleId: String
    ): Call<Receipt>

    @GET("sales/daily-summary")
    fun getDailySalesSummary(
        @Header("Authorization") token: String,
        @Query("shopId") shopId: String,
        @Query("date") date: String
    ): Call<DailySummary>

    // ==================== FINANCE MANAGEMENT ====================
    @GET("shops/{shopId}/financial-summary")
    fun getFinancialSummary(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String
    ): Call<FinancialSummary>

    @GET("shops/{shopId}/expenses")
    fun getExpenses(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String,
        @Query("month") month: String? = null,
        @Query("year") year: String? = null
    ): Call<List<Expense>>

    @POST("expenses")
    fun addExpense(
        @Header("Authorization") token: String,
        @Body request: AddExpenseRequest
    ): Call<Expense>

    @PUT("expenses/{expenseId}")
    fun updateExpense(
        @Header("Authorization") token: String,
        @Path("expenseId") expenseId: String,
        @Body request: UpdateExpenseRequest
    ): Call<Expense>

    @DELETE("expenses/{expenseId}")
    fun deleteExpense(
        @Header("Authorization") token: String,
        @Path("expenseId") expenseId: String
    ): Call<ApiResponse>

    // ==================== CONTACT & SUPPORT ====================
    @GET("support/contact-info")
    fun getContactInfo(): Call<com.devbrian.osebo.models.contact.ContactInfo>

    @POST("support/send-message")
    fun sendSupportMessage(@Body request: ContactFormRequest): Call<ApiResponse>

    @GET("support/tickets")
    fun getSupportTickets(
        @Header("Authorization") token: String,
        @Query("shop_id") shopId: String? = null,
        @Query("status") status: String? = null,
        @Query("user_id") userId: String? = null
    ): Call<List<SupportTicket>>

    @POST("support/tickets")
    fun createSupportTicket(
        @Header("Authorization") token: String,
        @Body request: SupportTicketRequest
    ): Call<SupportTicket>

    @GET("support/tickets/{ticketId}")
    fun getTicketDetails(
        @Header("Authorization") token: String,
        @Path("ticketId") ticketId: String
    ): Call<SupportTicketResponse>

    @POST("support/tickets/{ticketId}/messages")
    fun addTicketMessage(
        @Header("Authorization") token: String,
        @Path("ticketId") ticketId: String,
        @Body request: Map<String, String>
    ): Call<TicketMessage>

    @GET("support/faqs")
    fun getFAQs(
        @Query("category") category: String? = null,
        @Query("language") language: String = "en"
    ): Call<List<FAQ>>

    @GET("support/categories")
    fun getHelpCategories(): Call<List<String>>

    // ==================== SUBSCRIPTION & BILLING ====================
    @GET("subscriptions/packages")
    fun getSubscriptionPackages(): Call<List<SubscriptionPackage>>

    @GET("shops/{shopId}/subscription")
    fun getShopSubscription(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String
    ): Call<Subscription>

    @POST("subscriptions/activate")
    fun activateSubscription(
        @Header("Authorization") token: String,
        @Body request: ActivateSubscriptionRequest
    ): Call<Subscription>

    @POST("subscriptions/renew")
    fun renewSubscription(
        @Header("Authorization") token: String,
        @Body request: RenewSubscriptionRequest
    ): Call<Subscription>

    @GET("subscriptions/status/{subscriptionId}")
    fun getSubscriptionStatus(
        @Header("Authorization") token: String,
        @Path("subscriptionId") subscriptionId: String
    ): Call<SubscriptionStatus>

    // ==================== REPORTS & ANALYTICS ====================
    @GET("reports/sales")
    fun getSalesReport(
        @Header("Authorization") token: String,
        @Query("shopId") shopId: String,
        @Query("period") period: String, // daily, weekly, monthly, yearly
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Call<SalesReport>

    @GET("reports/inventory")
    fun getInventoryReport(
        @Header("Authorization") token: String,
        @Query("shopId") shopId: String
    ): Call<InventoryReport>

    @GET("reports/financial")
    fun getFinancialReport(
        @Header("Authorization") token: String,
        @Query("shopId") shopId: String,
        @Query("month") month: String? = null,
        @Query("year") year: String? = null
    ): Call<FinancialReport>

    // ==================== EMPLOYEE MANAGEMENT ====================
    @GET("shops/{shopId}/employees")
    fun getEmployees(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String
    ): Call<List<Employee>>

    @POST("employees")
    fun addEmployee(
        @Header("Authorization") token: String,
        @Body request: AddEmployeeRequest
    ): Call<Employee>

    @PUT("employees/{employeeId}")
    fun updateEmployee(
        @Header("Authorization") token: String,
        @Path("employeeId") employeeId: String,
        @Body request: UpdateEmployeeRequest
    ): Call<Employee>

    @DELETE("employees/{employeeId}")
    fun deleteEmployee(
        @Header("Authorization") token: String,
        @Path("employeeId") employeeId: String
    ): Call<ApiResponse>

    // ==================== CUSTOMER MANAGEMENT ====================
    @GET("shops/{shopId}/customers")
    fun getCustomers(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<CustomersResponse>

    @POST("customers")
    fun addCustomer(
        @Header("Authorization") token: String,
        @Body request: AddCustomerRequest
    ): Call<Customer>

    @PUT("customers/{customerId}")
    fun updateCustomer(
        @Header("Authorization") token: String,
        @Path("customerId") customerId: String,
        @Body request: UpdateCustomerRequest
    ): Call<Customer>

    @DELETE("customers/{customerId}")
    fun deleteCustomer(
        @Header("Authorization") token: String,
        @Path("customerId") customerId: String
    ): Call<ApiResponse>

    // ==================== SUPPLIER MANAGEMENT ====================
    @GET("shops/{shopId}/suppliers")
    fun getSuppliers(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String
    ): Call<List<Supplier>>

    @POST("suppliers")
    fun addSupplier(
        @Header("Authorization") token: String,
        @Body request: AddSupplierRequest
    ): Call<Supplier>

    @PUT("suppliers/{supplierId}")
    fun updateSupplier(
        @Header("Authorization") token: String,
        @Path("supplierId") supplierId: String,
        @Body request: UpdateSupplierRequest
    ): Call<Supplier>

    @DELETE("suppliers/{supplierId}")
    fun deleteSupplier(
        @Header("Authorization") token: String,
        @Path("supplierId") supplierId: String
    ): Call<ApiResponse>

    // ==================== NOTIFICATIONS ====================
    @GET("notifications")
    fun getNotifications(
        @Header("Authorization") token: String,
        @Query("shopId") shopId: String? = null,
        @Query("unreadOnly") unreadOnly: Boolean = false
    ): Call<List<Notification>>

    @PUT("notifications/{notificationId}/read")
    fun markNotificationAsRead(
        @Header("Authorization") token: String,
        @Path("notificationId") notificationId: String
    ): Call<ApiResponse>

    @PUT("notifications/mark-all-read")
    fun markAllNotificationsAsRead(
        @Header("Authorization") token: String,
        @Query("shopId") shopId: String? = null
    ): Call<ApiResponse>

    // ==================== SETTINGS ====================
    @GET("shops/{shopId}/settings")
    fun getShopSettings(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String
    ): Call<ShopSettings>

    @PUT("shops/{shopId}/settings")
    fun updateShopSettings(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String,
        @Body request: UpdateShopSettingsRequest
    ): Call<ShopSettings>

    @GET("user/preferences")
    fun getUserPreferences(
        @Header("Authorization") token: String
    ): Call<UserPreferences>

    @PUT("user/preferences")
    fun updateUserPreferences(
        @Header("Authorization") token: String,
        @Body request: UpdateUserPreferencesRequest
    ): Call<UserPreferences>

    // ==================== FILE UPLOAD ====================
    @Multipart
    @POST("upload/image")
    fun uploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: String? = null
    ): Call<UploadResponse>

    @Multipart
    @POST("upload/document")
    fun uploadDocument(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: String? = null
    ): Call<UploadResponse>
}

// ==================== REQUEST MODELS ====================
data class RefreshTokenRequest(
    val refresh_token: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val password: String,
    val password_confirmation: String
)

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String,
    val new_password_confirmation: String
)

data class UpdateShopRequest(
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val logo: String? = null
)

data class AddInventoryRequest(
    val shopId: String,
    val name: String,
    val category: String,
    val price: Double,
    val costPrice: Double,
    val quantity: Int,
    val minStockLevel: Int? = null,
    val barcode: String? = null,
    val description: String? = null,
    val imageUrl: String? = null
)

data class UpdateInventoryRequest(
    val name: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val costPrice: Double? = null,
    val quantity: Int? = null,
    val minStockLevel: Int? = null,
    val description: String? = null,
    val imageUrl: String? = null
)

data class CreateSaleRequest(
    val shopId: String,
    val customerId: String? = null,
    val items: List<SaleItem>,
    val paymentMethod: String,
    val discount: Double? = 0.0,
    val tax: Double? = 0.0,
    val notes: String? = null
)

data class SaleItem(
    val inventoryItemId: String,
    val quantity: Int,
    val price: Double
)

data class AddExpenseRequest(
    val shopId: String,
    val category: String,
    val amount: Double,
    val description: String,
    val date: String,
    val receiptUrl: String? = null
)

data class UpdateExpenseRequest(
    val category: String? = null,
    val amount: Double? = null,
    val description: String? = null,
    val date: String? = null,
    val receiptUrl: String? = null
)

data class ActivateSubscriptionRequest(
    val shopId: String,
    val packageId: String,
    val months: Int,
    val phoneNumber: String,
    val paymentMethod: String = "mobile_money"
)

data class RenewSubscriptionRequest(
    val shopId: String,
    val months: Int,
    val phoneNumber: String,
    val paymentMethod: String = "mobile_money"
)

data class AddEmployeeRequest(
    val shopId: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val roleId: String,
    val salary: Double? = null,
    val hireDate: String
)

data class UpdateEmployeeRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val roleId: String? = null,
    val salary: Double? = null,
    val status: String? = null
)

data class AddCustomerRequest(
    val shopId: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val customerType: String = "regular"
)

data class UpdateCustomerRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val customerType: String? = null
)

data class AddSupplierRequest(
    val shopId: String,
    val name: String,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val productsSupplied: List<String>? = null
)

data class UpdateSupplierRequest(
    val name: String? = null,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val productsSupplied: List<String>? = null
)

data class UpdateShopSettingsRequest(
    val currency: String? = null,
    val timezone: String? = null,
    val language: String? = null,
    val taxRate: Double? = null,
    val receiptFooter: String? = null,
    val lowStockThreshold: Int? = null
)

data class UpdateUserPreferencesRequest(
    val notificationsEnabled: Boolean? = null,
    val theme: String? = null,
    val language: String? = null,
    val currency: String? = null
)

data class UploadResponse(
    val url: String,
    val fileName: String,
    val fileSize: Long
)

// Note: For Multipart uploads, you'll need to add this import:
// import okhttp3.MultipartBody