package com.devbrian.osebo.data.remote

import com.devbrian.osebo.data.models.UserRole
import com.devbrian.osebo.data.remote.dto.request.AddCustomerRequest
import com.devbrian.osebo.data.remote.dto.request.AddSupplierRequest
import com.devbrian.osebo.data.remote.dto.request.ChangePasswordRequest
import com.devbrian.osebo.data.remote.dto.request.CreateCustomerRequest
import com.devbrian.osebo.data.remote.dto.request.CreateExpenseCategoryRequest
import com.devbrian.osebo.data.remote.dto.request.CreateExpenseRequest
import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.request.CreateSupportTicketRequest
import com.devbrian.osebo.data.remote.dto.request.EmployeeRequest
import com.devbrian.osebo.data.remote.dto.request.ForgotPasswordRequest
import com.devbrian.osebo.data.remote.dto.request.LoginNotificationsRequest
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.request.PaymentRequest
import com.devbrian.osebo.data.remote.dto.request.RefreshTokenRequest
import com.devbrian.osebo.data.remote.dto.request.RegisterRequest
import com.devbrian.osebo.data.remote.dto.request.ResendOtpRequest
import com.devbrian.osebo.data.remote.dto.request.ResetPasswordRequest
import com.devbrian.osebo.data.remote.dto.request.SaleRequest
import com.devbrian.osebo.data.remote.dto.request.SelectAccountRequest
import com.devbrian.osebo.data.remote.dto.request.SwitchAccountRequest
import com.devbrian.osebo.data.remote.dto.request.AccountsByPhoneRequest
import com.devbrian.osebo.data.remote.dto.request.TwoFactorAuthRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateCustomerRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateExpenseCategoryRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateExpenseRequest
import com.devbrian.osebo.data.remote.dto.response.UpdateFinanceSettingsRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateProductRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateProfileRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateShopRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateSupplierRequest
import com.devbrian.osebo.data.remote.dto.request.VerifyOtpRequest
import com.devbrian.osebo.data.remote.dto.response.AccountDto
import com.devbrian.osebo.data.remote.dto.response.AccountInfo
import com.devbrian.osebo.data.remote.dto.response.AuthResponse
import com.devbrian.osebo.data.remote.dto.response.BaseResponse
import com.devbrian.osebo.data.remote.dto.response.CustomerDto
import com.devbrian.osebo.data.remote.dto.response.FinanceSettingsDto
import com.devbrian.osebo.data.remote.dto.response.FinancialStatementDto
import com.devbrian.osebo.data.remote.dto.response.PackageDto
import com.devbrian.osebo.data.remote.dto.response.PaymentCheckResponse
import com.devbrian.osebo.data.remote.dto.response.PaymentData
import com.devbrian.osebo.data.remote.dto.response.PaymentDto
import com.devbrian.osebo.data.remote.dto.response.ProductDto
import com.devbrian.osebo.data.remote.dto.response.ResetResponse
import com.devbrian.osebo.data.remote.dto.response.SaleApiResponse
import com.devbrian.osebo.data.remote.dto.response.SaleListApiResponse
import com.devbrian.osebo.data.remote.dto.response.SalesComparisonDto
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.remote.dto.response.ShopSummaryDto
import com.devbrian.osebo.data.remote.dto.response.ShopTotalsDto
import com.devbrian.osebo.data.remote.dto.response.StockCategoryDto
import com.devbrian.osebo.data.remote.dto.response.SubscriptionResponse
import com.devbrian.osebo.data.remote.api.SupplierDto
import com.devbrian.osebo.data.remote.dto.response.UserDto
import com.devbrian.osebo.data.remote.api.FaqDto
import com.devbrian.osebo.data.remote.api.FinanceReportDto
import com.devbrian.osebo.data.remote.api.InventoryReportDto
import com.devbrian.osebo.data.remote.api.NotificationDto
import com.devbrian.osebo.data.remote.api.SalesReportDto
import com.devbrian.osebo.data.remote.api.SupportTicketDto
import com.devbrian.osebo.models.ApiResponse
import com.devbrian.osebo.models.CreateEmployeeResponse
import com.devbrian.osebo.models.CreateRoleRequest
import com.devbrian.osebo.models.EmployeeResponse
import com.devbrian.osebo.models.Expense
import com.devbrian.osebo.models.ExpenseCategory
import com.devbrian.osebo.models.Permission
import com.devbrian.osebo.models.RenewSubscriptionRequest
import com.devbrian.osebo.models.TimeSeriesApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException

/**
 * Ktor-based, Kotlin/Native-safe replacement for the Retrofit
 * `com.devbrian.osebo.data.ApiService` interface that androidMain's
 * repositories currently depend on. Built in commonMain so iOS gets the
 * same client; not wired into Koin or any repository yet — that happens
 * incrementally, one repository at a time, each validated before the next.
 *
 * Covers every endpoint from ApiService.kt except the ones whose
 * request/response models are still Android-only domain objects (Shop,
 * ShopType, Product search, the ledger's FinancialStatement, and the legacy
 * Subscription model — which pulls in java.text.SimpleDateFormat and
 * android.R.color, neither multiplatform-safe): createShop, getShopTypes,
 * searchProductByBarcode, searchProducts, getGeneralLedger,
 * getCustomerLedger, checkShopSubscription, getShopSubscriptions,
 * getUserShopsSubscriptions, getSubscriptionDetails. Those land once those
 * models get a real multiplatform port (date formatting via
 * kotlinx-datetime, color resource IDs moved to an androidMain extension).
 */
class KtorOseboApiService(private val client: HttpClient) {

    private suspend inline fun <reified T> execute(crossinline call: suspend () -> HttpResponse): ApiResult<T> {
        return try {
            val response = call()
            if (response.status.isSuccess()) {
                ApiResult.Success(response.body<T>(), response.status.value)
            } else {
                val message = try {
                    response.bodyAsText()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    response.status.description
                }
                ApiResult.Error(response.status.value, message)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.NetworkError(e.message ?: "Network error")
        }
    }

    // ==================== AUTH ====================

    suspend fun signIn(request: LoginRequest): ApiResult<AuthResponse> =
        execute { client.post("api/v1/auth/signin") { setBody(request) } }

    suspend fun verifyTwoFactor(request: com.devbrian.osebo.data.remote.dto.request.VerifyTwoFactorRequest): ApiResult<AuthResponse> =
        execute { client.post("api/v1/auth/verify-2fa") { setBody(request) } }

    suspend fun refreshToken(request: RefreshTokenRequest): ApiResult<AuthResponse> =
        execute { client.post("auth/refresh") { setBody(request) } }

    suspend fun signOut(request: String): ApiResult<ApiResponse<Unit>> =
        execute { client.post("auth/signout") { setBody(request) } }

    suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> =
        execute { client.post("auth/signup") { setBody(request) } }

    suspend fun logout(): ApiResult<ApiResponse<Unit>> =
        execute { client.post("auth/logout") }

    suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResult<ApiResponse<Unit>> =
        execute { client.post("auth/forgot-password") { setBody(request) } }

    suspend fun resetPassword(request: ResetPasswordRequest): ApiResult<ApiResponse<Unit>> =
        execute { client.post("auth/reset-password") { setBody(request) } }

    suspend fun resendVerificationCode(email: String): ApiResult<ApiResponse<Unit>> =
        execute { client.post("api/v1/auth/resend-verification") { parameter("email", email) } }

    suspend fun resendOtp(request: ResendOtpRequest): ApiResult<AuthResponse> =
        execute { client.post("api/v1/auth/resend-otp") { setBody(request) } }

    suspend fun verifyOtp(request: VerifyOtpRequest): ApiResult<AuthResponse> =
        execute { client.post("api/v1/auth/verify-otp") { setBody(request) } }

    suspend fun generateOtp(request: Map<String, String>): ApiResult<BaseResponse<Unit>> =
        execute { client.post("api/v1/auth/send-otp") { setBody(request) } }

    suspend fun sendOtp(request: Map<String, String>): ApiResult<BaseResponse<Unit>> =
        execute { client.post("api/v1/auth/resend-otp") { setBody(request) } }

    suspend fun verifyOtpMap(request: Map<String, String>): ApiResult<BaseResponse<Unit>> =
        execute { client.post("api/v1/auth/verify-otp") { setBody(request) } }

    suspend fun getUserIdByPhone(request: Map<String, String>): ApiResult<BaseResponse<ResetResponse>> =
        execute { client.post("api/v1/auth/phone-password-reset") { setBody(request) } }

    suspend fun selectAccount(request: SelectAccountRequest): ApiResult<AuthResponse> =
        execute { client.post("api/v1/auth/select-account") { setBody(request) } }

    suspend fun switchAccount(request: SwitchAccountRequest): ApiResult<AuthResponse> =
        execute { client.post("api/v1/auth/switch-account") { setBody(request) } }

    suspend fun getAccountsByPhone(request: AccountsByPhoneRequest): ApiResult<ApiResponse<List<AccountInfo>>> =
        execute { client.post("auth/accounts") { setBody(request) } }

    // ==================== USER / ACCOUNT ====================

    suspend fun getCurrentUser(): ApiResult<ApiResponse<UserDto>> =
        execute { client.get("users/profile") }

    suspend fun updateProfile(request: UpdateProfileRequest): ApiResult<ApiResponse<UserDto>> =
        execute { client.put("users/profile") { setBody(request) } }

    suspend fun changePassword(request: ChangePasswordRequest): ApiResult<ApiResponse<Unit>> =
        execute { client.put("users/password") { setBody(request) } }

    suspend fun getAccountDetails(): ApiResult<ApiResponse<AccountDto>> =
        execute { client.get("api/v1/account") }

    suspend fun updateTwoFactorAuth(request: TwoFactorAuthRequest): ApiResult<ApiResponse<AccountDto>> =
        execute { client.put("api/v1/account/two-factor") { setBody(request) } }

    suspend fun updateLoginNotifications(request: LoginNotificationsRequest): ApiResult<ApiResponse<AccountDto>> =
        execute { client.put("api/v1/account/login-notifications") { setBody(request) } }

    suspend fun deactivateAccount(): ApiResult<ApiResponse<Unit>> =
        execute { client.post("api/v1/account/deactivate") }

    suspend fun deleteAccount(): ApiResult<ApiResponse<Unit>> =
        execute { client.delete("api/v1/account") }

    // ==================== PERMISSION / ROLE / EMPLOYEE ====================

    suspend fun getPermissions(): ApiResult<ApiResponse<List<Permission>>> =
        execute { client.get("api/v1/permissions") }

    suspend fun getRoles(shopId: String? = null): ApiResult<ApiResponse<List<UserRole>>> =
        execute { client.get("roles") { shopId?.let { parameter("shopId", it) } } }

    suspend fun createRole(request: CreateRoleRequest): ApiResult<ApiResponse<UserRole>> =
        execute { client.post("roles") { setBody(request) } }

    suspend fun updateRole(roleId: String, request: CreateRoleRequest): ApiResult<ApiResponse<UserRole>> =
        execute { client.put("roles/$roleId") { setBody(request) } }

    suspend fun createEmployee(employee: EmployeeRequest): ApiResult<CreateEmployeeResponse> =
        execute { client.post("api/v1/users") { setBody(employee) } }

    suspend fun getEmployees(): ApiResult<EmployeeResponse> =
        execute { client.get("api/v1/users") }

    suspend fun updateEmployee(userId: String, employee: EmployeeRequest): ApiResult<EmployeeResponse> =
        execute { client.put("api/v1/users/$userId") { setBody(employee) } }

    suspend fun deleteEmployee(userId: String): ApiResult<ApiResponse<Unit>> =
        execute { client.delete("api/v1/users/$userId") }

    // ==================== SHOP ====================

    suspend fun getShops(): ApiResult<ApiResponse<List<ShopDto>>> =
        execute { client.get("api/v1/shops") }

    suspend fun deleteShop(shopId: String): ApiResult<ApiResponse<Unit>> =
        execute { client.delete("shops/$shopId") }

    suspend fun updateShop(shopId: String, request: UpdateShopRequest): ApiResult<ApiResponse<ShopDto>> =
        execute { client.put("shops/$shopId") { setBody(request) } }

    suspend fun getFinanceSettings(shopId: String): ApiResult<ApiResponse<FinanceSettingsDto>> =
        execute { client.get("api/v1/shops/$shopId/settings") { header("X-Shop", shopId) } }

    suspend fun updateFinanceSettings(
        shopId: String,
        request: UpdateFinanceSettingsRequest
    ): ApiResult<ApiResponse<FinanceSettingsDto>> =
        execute {
            client.put("api/v1/shops/$shopId/settings") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    // ==================== PRODUCT ====================

    suspend fun getProducts(shopId: String): ApiResult<ApiResponse<List<ProductDto>>> =
        execute { client.get("api/v1/stock-item") { header("X-Shop", shopId) } }

    suspend fun createProduct(
        shopId: String,
        name: String,
        sku: String,
        description: String?,
        lowQuantityMark: String,
        purchasePrice: String,
        sellingPrice: String,
        maxDiscount: String,
        quantity: String,
        unitMeasure: String,
        stockCategoryId: String,
        photoBytes: ByteArray? = null,
        photoFileName: String? = null,
        photoContentType: String? = null
    ): ApiResult<ApiResponse<ProductDto>> = execute {
        client.submitFormWithBinaryData(
            url = "api/v1/stock-item/single",
            formData = formData {
                append("name", name)
                append("sku", sku)
                description?.let { append("description", it) }
                append("low_quantity_mark", lowQuantityMark)
                append("purchase_price", purchasePrice)
                append("selling_price", sellingPrice)
                append("max_discount", maxDiscount)
                append("quantity", quantity)
                append("unit_measure", unitMeasure)
                append("stock_category_id", stockCategoryId)
                if (photoBytes != null && photoFileName != null) {
                    append(
                        "photo",
                        photoBytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, photoContentType ?: "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"$photoFileName\"")
                        }
                    )
                }
            }
        ) {
            header("X-Shop", shopId)
        }
    }

    suspend fun updateProduct(
        shopId: String,
        productId: String,
        request: UpdateProductRequest
    ): ApiResult<ApiResponse<ProductDto>> =
        execute {
            client.put("api/v1/stock/$productId") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun deleteProduct(shopId: String, productId: String): ApiResult<ApiResponse<Unit>> =
        execute { client.delete("api/v1/stock/$productId") { header("X-Shop", shopId) } }

    suspend fun getCategories(shopId: String): ApiResult<ApiResponse<List<StockCategoryDto>>> =
        execute { client.get("api/v1/stock-category") { header("X-Shop", shopId) } }

    // ==================== CUSTOMER ====================

    suspend fun getCustomers(shopId: String): ApiResult<ApiResponse<List<CustomerDto>>> =
        execute { client.get("api/v1/customer") { header("X-Shop", shopId) } }

    suspend fun getCustomer(shopId: String, customerId: String): ApiResult<ApiResponse<CustomerDto>> =
        execute { client.get("api/v1/customer/$customerId") { header("X-Shop", shopId) } }

    suspend fun createCustomer(shopId: String, request: CreateCustomerRequest): ApiResult<ApiResponse<CustomerDto>> =
        execute {
            client.post("api/v1/customer") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun updateCustomer(
        shopId: String,
        customerId: String,
        request: UpdateCustomerRequest
    ): ApiResult<ApiResponse<CustomerDto>> =
        execute {
            client.put("api/v1/customer/$customerId") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun deleteCustomer(shopId: String, customerId: String): ApiResult<ApiResponse<Unit>> =
        execute { client.delete("api/v1/customer/$customerId") { header("X-Shop", shopId) } }

    // ==================== SUPPLIER ====================

    suspend fun getSuppliers(shopId: String): ApiResult<ApiResponse<List<SupplierDto>>> =
        execute { client.get("api/v1/supplier") { header("X-Shop", shopId) } }

    suspend fun getSupplier(shopId: String, supplierId: String): ApiResult<ApiResponse<SupplierDto>> =
        execute { client.get("api/v1/supplier/$supplierId") { header("X-Shop", shopId) } }

    suspend fun createSupplier(shopId: String, request: AddSupplierRequest): ApiResult<ApiResponse<SupplierDto>> =
        execute {
            client.post("api/v1/supplier") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun updateSupplier(
        shopId: String,
        supplierId: String,
        request: UpdateSupplierRequest
    ): ApiResult<ApiResponse<SupplierDto>> =
        execute {
            client.patch("api/v1/supplier/$supplierId") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun deleteSupplier(shopId: String, supplierId: String): ApiResult<ApiResponse<Unit>> =
        execute { client.delete("api/v1/supplier/$supplierId") { header("X-Shop", shopId) } }

    // ==================== SALE ====================

    suspend fun getSales(shopId: String): ApiResult<SaleListApiResponse> =
        execute { client.get("api/v1/sale") { header("X-Shop", shopId) } }

    suspend fun createSale(shopId: String, request: SaleRequest): ApiResult<SaleApiResponse> =
        execute {
            client.post("api/v1/sale") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun getSale(shopId: String, saleId: String): ApiResult<SaleApiResponse> =
        execute { client.get("api/v1/sale/$saleId") { header("X-Shop", shopId) } }

    suspend fun getCustomerSales(
        shopId: String,
        customerId: String,
        type: String? = null
    ): ApiResult<SaleListApiResponse> =
        execute {
            client.get("api/v1/sale/customer/$customerId") {
                header("X-Shop", shopId)
                type?.let { parameter("type", it) }
            }
        }

    suspend fun getCustomerSaleHistory(shopId: String, customerId: String): ApiResult<SaleListApiResponse> =
        execute { client.get("api/v1/sale/customer-sale-history/$customerId") { header("X-Shop", shopId) } }

    suspend fun recordPayment(shopId: String, request: PaymentRequest): ApiResult<ApiResponse<PaymentData>> =
        execute {
            client.post("api/v1/sale/record-payment") {
                header("x-shop-id", shopId)
                setBody(request)
            }
        }

    // ==================== EXPENSE ====================

    suspend fun getExpenses(shopId: String): ApiResult<ApiResponse<List<Expense>>> =
        execute { client.get("api/v1/expense") { header("X-Shop", shopId) } }

    suspend fun getAllExpenses(
        shopId: String,
        startDate: String? = null,
        endDate: String? = null
    ): ApiResult<ApiResponse<List<Expense>>> =
        execute {
            client.get("api/v1/expense") {
                header("X-Shop", shopId)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
            }
        }

    suspend fun createExpense(shopId: String, request: CreateExpenseRequest): ApiResult<ApiResponse<Expense>> =
        execute {
            client.post("api/v1/expense") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun getExpense(shopId: String, expenseId: String): ApiResult<ApiResponse<Expense>> =
        execute { client.get("api/v1/expense/$expenseId") { header("X-Shop", shopId) } }

    suspend fun updateExpense(
        shopId: String,
        expenseId: String,
        request: UpdateExpenseRequest
    ): ApiResult<ApiResponse<Expense>> =
        execute {
            client.put("api/v1/expense/$expenseId") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun deleteExpense(shopId: String, expenseId: String): ApiResult<ApiResponse<Unit>> =
        execute { client.delete("api/v1/expense/$expenseId") { header("X-Shop", shopId) } }

    suspend fun getExpenseCategories(shopId: String): ApiResult<ApiResponse<List<ExpenseCategory>>> =
        execute { client.get("api/v1/expense-category") { header("X-Shop", shopId) } }

    suspend fun createExpenseCategory(
        shopId: String,
        request: CreateExpenseCategoryRequest
    ): ApiResult<ApiResponse<ExpenseCategory>> =
        execute {
            client.post("api/v1/expense-category") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun updateExpenseCategory(
        shopId: String,
        categoryId: String,
        request: UpdateExpenseCategoryRequest
    ): ApiResult<ApiResponse<ExpenseCategory>> =
        execute {
            client.put("api/v1/expense-category/$categoryId") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun deleteExpenseCategory(shopId: String, categoryId: String): ApiResult<ApiResponse<Unit>> =
        execute { client.delete("api/v1/expense-category/$categoryId") { header("X-Shop", shopId) } }

    // ==================== ANALYTICS & REPORTS ====================

    suspend fun getTopStockItems(shopId: String): ApiResult<ApiResponse<com.devbrian.osebo.data.remote.dto.response.TopStockItemsDto>> =
        execute { client.get("api/v1/analytics/top-stock-items") { header("X-Shop", shopId) } }

    suspend fun getFinancialStatementSummary(
        shopId: String,
        range: String = "yearly"
    ): ApiResult<ApiResponse<FinancialStatementDto>> =
        execute {
            client.get("api/v1/analytics/shop-financial-statement") {
                header("X-Shop", shopId)
                parameter("range", range)
            }
        }

    suspend fun getSalesComparison(shopId: String, period: String = "monthly"): ApiResult<ApiResponse<SalesComparisonDto>> =
        execute {
            client.get("api/v1/analytics/sales-comparison") {
                header("X-Shop", shopId)
                parameter("period", period)
            }
        }

    suspend fun getShopSummary(shopId: String): ApiResult<ApiResponse<ShopSummaryDto>> =
        execute { client.get("api/v1/analytics/shop-summary") { header("X-Shop", shopId) } }

    suspend fun getShopTotals(shopId: String): ApiResult<ApiResponse<ShopTotalsDto>> =
        execute { client.get("api/v1/analytics/totals") { header("X-Shop", shopId) } }

    suspend fun getTimeSeries(shopId: String, range: String = "monthly"): ApiResult<ApiResponse<TimeSeriesApiResponse>> =
        execute {
            client.get("api/v1/analytics/time-series") {
                header("X-Shop", shopId)
                parameter("range", range)
            }
        }

    suspend fun getSalesReport(
        shopId: String? = null,
        startDate: String,
        endDate: String,
        format: String = "json"
    ): ApiResult<ApiResponse<SalesReportDto>> =
        execute {
            client.get("reports/sales") {
                shopId?.let { parameter("shopId", it) }
                parameter("startDate", startDate)
                parameter("endDate", endDate)
                parameter("format", format)
            }
        }

    suspend fun getInventoryReport(shopId: String? = null, format: String = "json"): ApiResult<ApiResponse<InventoryReportDto>> =
        execute {
            client.get("reports/inventory") {
                shopId?.let { parameter("shopId", it) }
                parameter("format", format)
            }
        }

    suspend fun getFinanceReport(
        shopId: String? = null,
        startDate: String,
        endDate: String,
        format: String = "json"
    ): ApiResult<ApiResponse<FinanceReportDto>> =
        execute {
            client.get("reports/finance") {
                shopId?.let { parameter("shopId", it) }
                parameter("startDate", startDate)
                parameter("endDate", endDate)
                parameter("format", format)
            }
        }

    // ==================== SUBSCRIPTION & PAYMENT ====================

    suspend fun getSubscriptionPackages(): ApiResult<ApiResponse<List<PackageDto>>> =
        execute { client.get("api/v1/package") }

    suspend fun createSubscription(
        shopId: String,
        request: CreateSubscriptionRequest
    ): ApiResult<ApiResponse<SubscriptionResponse>> =
        execute {
            client.post("api/v1/subscription") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun renewSubscription(
        shopId: String,
        request: RenewSubscriptionRequest
    ): ApiResult<ApiResponse<SubscriptionResponse>> =
        execute {
            client.post("api/v1/subscription/renew") {
                header("X-Shop", shopId)
                setBody(request)
            }
        }

    suspend fun cancelSubscription(shopId: String, subscriptionId: String): ApiResult<ApiResponse<Unit>> =
        execute { client.patch("api/v1/subscription/$subscriptionId/cancel") { header("X-Shop", shopId) } }

    suspend fun cancelPackage(shopId: String, subscriptionId: String, packageId: String): ApiResult<ApiResponse<Unit>> =
        execute {
            client.delete("api/v1/subscription/$subscriptionId/packages/$packageId") {
                header("X-Shop", shopId)
            }
        }

    suspend fun getSubscriptionPayments(
        shopId: String,
        page: Int = 1,
        limit: Int = 10,
        provider: String? = null
    ): ApiResult<ApiResponse<List<PaymentDto>>> =
        execute {
            client.get("api/v1/subscription/payments") {
                header("X-Shop", shopId)
                parameter("page", page)
                parameter("limit", limit)
                provider?.let { parameter("provider", it) }
            }
        }

    suspend fun findSubscriptionByPaymentId(paymentId: String): ApiResult<ApiResponse<PaymentCheckResponse>> =
        execute { client.get("api/v1/subscription/payment/$paymentId") }

    // ==================== NOTIFICATIONS ====================

    suspend fun getNotifications(
        unreadOnly: Boolean = false,
        limit: Int = 20,
        offset: Int = 0
    ): ApiResult<ApiResponse<List<NotificationDto>>> =
        execute {
            client.get("notifications") {
                parameter("unreadOnly", unreadOnly)
                parameter("limit", limit)
                parameter("offset", offset)
            }
        }

    suspend fun markNotificationAsRead(notificationId: String): ApiResult<ApiResponse<Unit>> =
        execute { client.put("notifications/$notificationId/read") }

    suspend fun markAllNotificationsAsRead(): ApiResult<ApiResponse<Unit>> =
        execute { client.put("notifications/read-all") }

    // ==================== SUPPORT ====================

    suspend fun createSupportTicket(request: CreateSupportTicketRequest): ApiResult<ApiResponse<SupportTicketDto>> =
        execute { client.post("support/tickets") { setBody(request) } }

    suspend fun getFaqs(): ApiResult<ApiResponse<List<FaqDto>>> =
        execute { client.get("support/faqs") }
}
