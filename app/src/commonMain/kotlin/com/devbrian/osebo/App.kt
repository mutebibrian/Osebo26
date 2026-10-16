package com.devbrian.osebo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_more
import com.devbrian.osebo.resources.iconsax_nav_home
import com.devbrian.osebo.resources.iconsax_nav_inventory
import com.devbrian.osebo.resources.iconsax_nav_sales
import com.devbrian.osebo.resources.oseboicon
import org.jetbrains.compose.resources.painterResource
import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.createOseboHttpClient
import com.devbrian.osebo.data.remote.dto.request.CreateExpenseRequest
import com.devbrian.osebo.data.remote.dto.request.CreateShopRequest
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.request.SelectAccountRequest
import com.devbrian.osebo.data.remote.dto.request.SignUpRequest
import com.devbrian.osebo.data.remote.dto.response.PackageDto
import com.devbrian.osebo.data.remote.dto.response.ProductDto
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.remote.dto.response.ShopTypeDto
import com.devbrian.osebo.data.repository.SharedCustomerRepository
import com.devbrian.osebo.data.repository.SharedDashboardRepository
import com.devbrian.osebo.data.repository.SharedFinanceRepository
import com.devbrian.osebo.data.repository.SharedInventoryRepository
import com.devbrian.osebo.data.repository.SharedReportsRepository
import com.devbrian.osebo.data.repository.SharedSalesRepository
import com.devbrian.osebo.data.repository.buildReceiptState
import com.devbrian.osebo.data.repository.dateRangeForPeriod
import com.devbrian.osebo.data.repository.SaleCartItem
import com.devbrian.osebo.data.repository.cartSummary
import com.devbrian.osebo.data.remote.api.SupplierDto
import com.devbrian.osebo.data.remote.dto.request.AddSupplierRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateSupplierRequest
import com.devbrian.osebo.data.repository.SharedAiAssistantRepository
import com.devbrian.osebo.data.repository.SharedEmployeeRepository
import com.devbrian.osebo.data.repository.SharedShopDashboardRepository
import com.devbrian.osebo.data.repository.SharedShopsRepository
import com.devbrian.osebo.data.repository.SharedSubscriptionRepository
import com.devbrian.osebo.data.repository.formatSubscriptionAmount
import com.devbrian.osebo.data.repository.formatSubscriptionPhone
import com.devbrian.osebo.data.repository.toBundleUi
import com.devbrian.osebo.data.repository.validateSubscriptionPhone
import com.devbrian.osebo.data.repository.SharedSupplierRepository
import com.devbrian.osebo.data.repository.toSupplierItemUi
import com.devbrian.osebo.data.repository.countryDialCode
import com.devbrian.osebo.data.repository.defaultEmployeeRoles
import com.devbrian.osebo.data.repository.toAddStockProductUi
import com.devbrian.osebo.data.repository.toEmployeeItemUi
import com.devbrian.osebo.data.repository.toExpenseCategoryUi
import com.devbrian.osebo.data.repository.toFinanceTransactionUi
import com.devbrian.osebo.data.repository.toNewSaleCartItemUi
import com.devbrian.osebo.data.repository.toNewSaleCustomerUi
import com.devbrian.osebo.data.repository.toNewSaleProductUi
import com.devbrian.osebo.data.repository.toTransactionListItemUi
import com.devbrian.osebo.data.repository.toTransactionsUiState
import com.devbrian.osebo.data.remote.dto.request.EmployeeRequest
import com.devbrian.osebo.models.AiAssistantUiState
import com.devbrian.osebo.models.AiChatMessage
import com.devbrian.osebo.models.AiConversation
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.models.Employee
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.models.Role
import com.devbrian.osebo.models.ExpenseCategory
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.data.repository.currentDateLabel
import com.devbrian.osebo.data.repository.currentGreeting
import com.devbrian.osebo.data.settings.SettingsStoreSessionProvider
import com.devbrian.osebo.data.settings.createSettingsStore
import com.devbrian.osebo.ui.components.BarcodeScannerView
import com.devbrian.osebo.ui.components.BottomNavBar
import com.devbrian.osebo.ui.components.MoreMenu
import com.devbrian.osebo.ui.components.MoreMenuEntry
import com.devbrian.osebo.ui.components.SharedMoreMenuSection
import com.devbrian.osebo.ui.components.NavItem
import com.devbrian.osebo.ui.components.ShopPickerItem
import com.devbrian.osebo.ui.components.ShopPickerMenu
import com.devbrian.osebo.ui.screens.AddExpenseScreen
import com.devbrian.osebo.ui.screens.AddExpenseUiState
import com.devbrian.osebo.ui.screens.AddStockProductUi
import com.devbrian.osebo.data.remote.dto.response.StockCategoryDto
import com.devbrian.osebo.ui.screens.AddProductScreen
import com.devbrian.osebo.ui.screens.AddProductUiState
import com.devbrian.osebo.ui.screens.AddStockScreen
import com.devbrian.osebo.ui.screens.AddStockUiState
import com.devbrian.osebo.ui.screens.StockAdjustmentMode
import com.devbrian.osebo.ui.screens.CustomersScreen
import com.devbrian.osebo.ui.screens.CustomersUiState
import com.devbrian.osebo.ui.screens.DashboardScreen
import com.devbrian.osebo.ui.screens.DashboardUiState
import com.devbrian.osebo.ui.screens.EmployeeFormScreen
import com.devbrian.osebo.ui.screens.EmployeeFormUiState
import com.devbrian.osebo.ui.screens.EmployeesScreen
import com.devbrian.osebo.ui.screens.EmployeesUiState
import com.devbrian.osebo.ui.screens.ExpenseCategoriesScreen
import com.devbrian.osebo.ui.screens.ShopDashboardScreen
import com.devbrian.osebo.ui.screens.ShopDashboardUiState
import com.devbrian.osebo.data.repository.SharedAccountRepository
import com.devbrian.osebo.ui.screens.SharedAccountConfirmation
import com.devbrian.osebo.ui.screens.SharedAccountScreen
import com.devbrian.osebo.ui.screens.SharedAccountScreenState
import com.devbrian.osebo.ui.screens.SharedEditAccountScreen
import com.devbrian.osebo.ui.screens.SharedEditableAccountDetails
import com.devbrian.osebo.ui.screens.SharedPaymentMethodScreen
import com.devbrian.osebo.ui.screens.SignUpScreen
import com.devbrian.osebo.ui.screens.SignUpUiState
import com.devbrian.osebo.ui.screens.defaultFaqItems
import com.devbrian.osebo.ui.screens.FaqDetailDialog
import com.devbrian.osebo.ui.screens.FaqScreen
import com.devbrian.osebo.ui.screens.SharedContactInfoUi
import com.devbrian.osebo.ui.screens.SharedContactUsScreen
import com.devbrian.osebo.ui.screens.SharedSupportMessageUi
import com.devbrian.osebo.ui.screens.EditPermissionsState
import com.devbrian.osebo.ui.screens.PermissionCategoryUi
import com.devbrian.osebo.ui.screens.PermissionItemUi
import com.devbrian.osebo.ui.screens.ShopFormMode
import com.devbrian.osebo.ui.screens.ShopFormScreen
import com.devbrian.osebo.ui.screens.ShopFormUiState
import com.devbrian.osebo.ui.screens.ShopsScreen
import com.devbrian.osebo.ui.screens.UserRoleUi
import com.devbrian.osebo.ui.screens.UserRolesScreen
import com.devbrian.osebo.ui.screens.UserRolesUiState
import com.devbrian.osebo.ui.screens.ShopsUiState
import com.devbrian.osebo.ui.screens.SubscriptionPackagesScreen
import com.devbrian.osebo.ui.screens.SubscriptionPackagesUiState
import com.devbrian.osebo.ui.screens.SubscriptionPaymentScreen
import com.devbrian.osebo.ui.screens.SubscriptionPaymentUiState
import com.devbrian.osebo.ui.screens.SuppliersScreen
import com.devbrian.osebo.ui.screens.SuppliersUiState
import com.devbrian.osebo.ui.screens.SupplierFormDialog
import com.devbrian.osebo.ui.screens.SupplierFormUiState
import com.devbrian.osebo.ui.screens.ExpenseCategoryOption
import com.devbrian.osebo.ui.screens.ExpenseCategoryUi
import com.devbrian.osebo.ui.screens.NewSaleScreen
import com.devbrian.osebo.ui.screens.NewSaleUiState
import com.devbrian.osebo.ui.screens.OseboAiScreen
import com.devbrian.osebo.ui.screens.PaymentScreen
import com.devbrian.osebo.ui.screens.PaymentUiState
import com.devbrian.osebo.ui.screens.SalePaymentMethod
import com.devbrian.osebo.ui.screens.FinanceScreen
import com.devbrian.osebo.ui.screens.FinanceSettingsScreen
import com.devbrian.osebo.ui.screens.FinanceUiState
import com.devbrian.osebo.ui.screens.FinancialStatementScreen
import com.devbrian.osebo.ui.screens.FinancialStatementUiState
import com.devbrian.osebo.ui.screens.InventoryScreen
import com.devbrian.osebo.ui.screens.InventoryUiState
import com.devbrian.osebo.ui.screens.LoginScreen
import com.devbrian.osebo.ui.screens.LoginUiState
import com.devbrian.osebo.ui.screens.ReceiptScreen
import com.devbrian.osebo.ui.screens.ReceiptUiState
import com.devbrian.osebo.ui.screens.ReportsScreen
import com.devbrian.osebo.ui.screens.ReportsUiState
import com.devbrian.osebo.ui.screens.SalesScreen
import com.devbrian.osebo.ui.screens.SalesUiState
import com.devbrian.osebo.ui.screens.SelectAccountScreen
import com.devbrian.osebo.ui.screens.SelectAccountUiState
import com.devbrian.osebo.ui.screens.SplashScreen
import com.devbrian.osebo.ui.screens.TransactionFilterScreen
import com.devbrian.osebo.ui.screens.TransactionFilterUiState
import com.devbrian.osebo.ui.screens.TransactionsScreen
import com.devbrian.osebo.ui.screens.TransactionsUiState
import com.devbrian.osebo.ui.screens.WelcomeScreen
import com.devbrian.osebo.ui.theme.OseboTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

/**
 * Mirrors Android's real UserNavigationItem (Home, Inventory, Sales, More) —
 * Finance and Customers are not bottom-nav tabs on Android, they're reached
 * from the More sheet's Business/People sections instead (see MoreMenu
 * wiring below), so they're separate Destinations here, not AppTab entries.
 */
private enum class AppTab(val label: String) {
    Dashboard("Home"),
    Inventory("Inventory"),
    Sales("Sales"),
}

/**
 * Screens pushed on top of the tab shell (not peer tabs — each has its own
 * back button wired to return to MainTabs). Reached from the More sheet or
 * from another pushed screen's own callbacks; see the when-blocks below for
 * which callback triggers which push.
 */
private sealed class Destination {
    data object MainTabs : Destination()
    data object Reports : Destination()
    data object AddStock : Destination()
    data object RemoveStock : Destination()
    data object NewSale : Destination()
    data object Payment : Destination()
    data object Receipt : Destination()
    data object AddEmployee : Destination()
    data object Employees : Destination()
    data object Suppliers : Destination()
    data object Finance : Destination()
    data object Customers : Destination()
    data object Shops : Destination()
    data object ShopForm : Destination()
    data object ShopDashboard : Destination()
    data object AddProduct : Destination()
    data object AiAssistant : Destination()
    data object SubscriptionPackages : Destination()
    data object SubscriptionPayment : Destination()
    data object AddExpense : Destination()
    data object ExpenseCategories : Destination()
    data object FinancialStatement : Destination()
    data object FinanceSettings : Destination()
    data object AllTransactions : Destination()
    data object UserRoles : Destination()
    data object ContactUs : Destination()
    data object FaqList : Destination()
    data object Account : Destination()
}

/**
 * Mirrors UserRolesViewModel's own UserRole domain model exactly — including
 * that it's sample/placeholder data with no real backend behind it (see
 * loadUserRoles/saveUserRoleChanges below). Kept separate from
 * models.Role/PermissionType (the real, backend-driven permission system
 * gating the More menu): Android itself keeps these two permission
 * vocabularies — enum-based PermissionType and raw strings like
 * "customers_view" — unrelated and side by side, confusingly, but that's
 * its actual design, not something to unify here.
 */
private data class DemoUserRole(
    val id: String,
    val name: String,
    val description: String,
    val permissions: List<String>,
)

private fun DemoUserRole.toUserRoleUi() = UserRoleUi(id = id, name = name, description = description, permissionCount = permissions.size)

// Mirrors AddExpenseFragment's displayDateFormat ("dd MMM yyyy") using
// kotlinx-datetime instead of java.text.SimpleDateFormat.
private val ExpenseDateMonthAbbreviations = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

private fun kotlinx.datetime.LocalDate.toDisplayLabel(): String {
    val day = dayOfMonth.toString().padStart(2, '0')
    return "$day ${ExpenseDateMonthAbbreviations[month.ordinal]} $year"
}

/**
 * Mirrors the real Android launch flow: an animated Splash screen decides
 * between Welcome (logged out) and the main app (logged in). Welcome leads
 * to Login, whose password endpoint always returns a preAuthToken + account
 * list (never a direct token, per AuthRepository.kt on Android) — so
 * SelectingAccount is not an edge case, it's the only way LoggedIn is ever
 * reached.
 */
private sealed class AuthState {
    data object Splash : AuthState()
    data object LoggedOut : AuthState()
    data object EnteringCredentials : AuthState()
    data object SigningUp : AuthState()
    data class SelectingAccount(val preAuthToken: String) : AuthState()
    data class LoggedIn(val token: String) : AuthState()
}

/**
 * Root Compose Multiplatform entry point — a real navigable shell with a
 * real login flow and one screen (Customers) wired to live backend data.
 *
 * Data: on login, fetches the account's shops, takes the first one as the
 * active shop, then fetches its customers — proving the full pipe (Ktor ->
 * real backend -> DTO -> UI model -> screen) works end to end. The other 12
 * wired screens still show placeholder/empty state; wiring them the same
 * way is the natural next step, repository by repository.
 */
@Composable
fun App() {
    OseboTheme {
        val settingsStore = remember { createSettingsStore() }
        val sessionProvider = remember { SettingsStoreSessionProvider(settingsStore) }
        val httpClient = remember { createOseboHttpClient(sessionProvider) }
        val api = remember { KtorOseboApiService(httpClient, sessionProvider) }
        val customerRepository = remember { SharedCustomerRepository(api) }
        val dashboardRepository = remember { SharedDashboardRepository(api) }
        val salesRepository = remember { SharedSalesRepository(api) }
        val inventoryRepository = remember { SharedInventoryRepository(api) }
        val financeRepository = remember { SharedFinanceRepository(api) }
        val reportsRepository = remember { SharedReportsRepository(api) }
        val employeeRepository = remember { SharedEmployeeRepository(api) }
        val supplierRepository = remember { SharedSupplierRepository(api) }
        val shopsRepository = remember { SharedShopsRepository(api) }
        val accountRepository = remember { SharedAccountRepository(api) }
        val shopDashboardRepository = remember { SharedShopDashboardRepository(api) }
        val aiAssistantRepository = remember { SharedAiAssistantRepository(api) }
        val subscriptionRepository = remember { SharedSubscriptionRepository(api) }
        val scope = rememberCoroutineScope()

        var authState by remember { mutableStateOf<AuthState>(AuthState.Splash) }
        var loginState by remember { mutableStateOf(LoginUiState()) }
        var selectAccountState by remember { mutableStateOf(SelectAccountUiState()) }
        var signUpState by remember { mutableStateOf(SignUpUiState()) }
        var signUpUserId by remember { mutableStateOf<String?>(null) }
        var signUpPhone by remember { mutableStateOf("") }
        var selectedTab by remember { mutableStateOf(AppTab.Dashboard) }
        var destination by remember { mutableStateOf<Destination>(Destination.MainTabs) }
        var customersState by remember { mutableStateOf(CustomersUiState()) }
        var hasLoadedCustomers by remember { mutableStateOf(false) }
        var dashboardState by remember { mutableStateOf(DashboardUiState()) }
        var salesState by remember { mutableStateOf(SalesUiState()) }
        var inventoryState by remember { mutableStateOf(InventoryUiState()) }
        var financeState by remember { mutableStateOf(FinanceUiState()) }
        var reportsState by remember { mutableStateOf(ReportsUiState()) }
        var addExpenseState by remember { mutableStateOf(AddExpenseUiState()) }
        var addExpenseDate by remember { mutableStateOf("") }
        var isExpenseDatePickerOpen by remember { mutableStateOf(false) }
        var expenseCategoriesSource by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }
        var expenseCategoriesUi by remember { mutableStateOf<List<ExpenseCategoryUi>>(emptyList()) }
        var isExpenseCategoriesLoading by remember { mutableStateOf(false) }
        var financialStatementState by remember { mutableStateOf(FinancialStatementUiState()) }
        var transactionsState by remember { mutableStateOf(TransactionsUiState()) }
        var transactionsSource by remember { mutableStateOf<List<Transaction>>(emptyList()) }
        var transactionFilterState by remember { mutableStateOf(TransactionFilterUiState()) }
        var isTransactionFilterOpen by remember { mutableStateOf(false) }
        var addStockState by remember { mutableStateOf(AddStockUiState()) }
        var addStockSourceProducts by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
        var addProductState by remember { mutableStateOf(AddProductUiState()) }
        var productCategories by remember { mutableStateOf<List<StockCategoryDto>>(emptyList()) }
        var employeesState by remember { mutableStateOf(EmployeesUiState()) }
        var employeesSource by remember { mutableStateOf<List<Employee>>(emptyList()) }
        var employeeFormState by remember { mutableStateOf(EmployeeFormUiState()) }
        var employeeRoles by remember { mutableStateOf<List<Role>>(emptyList()) }
        var suppliersState by remember { mutableStateOf(SuppliersUiState()) }
        var suppliersSource by remember { mutableStateOf<List<SupplierDto>>(emptyList()) }
        var supplierEditorForm by remember { mutableStateOf<SupplierFormUiState?>(null) }
        var editingSupplier by remember { mutableStateOf<SupplierDto?>(null) }
        var supplierPendingDelete by remember { mutableStateOf<SupplierDto?>(null) }
        var shopsListState by remember { mutableStateOf(ShopsUiState()) }
        var shopFormState by remember { mutableStateOf(ShopFormUiState()) }
        var shopFormTypes by remember { mutableStateOf<List<ShopTypeDto>>(emptyList()) }
        var userRolesState by remember { mutableStateOf(UserRolesUiState()) }
        var userRolesSource by remember { mutableStateOf<List<DemoUserRole>>(emptyList()) }
        var userRolesEditCategories by remember { mutableStateOf<List<PermissionCategoryUi>>(emptyList()) }
        var contactInfo by remember {
            mutableStateOf(
                SharedContactInfoUi(
                    email = "support@osebo.ai",
                    phone = "+256 765 549811",
                    whatsapp = "+256 765 549811",
                    address = "Innovation Village, Ntinda Complex, Block C, 3rd floor",
                    workingHours = "Mon–Fri: 8:00 AM–6:00 PM",
                ),
            )
        }
        var isContactUsSubmitting by remember { mutableStateOf(false) }
        var contactUsResetKey by remember { mutableStateOf(0) }
        var faqDetailTarget by remember { mutableStateOf<Int?>(null) }
        var accountState by remember { mutableStateOf(SharedAccountScreenState()) }
        var accountConfirmation by remember { mutableStateOf<SharedAccountConfirmation?>(null) }
        var isEditAccountOpen by remember { mutableStateOf(false) }
        var isPaymentMethodOpen by remember { mutableStateOf(false) }
        var shopDashboardState by remember { mutableStateOf(ShopDashboardUiState()) }
        var isMoreMenuOpen by remember { mutableStateOf(false) }
        var isBarcodeScannerActive by remember { mutableStateOf(false) }
        var aiAssistantState by remember { mutableStateOf(AiAssistantUiState()) }
        var subscriptionPackagesState by remember { mutableStateOf(SubscriptionPackagesUiState()) }
        var subscriptionPackagesSource by remember { mutableStateOf<List<PackageDto>>(emptyList()) }
        var subscriptionPaymentState by remember { mutableStateOf(SubscriptionPaymentUiState()) }
        var subscriptionCheckoutPackageIds by remember { mutableStateOf<List<String>>(emptyList()) }
        var subscriptionCheckoutIsTrial by remember { mutableStateOf(false) }
        var newSaleState by remember { mutableStateOf(NewSaleUiState()) }
        var newSaleCart by remember { mutableStateOf<List<SaleCartItem>>(emptyList()) }
        var newSaleSourceProducts by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
        var newSaleQuery by remember { mutableStateOf("") }
        var newSaleCustomers by remember { mutableStateOf<List<Customer>>(emptyList()) }
        var selectedSaleCustomer by remember { mutableStateOf<Customer?>(null) }
        var paymentState by remember { mutableStateOf(PaymentUiState()) }
        var receiptState by remember { mutableStateOf(ReceiptUiState()) }
        var shops by remember { mutableStateOf<List<ShopDto>>(emptyList()) }
        var selectedShopId by remember { mutableStateOf<String?>(null) }
        var isShopPickerOpen by remember { mutableStateOf(false) }

        remember(sessionProvider) {
            sessionProvider.onSessionExpiredListener = {
                authState = AuthState.LoggedOut
                loginState = LoginUiState()
                selectAccountState = SelectAccountUiState()
                customersState = CustomersUiState()
                hasLoadedCustomers = false
                dashboardState = DashboardUiState()
                salesState = SalesUiState()
                inventoryState = InventoryUiState()
                financeState = FinanceUiState()
                reportsState = ReportsUiState()
                addExpenseState = AddExpenseUiState()
                addExpenseDate = ""
                expenseCategoriesSource = emptyList()
                expenseCategoriesUi = emptyList()
                financialStatementState = FinancialStatementUiState()
                transactionsState = TransactionsUiState()
                transactionsSource = emptyList()
                transactionFilterState = TransactionFilterUiState()
                isTransactionFilterOpen = false
                addStockState = AddStockUiState()
                addStockSourceProducts = emptyList()
                employeesState = EmployeesUiState()
                employeesSource = emptyList()
                employeeFormState = EmployeeFormUiState()
                employeeRoles = emptyList()
                suppliersState = SuppliersUiState()
                suppliersSource = emptyList()
                supplierEditorForm = null
                editingSupplier = null
                supplierPendingDelete = null
                shopFormState = ShopFormUiState()
                shopFormTypes = emptyList()
                userRolesState = UserRolesUiState()
                userRolesSource = emptyList()
                userRolesEditCategories = emptyList()
                isContactUsSubmitting = false
                contactUsResetKey = 0
                faqDetailTarget = null
                accountState = SharedAccountScreenState()
                accountConfirmation = null
                isEditAccountOpen = false
                isPaymentMethodOpen = false
                shopsListState = ShopsUiState()
                shopDashboardState = ShopDashboardUiState()
                isMoreMenuOpen = false
                aiAssistantState = AiAssistantUiState()
                subscriptionPackagesState = SubscriptionPackagesUiState()
                subscriptionPackagesSource = emptyList()
                subscriptionPaymentState = SubscriptionPaymentUiState()
                subscriptionCheckoutPackageIds = emptyList()
                subscriptionCheckoutIsTrial = false
                addProductState = AddProductUiState()
                productCategories = emptyList()
                newSaleState = NewSaleUiState()
                newSaleCart = emptyList()
                newSaleSourceProducts = emptyList()
                newSaleQuery = ""
                newSaleCustomers = emptyList()
                selectedSaleCustomer = null
                paymentState = PaymentUiState()
                receiptState = ReceiptUiState()
                shops = emptyList()
                selectedShopId = null
            }
        }

        LaunchedEffect(authState) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (hasLoadedCustomers) return@LaunchedEffect
            hasLoadedCustomers = true

            customersState = customersState.copy(isLoading = true, errorMessage = null)

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }

            if (shopId == null) {
                val message = when (shopsResult) {
                    is ApiResult.Error -> shopsResult.message
                    is ApiResult.NetworkError -> shopsResult.message
                    else -> "No shop found for this account"
                }
                customersState = customersState.copy(isLoading = false, errorMessage = message)
                return@LaunchedEffect
            }

            sessionProvider.saveCurrentShopId(shopId)

            when (val result = customerRepository.getCustomers(shopId)) {
                is ApiResult.Success -> {
                    customersState = CustomersUiState(
                        customers = result.data,
                        totalCustomersLabel = result.data.size.toString(),
                    )
                }
                is ApiResult.Error -> customersState = customersState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
                is ApiResult.NetworkError -> customersState = customersState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }
        }

        LaunchedEffect(authState, selectedShopId) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect

            dashboardState = dashboardState.copy(
                greeting = currentGreeting(),
                userName = sessionProvider.userFirstName() ?: "User",
                currentDate = currentDateLabel(),
                isLoading = true,
                errorMessage = null,
            )

            when (val result = dashboardRepository.loadShopsAndTotals(selectedShopId)) {
                is ApiResult.Success -> {
                    val data = result.data
                    shops = data.allShops
                    val selectedShop = selectedShopId?.let { id -> data.allShops.find { it.id == id } }
                    dashboardState = dashboardState.copy(
                        isLoading = false,
                        isShopSelected = selectedShop != null,
                        selectedShopName = selectedShop?.name.orEmpty(),
                        selectedShopType = selectedShop?.shopType.orEmpty(),
                        totalSales = data.totalSales,
                        totalExpenses = data.totalExpenses,
                        totalShopsLabel = data.totalShopsLabel,
                        todaySales = data.todaySales,
                        todayExpenses = data.todayExpenses,
                        todayBalance = data.todayBalance,
                        salesTrend = data.salesTrend,
                        expensesTrend = data.expensesTrend,
                        shopPerformances = data.shopPerformances,
                        shops = data.shops,
                    )
                }
                is ApiResult.Error -> dashboardState = dashboardState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
                is ApiResult.NetworkError -> dashboardState = dashboardState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }
        }

        LaunchedEffect(authState, selectedShopId) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            salesState = salesState.copy(isLoading = true)

            when (val result = salesRepository.getSales(shopId)) {
                is ApiResult.Success -> {
                    salesState = salesState.copy(
                        isLoading = false,
                        recentSales = result.data.recentSales,
                        totalSales = result.data.totalSales,
                    )
                }
                is ApiResult.Error -> salesState = salesState.copy(isLoading = false)
                is ApiResult.NetworkError -> salesState = salesState.copy(isLoading = false)
            }
        }

        LaunchedEffect(authState, selectedShopId) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            inventoryState = inventoryState.copy(isLoading = true)

            when (val result = inventoryRepository.getProducts(shopId)) {
                is ApiResult.Success -> {
                    inventoryState = inventoryState.copy(isLoading = false, products = result.data)
                }
                is ApiResult.Error -> inventoryState = inventoryState.copy(isLoading = false)
                is ApiResult.NetworkError -> inventoryState = inventoryState.copy(isLoading = false)
            }
        }

        LaunchedEffect(authState, selectedShopId, financeState.selectedPeriod) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            val (startDate, endDate) = dateRangeForPeriod(financeState.selectedPeriod)
            financeState = financeState.copy(isLoading = true)

            when (val result = financeRepository.getTransactions(shopId, startDate, endDate)) {
                is ApiResult.Success -> {
                    val income = result.data.filter { Transaction.isPositive(it.type) }.sumOf { it.amount }
                    val expenses = result.data.filter { Transaction.isNegative(it.type) }.sumOf { it.amount }
                    val profit = income - expenses
                    val margin = if (income > 0) profit / income * 100 else 0.0
                    financeState = financeState.copy(
                        isLoading = false,
                        transactions = result.data.map { it.toFinanceTransactionUi() },
                        totalIncome = "UGX ${income.toInt()}",
                        totalExpenses = "UGX ${expenses.toInt()}",
                        netProfit = "UGX ${profit.toInt()}",
                        profitMargin = "${(margin * 10).toInt() / 10.0}% profit margin",
                        isProfitPositive = profit >= 0,
                    )
                }
                is ApiResult.Error -> financeState = financeState.copy(isLoading = false)
                is ApiResult.NetworkError -> financeState = financeState.copy(isLoading = false)
            }
        }

        LaunchedEffect(authState, selectedShopId, reportsState.selectedPeriod) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            reportsState = reportsState.copy(isLoading = true)

            val period = reportsState.selectedPeriod.lowercase()
            val afterComparison = when (
                val result = reportsRepository.loadSalesComparison(shopId, period, reportsState)
            ) {
                is ApiResult.Success -> result.data
                else -> reportsState
            }
            val afterSummary = when (
                val result = reportsRepository.loadShopSummary(shopId, afterComparison)
            ) {
                is ApiResult.Success -> result.data
                else -> afterComparison
            }
            reportsState = afterSummary.copy(isLoading = false)
        }

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.AddExpense && destination != Destination.ExpenseCategories) {
                return@LaunchedEffect
            }

            if (destination == Destination.AddExpense && addExpenseDate.isEmpty()) {
                val monthAbbreviations = listOf(
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
                )
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                addExpenseDate = today.toString()
                addExpenseState = addExpenseState.copy(
                    dateLabel = "${today.dayOfMonth} ${monthAbbreviations[today.month.ordinal]} ${today.year}",
                )
            }

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            isExpenseCategoriesLoading = true
            when (val result = financeRepository.getExpenseCategories(shopId)) {
                is ApiResult.Success -> {
                    expenseCategoriesSource = result.data
                    expenseCategoriesUi = result.data.map { it.toExpenseCategoryUi() }
                    addExpenseState = addExpenseState.copy(
                        categories = result.data.map { ExpenseCategoryOption(id = it.id, name = it.name) },
                    )
                }
                is ApiResult.Error, is ApiResult.NetworkError -> Unit
            }
            isExpenseCategoriesLoading = false
        }

        LaunchedEffect(authState, selectedShopId, destination, financialStatementState.selectedPeriod) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.FinancialStatement) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            val (startDate, endDate) = dateRangeForPeriod(financialStatementState.selectedPeriod)
            financialStatementState = financialStatementState.copy(isLoading = true)

            when (val result = financeRepository.loadStatement(shopId, startDate, endDate)) {
                is ApiResult.Success -> {
                    val totals = result.data
                    val margin = if (totals.sales > 0) totals.netProfit / totals.sales * 100 else 0.0
                    financialStatementState = financialStatementState.copy(
                        isLoading = false,
                        sales = "UGX ${totals.sales.toInt()}",
                        purchases = "UGX ${totals.purchases.toInt()}",
                        expenses = "UGX ${totals.expenses.toInt()}",
                        inventory = "UGX 0",
                        grossMargin = "UGX ${totals.grossMargin.toInt()}",
                        netProfit = "UGX ${totals.netProfit.toInt()}",
                        profitMargin = "${(margin * 10).toInt() / 10.0}%",
                        isProfitPositive = totals.netProfit >= 0,
                    )
                }
                is ApiResult.Error -> financialStatementState = financialStatementState.copy(isLoading = false)
                is ApiResult.NetworkError -> financialStatementState = financialStatementState.copy(isLoading = false)
            }
        }

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.AllTransactions) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            transactionsState = transactionsState.copy(isLoading = true)

            when (val result = financeRepository.getTransactions(shopId)) {
                is ApiResult.Success -> {
                    transactionsSource = result.data
                    transactionFilterState = TransactionFilterUiState()
                    transactionsState = result.data.toTransactionsUiState()
                }
                is ApiResult.Error -> transactionsState = transactionsState.copy(isLoading = false)
                is ApiResult.NetworkError -> transactionsState = transactionsState.copy(isLoading = false)
            }
        }

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.AddStock && destination != Destination.RemoveStock) {
                return@LaunchedEffect
            }

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            addStockState = AddStockUiState(isLoading = true)
            when (val result = inventoryRepository.getRawProducts(shopId)) {
                is ApiResult.Success -> {
                    addStockSourceProducts = result.data
                    addStockState = addStockState.copy(
                        isLoading = false,
                        products = result.data.map { it.toAddStockProductUi() },
                    )
                }
                is ApiResult.Error -> addStockState = addStockState.copy(isLoading = false)
                is ApiResult.NetworkError -> addStockState = addStockState.copy(isLoading = false)
            }
        }

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.AddProduct) return@LaunchedEffect
            if (productCategories.isNotEmpty()) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            when (val result = inventoryRepository.getCategories(shopId)) {
                is ApiResult.Success -> productCategories = result.data
                is ApiResult.Error, is ApiResult.NetworkError -> Unit
            }
        }

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect

            if (destination == Destination.AddEmployee && employeeRoles.isEmpty()) {
                val shopId = selectedShopId ?: shops.firstOrNull()?.id ?: ""
                val roles = defaultEmployeeRoles(shopId, isMultiShopOwner = shops.size > 1)
                employeeRoles = roles
                employeeFormState = employeeFormState.copy(role = roles.firstOrNull()?.name.orEmpty())
            }

            if (destination != Destination.Employees) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            employeesState = employeesState.copy(isLoading = true)
            when (val result = employeeRepository.getEmployees(shopId)) {
                is ApiResult.Success -> {
                    employeesSource = result.data
                    employeesState = employeesState.copy(
                        isLoading = false,
                        employees = result.data.map { it.toEmployeeItemUi() },
                    )
                }
                is ApiResult.Error -> employeesState = employeesState.copy(isLoading = false)
                is ApiResult.NetworkError -> employeesState = employeesState.copy(isLoading = false)
            }
        }

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.Suppliers) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

            suppliersState = suppliersState.copy(isLoading = true)
            when (val result = supplierRepository.getSuppliers(shopId)) {
                is ApiResult.Success -> {
                    suppliersSource = result.data
                    suppliersState = suppliersState.copy(
                        isLoading = false,
                        suppliers = result.data.map { it.toSupplierItemUi() },
                    )
                }
                is ApiResult.Error -> suppliersState = suppliersState.copy(isLoading = false)
                is ApiResult.NetworkError -> suppliersState = suppliersState.copy(isLoading = false)
            }
        }

        LaunchedEffect(authState, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.Shops) return@LaunchedEffect

            shopsListState = shopsListState.copy(isLoading = true)
            when (val result = shopsRepository.getShops(selectedShopId)) {
                is ApiResult.Success -> shopsListState = shopsListState.copy(isLoading = false, shops = result.data)
                is ApiResult.Error -> shopsListState = shopsListState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
                is ApiResult.NetworkError -> shopsListState = shopsListState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }
        }

        // Mirrors ShopCreationActivity.loadShopTypes, including its
        // hardcoded fallback pair when the endpoint is unreachable.
        LaunchedEffect(authState, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.ShopForm) return@LaunchedEffect

            shopFormState = ShopFormUiState(mode = ShopFormMode.Create, isTypeLoading = true)
            val loaded = when (val result = shopsRepository.getShopTypes()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> emptyList()
                is ApiResult.NetworkError -> emptyList()
            }
            if (loaded.isNotEmpty()) {
                shopFormTypes = loaded
                shopFormState = shopFormState.copy(type = loaded.first().name, isTypeLoading = false)
            } else {
                // Mirrors ShopCreationActivity.useFallbackShopTypes exactly,
                // including its two hardcoded backend type IDs.
                shopFormTypes = listOf(
                    ShopTypeDto(id = "a5613dae-6297-448c-a022-40f7ef1f57f4", name = "Retail"),
                    ShopTypeDto(id = "9423230e-df5a-4669-8fd8-193e11ce72b0", name = "Other"),
                )
                shopFormState = shopFormState.copy(
                    type = "Retail",
                    isTypeLoading = false,
                    submitError = "Default shop types are available while the full list is offline.",
                )
            }
        }

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.ShopDashboard) return@LaunchedEffect
            val shopId = selectedShopId ?: return@LaunchedEffect

            shopDashboardState = shopDashboardState.copy(
                shopName = shops.firstOrNull { it.id == shopId }?.name ?: "Your shop",
                isLoading = true,
            )
            when (val result = shopDashboardRepository.loadDashboard(shopId)) {
                is ApiResult.Success -> {
                    val data = result.data
                    shopDashboardState = shopDashboardState.copy(
                        hasData = true,
                        isLoading = false,
                        errorMessage = null,
                        employeesCount = data.employeesCount,
                        suppliersCount = data.suppliersCount,
                        customersCount = data.customersCount,
                        totalSales = data.totalSales,
                        estimatedProfit = data.estimatedProfit,
                        periodSales = data.periodSales,
                        periodExpenses = data.periodExpenses,
                        periodProfit = data.periodProfit,
                        salesTrend = data.salesTrend,
                        trendLabels = data.trendLabels,
                        topProducts = data.topProducts,
                    )
                }
                is ApiResult.Error -> shopDashboardState = shopDashboardState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
                is ApiResult.NetworkError -> shopDashboardState = shopDashboardState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }
        }

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.NewSale) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) {
                val message = when (shopsResult) {
                    is ApiResult.Error -> shopsResult.message
                    is ApiResult.NetworkError -> shopsResult.message
                    else -> "No shop found for this account"
                }
                newSaleState = NewSaleUiState(errorMessage = message)
                return@LaunchedEffect
            }

            newSaleCart = emptyList()
            newSaleQuery = ""
            newSaleState = NewSaleUiState(
                shopName = shops.firstOrNull { it.id == shopId }?.name ?: "Selected shop",
                isLoading = true,
            )

            when (val result = inventoryRepository.getRawProducts(shopId)) {
                is ApiResult.Success -> {
                    newSaleSourceProducts = result.data
                    newSaleState = newSaleState.copy(
                        isLoading = false,
                        products = result.data.map { it.toNewSaleProductUi(cartQuantity = 0) },
                    )
                }
                is ApiResult.Error -> newSaleState = newSaleState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
                is ApiResult.NetworkError -> newSaleState = newSaleState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }

            when (val result = customerRepository.getRawCustomers(shopId)) {
                is ApiResult.Success -> {
                    newSaleCustomers = result.data
                    newSaleState = newSaleState.copy(customers = result.data.map { it.toNewSaleCustomerUi() })
                    val defaultCustomer = selectedSaleCustomer ?: result.data.firstOrNull(Customer::isDefault)
                    selectedSaleCustomer = defaultCustomer
                    newSaleState = newSaleState.copy(
                        selectedCustomerId = defaultCustomer?.id.orEmpty(),
                        selectedCustomerName = defaultCustomer?.name?.ifBlank { "Walk-in Customer" }
                            ?: "Walk-in Customer",
                    )
                }
                is ApiResult.Error, is ApiResult.NetworkError -> Unit
            }
        }

        fun attemptSignIn() {
            loginState = loginState.copy(isLoading = true, errorMessage = null)
            scope.launch {
                when (
                    val result = api.signInWithPassword(
                        LoginRequest(username = loginState.username, password = loginState.password),
                    )
                ) {
                    is ApiResult.Success -> {
                        val preAuthData = result.data.data
                        if (result.data.success && preAuthData != null && preAuthData.accounts.isNotEmpty()) {
                            loginState = LoginUiState()
                            selectAccountState = SelectAccountUiState(accounts = preAuthData.accounts)
                            authState = AuthState.SelectingAccount(preAuthData.preAuthToken)
                        } else {
                            loginState = loginState.copy(
                                isLoading = false,
                                errorMessage = result.data.message.ifBlank { "Sign in failed" },
                            )
                        }
                    }
                    is ApiResult.Error -> loginState = loginState.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                    is ApiResult.NetworkError -> loginState = loginState.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
            }
        }

        fun attemptSelectAccount(preAuthToken: String, accountId: String) {
            selectAccountState = selectAccountState.copy(isLoading = true, errorMessage = null)
            scope.launch {
                when (val result = api.selectAccount(SelectAccountRequest(preAuthToken, accountId))) {
                    is ApiResult.Success -> {
                        val authData = result.data.data
                        val token = authData?.accessToken
                        if (result.data.success && !token.isNullOrBlank()) {
                            sessionProvider.saveAuthToken(token)
                            authData?.refreshToken?.let { sessionProvider.saveRefreshToken(it) }
                            val firstName = authData?.user?.extractFirstName()?.takeIf { it.isNotBlank() } ?: "User"
                            sessionProvider.saveUserFirstName(firstName)
                            sessionProvider.saveAccountId(accountId)
                            sessionProvider.saveRole(authData?.user?.role)
                            sessionProvider.savePermissions(authData?.user?.permissions.orEmpty())
                            selectAccountState = SelectAccountUiState()
                            authState = AuthState.LoggedIn(token)
                        } else {
                            selectAccountState = selectAccountState.copy(
                                isLoading = false,
                                errorMessage = result.data.message ?: "Could not select account",
                            )
                        }
                    }
                    is ApiResult.Error -> selectAccountState = selectAccountState.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                    is ApiResult.NetworkError -> selectAccountState = selectAccountState.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
            }
        }

        // Mirrors SignUpActivity.requestOtp: sends a sign-in-style OTP to
        // the phone number to prove it's real before letting the user
        // actually register.
        fun requestSignUpOtp() {
            val phoneClean = signUpState.phone.trim().replace(" ", "")
            if (phoneClean.isEmpty()) {
                signUpState = signUpState.copy(phoneError = "Phone required")
                return
            }
            if (phoneClean.length < 6) {
                signUpState = signUpState.copy(phoneError = "Invalid phone number")
                return
            }
            signUpPhone = signUpState.countryCode + phoneClean
            signUpState = signUpState.copy(isLoading = true, phoneError = null, submitError = null)
            scope.launch {
                when (val result = api.requestSignUpOtp(signUpPhone)) {
                    is ApiResult.Success -> {
                        val data = result.data.data
                        if (result.data.success && !data?.userId.isNullOrEmpty()) {
                            signUpUserId = data?.userId
                            signUpState = signUpState.copy(isLoading = false, isOtpMode = true)
                        } else {
                            signUpState = signUpState.copy(
                                isLoading = false,
                                submitError = result.data.message.ifBlank { "Failed to send verification code" },
                            )
                        }
                    }
                    is ApiResult.Error -> signUpState = signUpState.copy(isLoading = false, submitError = result.message)
                    is ApiResult.NetworkError -> signUpState = signUpState.copy(isLoading = false, submitError = result.message)
                }
            }
        }

        // Mirrors SignUpActivity.verifyAndSignup.
        fun verifyAndSignUp() {
            val code = signUpState.otp.trim()
            if (code.length != 6) {
                signUpState = signUpState.copy(otpError = "Enter 6-digit code")
                return
            }
            val userId = signUpUserId
            if (userId == null) {
                signUpState = signUpState.copy(isOtpMode = false, submitError = "Session expired. Try again.")
                return
            }
            val firstName = signUpState.firstName.trim()
            val lastName = signUpState.lastName.trim()
            val email = signUpState.email.trim()
            if (firstName.isEmpty() || lastName.isEmpty()) {
                signUpState = signUpState.copy(submitError = "First and last name required")
                return
            }
            if (!signUpState.acceptsTerms) {
                signUpState = signUpState.copy(submitError = "Accept Terms and Conditions")
                return
            }
            if (email.isNotEmpty() && (!email.contains("@") || !email.substringAfter("@").contains("."))) {
                signUpState = signUpState.copy(emailError = "Invalid email")
                return
            }
            signUpState = signUpState.copy(isLoading = true, submitError = null)
            scope.launch {
                val verified = when (val verifyResult = api.verifySignUpOtp(userId, code)) {
                    is ApiResult.Success -> verifyResult.data.success
                    is ApiResult.Error -> false
                    is ApiResult.NetworkError -> false
                }
                if (!verified) {
                    signUpState = signUpState.copy(isLoading = false, otpError = "Invalid OTP")
                    return@launch
                }
                val signUpResult = api.signUp(
                    SignUpRequest(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phone = signUpPhone,
                        title = signUpState.title,
                        otp = code,
                    ),
                )
                when (signUpResult) {
                    is ApiResult.Success -> {
                        if (signUpResult.data.success) {
                            signUpState = SignUpUiState()
                            signUpUserId = null
                            signUpPhone = ""
                            authState = AuthState.EnteringCredentials
                        } else {
                            signUpState = signUpState.copy(
                                isLoading = false,
                                submitError = signUpResult.data.message.ifBlank { "Signup failed" },
                            )
                        }
                    }
                    is ApiResult.Error -> signUpState = signUpState.copy(isLoading = false, submitError = signUpResult.message)
                    is ApiResult.NetworkError -> signUpState = signUpState.copy(isLoading = false, submitError = signUpResult.message)
                }
            }
        }

        fun saveExpense() {
            val amount = addExpenseState.amount.toDoubleOrNull()
            val categoryError = if (addExpenseState.selectedCategoryId == null) "Select a category" else null
            val amountError = when {
                addExpenseState.amount.isBlank() -> "Enter an amount"
                amount == null -> "Enter a valid amount"
                amount <= 0 -> "Amount must be greater than 0"
                else -> null
            }
            val descriptionError = if (addExpenseState.description.isBlank()) "Enter a description" else null
            val paymentError = if (addExpenseState.paymentMethod.isBlank()) "Select a payment method" else null

            addExpenseState = addExpenseState.copy(
                categoryError = categoryError,
                amountError = amountError,
                descriptionError = descriptionError,
                paymentError = paymentError,
            )
            if (categoryError != null || amountError != null || descriptionError != null || paymentError != null) {
                return
            }

            val shopId = selectedShopId ?: shops.firstOrNull()?.id ?: return
            val categoryId = addExpenseState.selectedCategoryId ?: return
            addExpenseState = addExpenseState.copy(isLoading = true)
            scope.launch {
                val request = CreateExpenseRequest(
                    name = addExpenseState.description.take(100),
                    description = addExpenseState.description.trim(),
                    amount = amount!!,
                    expenseCategoryId = categoryId,
                    date = addExpenseDate,
                    paymentMethod = addExpenseState.paymentMethod,
                    reference = addExpenseState.reference.trim().ifEmpty { null },
                )
                when (financeRepository.createExpense(shopId, request)) {
                    is ApiResult.Success -> {
                        addExpenseState = AddExpenseUiState(categories = addExpenseState.categories)
                        addExpenseDate = ""
                        destination = Destination.MainTabs
                    }
                    is ApiResult.Error -> addExpenseState = addExpenseState.copy(isLoading = false)
                    is ApiResult.NetworkError -> addExpenseState = addExpenseState.copy(isLoading = false)
                }
            }
        }

        fun createExpenseCategory(name: String, description: String?) {
            val shopId = selectedShopId ?: shops.firstOrNull()?.id ?: return
            isExpenseCategoriesLoading = true
            scope.launch {
                when (val result = financeRepository.createExpenseCategory(shopId, name, description)) {
                    is ApiResult.Success -> {
                        expenseCategoriesSource = expenseCategoriesSource + result.data
                        expenseCategoriesUi = expenseCategoriesUi + result.data.toExpenseCategoryUi()
                    }
                    is ApiResult.Error, is ApiResult.NetworkError -> Unit
                }
                isExpenseCategoriesLoading = false
            }
        }

        fun updateExpenseCategory(category: ExpenseCategoryUi, name: String, description: String?) {
            val shopId = selectedShopId ?: shops.firstOrNull()?.id ?: return
            isExpenseCategoriesLoading = true
            scope.launch {
                when (val result = financeRepository.updateExpenseCategory(shopId, category.id, name, description)) {
                    is ApiResult.Success -> {
                        expenseCategoriesSource = expenseCategoriesSource.map { if (it.id == category.id) result.data else it }
                        expenseCategoriesUi = expenseCategoriesSource.map { it.toExpenseCategoryUi() }
                    }
                    is ApiResult.Error, is ApiResult.NetworkError -> Unit
                }
                isExpenseCategoriesLoading = false
            }
        }

        fun deleteExpenseCategory(category: ExpenseCategoryUi) {
            val shopId = selectedShopId ?: shops.firstOrNull()?.id ?: return
            isExpenseCategoriesLoading = true
            scope.launch {
                when (financeRepository.deleteExpenseCategory(shopId, category.id)) {
                    is ApiResult.Success -> {
                        expenseCategoriesSource = expenseCategoriesSource.filterNot { it.id == category.id }
                        expenseCategoriesUi = expenseCategoriesUi.filterNot { it.id == category.id }
                    }
                    is ApiResult.Error, is ApiResult.NetworkError -> Unit
                }
                isExpenseCategoriesLoading = false
            }
        }

        fun sanitizeStockQuantity(raw: String, allowsDecimal: Boolean): String {
            if (!allowsDecimal) return raw.filter(Char::isDigit)
            var decimalSeen = false
            return buildString {
                raw.forEach { character ->
                    when {
                        character.isDigit() -> append(character)
                        character == '.' && !decimalSeen -> {
                            append(character)
                            decimalSeen = true
                        }
                    }
                }
            }
        }

        fun formatStockQuantity(product: ProductDto, quantity: Double): String = if (product.allowsFloatQuantity) {
            (kotlin.math.round(quantity * 10) / 10).toString()
        } else {
            quantity.toInt().toString()
        }

        fun updateStockQuantity(mode: StockAdjustmentMode, productId: String, rawQuantity: String) {
            val product = addStockSourceProducts.firstOrNull { it.id == productId } ?: return
            val sanitized = sanitizeStockQuantity(rawQuantity, product.allowsFloatQuantity).take(9)
            val requested = sanitized.toDoubleOrNull()
            val bounded = when {
                sanitized.isBlank() -> ""
                mode == StockAdjustmentMode.Remove && requested != null && requested > product.quantity ->
                    formatStockQuantity(product, product.quantity)
                else -> sanitized
            }
            addStockState = addStockState.copy(
                quantities = addStockState.quantities.toMutableMap().apply {
                    if (bounded.isBlank()) remove(productId) else put(productId, bounded)
                },
            )
        }

        fun adjustStockQuantity(mode: StockAdjustmentMode, productId: String, increase: Boolean) {
            val product = addStockSourceProducts.firstOrNull { it.id == productId } ?: return
            val current = addStockState.quantities[productId]?.toDoubleOrNull() ?: 0.0
            val step = if (product.allowsFloatQuantity) 0.1 else 1.0
            val updated = if (increase) {
                if (mode == StockAdjustmentMode.Remove) {
                    (current + step).coerceAtMost(product.quantity)
                } else {
                    current + step
                }
            } else {
                (current - step).coerceAtLeast(0.0)
            }
            updateStockQuantity(mode, productId, if (updated == 0.0) "" else formatStockQuantity(product, updated))
        }

        fun performStockAdjustment(mode: StockAdjustmentMode) {
            if (addStockState.isProcessing) return
            val selected = addStockState.quantities.mapNotNull { (productId, rawQuantity) ->
                val product = addStockSourceProducts.firstOrNull { it.id == productId }
                val quantity = rawQuantity.toDoubleOrNull() ?: 0.0
                if (product != null && quantity > 0.0) product to quantity else null
            }
            if (selected.isEmpty()) return

            val shopId = selectedShopId ?: shops.firstOrNull()?.id ?: return
            addStockState = addStockState.copy(isProcessing = true)
            scope.launch {
                selected.forEach { (product, quantity) ->
                    val newQuantity = if (mode == StockAdjustmentMode.Remove) {
                        (product.quantity - quantity).coerceAtLeast(0.0)
                    } else {
                        product.quantity + quantity
                    }
                    inventoryRepository.updateProductQuantity(shopId, product.id, newQuantity)
                }
                addStockState = AddStockUiState()
                destination = Destination.MainTabs
                when (val result = inventoryRepository.getProducts(shopId)) {
                    is ApiResult.Success -> inventoryState = inventoryState.copy(products = result.data)
                    else -> Unit
                }
            }
        }

        fun saveAddProductForm() {
            val state = addProductState
            val nameError = "Product name is required".takeIf { state.name.isBlank() }
            val skuError = "SKU is required".takeIf { state.sku.isBlank() }
            val categoryError = "Category is required".takeIf { state.category.isBlank() }
            val price = state.price.toDoubleOrNull()
            val priceError = when {
                state.price.isBlank() -> "Selling price is required"
                price == null -> "Enter a valid price"
                price < 0 -> "Price cannot be negative"
                else -> null
            }
            val cost = state.cost.toDoubleOrNull()
            val costError = when {
                state.cost.isBlank() -> null
                cost == null -> "Enter a valid cost"
                cost < 0 -> "Cost cannot be negative"
                else -> null
            }
            val stock = state.stock.toDoubleOrNull()
            val stockError = when {
                state.stock.isBlank() -> "Current stock is required"
                stock == null -> "Enter a valid quantity"
                stock < 0 -> "Stock cannot be negative"
                else -> null
            }
            val threshold = state.lowStockThreshold.toIntOrNull()
            val thresholdError = when {
                state.lowStockThreshold.isBlank() -> "Low-stock alert is required"
                threshold == null -> "Enter a whole number"
                threshold < 0 -> "Alert cannot be negative"
                else -> null
            }
            val tax = state.taxRate.toDoubleOrNull()
            val taxError = when {
                state.taxRate.isBlank() -> null
                tax == null -> "Enter a valid rate"
                tax !in 0.0..100.0 -> "Use a rate from 0 to 100"
                else -> null
            }
            val weight = state.weight.toDoubleOrNull()
            val weightError = when {
                state.weight.isBlank() -> null
                weight == null -> "Enter a valid weight"
                weight < 0 -> "Weight cannot be negative"
                else -> null
            }

            val detailsInvalid = nameError != null || skuError != null || categoryError != null
            val stockInvalid = priceError != null || costError != null || stockError != null ||
                thresholdError != null || taxError != null || weightError != null
            addProductState = state.copy(
                currentStep = if (detailsInvalid) 0 else 1,
                nameError = nameError,
                skuError = skuError,
                categoryError = categoryError,
                priceError = priceError,
                costError = costError,
                stockError = stockError,
                thresholdError = thresholdError,
                taxError = taxError,
                weightError = weightError,
            )
            if (detailsInvalid || stockInvalid) return

            val category = productCategories.firstOrNull { it.name == state.category }
            if (category == null) {
                addProductState = addProductState.copy(currentStep = 0, categoryError = "Choose a valid category")
                return
            }

            val shopId = selectedShopId ?: shops.firstOrNull()?.id
            if (shopId == null) {
                addProductState = addProductState.copy(isLoading = false)
                return
            }

            addProductState = addProductState.copy(isLoading = true)
            scope.launch {
                val result = inventoryRepository.createProduct(
                    shopId = shopId,
                    name = state.name.trim(),
                    sku = state.sku.trim(),
                    description = state.description.trim().ifBlank { null },
                    lowQuantityMark = threshold.toString(),
                    purchasePrice = (cost ?: 0.0).toString(),
                    sellingPrice = price.toString(),
                    quantity = stock.toString(),
                    unitMeasure = state.unit.ifBlank { "Piece" },
                    stockCategoryId = category.id,
                )
                when (result) {
                    is ApiResult.Success -> {
                        addProductState = AddProductUiState()
                        destination = Destination.MainTabs
                        selectedTab = AppTab.Inventory
                        when (val refreshed = inventoryRepository.getProducts(shopId)) {
                            is ApiResult.Success -> inventoryState = inventoryState.copy(products = refreshed.data)
                            else -> Unit
                        }
                    }
                    is ApiResult.Error -> addProductState = addProductState.copy(isLoading = false)
                    is ApiResult.NetworkError -> addProductState = addProductState.copy(isLoading = false)
                }
            }
        }

        fun refreshNewSaleUiState() {
            val quantities = newSaleCart.associate { it.product.id to it.quantity }
            val filteredProducts = if (newSaleQuery.isBlank()) {
                newSaleSourceProducts
            } else {
                newSaleSourceProducts.filter {
                    it.name.contains(newSaleQuery, ignoreCase = true) || it.sku.contains(newSaleQuery, ignoreCase = true)
                }
            }
            val summary = newSaleCart.cartSummary()
            newSaleState = newSaleState.copy(
                products = filteredProducts.map { it.toNewSaleProductUi(cartQuantity = quantities[it.id] ?: 0) },
                cartItems = newSaleCart.map { it.toNewSaleCartItemUi() },
                subtotal = "UGX ${summary.subtotal.toInt()}",
                discount = "UGX ${summary.totalDiscount.toInt()}",
                tax = "UGX ${summary.tax.toInt()}",
                total = "UGX ${summary.total.toInt()}",
            )
        }

        fun searchNewSaleProducts(query: String) {
            newSaleQuery = query
            refreshNewSaleUiState()
        }

        fun addProductToNewSale(productId: String) {
            val product = newSaleSourceProducts.firstOrNull { it.id == productId } ?: return
            val existing = newSaleCart.firstOrNull { it.product.id == productId }
            newSaleCart = if (existing != null) {
                newSaleCart.map { if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it }
            } else {
                newSaleCart + SaleCartItem(product = product, quantity = 1)
            }
            refreshNewSaleUiState()
        }

        // Mirrors NewSaleFragment.searchProductByBarcode on Android: an exact
        // barcode match adds straight to the cart instead of just filtering
        // the product list, so scanning feels instant.
        fun addProductByBarcode(barcode: String) {
            val product = newSaleSourceProducts.firstOrNull { it.barcode == barcode }
            if (product == null) {
                newSaleState = newSaleState.copy(errorMessage = "No product found for this barcode")
            } else {
                addProductToNewSale(product.id)
            }
        }

        // Mirrors SalesViewModel.holdSale on Android: it's a toast-only stub
        // there too (no real persistence, cart isn't even cleared), so this
        // matches that behavior exactly rather than building a real one.
        fun holdNewSale() {
            newSaleState = newSaleState.copy(successMessage = "Sale held successfully")
        }

        fun changeNewSaleQuantity(productId: String, quantity: Int) {
            newSaleCart = if (quantity <= 0) {
                newSaleCart.filterNot { it.product.id == productId }
            } else {
                newSaleCart.map { if (it.product.id == productId) it.copy(quantity = quantity) else it }
            }
            refreshNewSaleUiState()
        }

        fun removeNewSaleItem(productId: String) {
            newSaleCart = newSaleCart.filterNot { it.product.id == productId }
            refreshNewSaleUiState()
        }

        fun applyNewSaleDiscount(productId: String, discount: Double) {
            newSaleCart = newSaleCart.map { if (it.product.id == productId) it.copy(discount = discount) else it }
            refreshNewSaleUiState()
        }

        fun selectNewSaleCustomer(customerId: String) {
            val customer = newSaleCustomers.firstOrNull { it.id == customerId } ?: return
            selectedSaleCustomer = customer
            newSaleState = newSaleState.copy(
                selectedCustomerId = customer.id,
                selectedCustomerName = customer.name.ifBlank { "Walk-in Customer" },
            )
        }

        fun clearNewSaleCart() {
            newSaleCart = emptyList()
            refreshNewSaleUiState()
        }

        fun proceedToPaymentScreen() {
            val customer = selectedSaleCustomer ?: return
            if (newSaleCart.isEmpty()) return
            val summary = newSaleCart.cartSummary()
            paymentState = PaymentUiState(
                shopName = newSaleState.shopName,
                customerLabel = customer.name.ifBlank { "Walk-in Customer" },
                customerDetail = customer.id,
                uniqueItemCount = newSaleCart.size,
                totalQuantity = newSaleCart.sumOf { it.quantity },
                total = "UGX ${summary.total.toInt()}",
            )
            destination = Destination.Payment
        }

        fun proceedWithPaymentSubmit() {
            val shopId = selectedShopId ?: shops.firstOrNull()?.id ?: return
            val customer = selectedSaleCustomer
            val totalAmount = newSaleCart.cartSummary().total
            paymentState = paymentState.copy(isLoading = true)
            scope.launch {
                val notes = buildString {
                    paymentState.notes.trim().takeIf { it.isNotEmpty() }?.let { append(it) }
                    paymentState.reference.trim().takeIf { it.isNotEmpty() }?.let {
                        if (isNotEmpty()) append(", ")
                        append("Ref: $it")
                    }
                    paymentState.phoneNumber.trim().takeIf { it.isNotEmpty() }?.let {
                        if (isNotEmpty()) append(", ")
                        append("Phone: $it")
                    }
                }.ifEmpty { null }

                when (
                    val result = salesRepository.createSale(
                        shopId = shopId,
                        customerId = customer?.id,
                        cartItems = newSaleCart,
                        paidAmount = totalAmount,
                        notes = notes,
                    )
                ) {
                    is ApiResult.Success -> {
                        val sale = result.data.data
                        val cartSnapshot = newSaleCart
                        val paymentMethod = paymentState.selectedMethod.apiValue
                        if (sale != null) {
                            receiptState = buildReceiptState(
                                sale = sale,
                                cartItems = cartSnapshot,
                                customerName = customer?.name.orEmpty(),
                                customerPhone = customer?.phone.orEmpty(),
                                servedBy = sessionProvider.userFirstName().orEmpty(),
                                paidAmount = totalAmount,
                                paymentMethod = paymentMethod,
                            )
                        }
                        paymentState = PaymentUiState()
                        newSaleCart = emptyList()
                        newSaleQuery = ""
                        // Falls back to MainTabs/Sales on the rare case the server's own
                        // create-sale response omits `data` — the sale itself still
                        // succeeded, there's just nothing to build a receipt from.
                        destination = if (sale != null) Destination.Receipt else Destination.MainTabs
                        selectedTab = AppTab.Sales
                        when (val salesResult = salesRepository.getSales(shopId)) {
                            is ApiResult.Success -> salesState = salesState.copy(
                                recentSales = salesResult.data.recentSales,
                                totalSales = salesResult.data.totalSales,
                            )
                            else -> Unit
                        }
                    }
                    is ApiResult.Error -> paymentState = paymentState.copy(isLoading = false, amountError = result.message)
                    is ApiResult.NetworkError -> paymentState = paymentState.copy(
                        isLoading = false,
                        amountError = result.message,
                    )
                }
            }
        }

        fun processPaymentSubmit() {
            val totalAmount = newSaleCart.cartSummary().total
            val tendered = paymentState.amountTendered.toDoubleOrNull()
            when {
                paymentState.amountTendered.isBlank() || tendered == null -> {
                    paymentState = paymentState.copy(amountError = "Enter the amount received")
                }
                tendered < totalAmount -> {
                    paymentState = paymentState.copy(amountError = "Amount is less than the sale total")
                }
                tendered > totalAmount -> {
                    paymentState = paymentState.copy(amountError = null, showOverpaymentConfirmation = true)
                }
                else -> proceedWithPaymentSubmit()
            }
        }

        fun updatePaymentAmount(raw: String) {
            val normalized = raw.filterIndexed { index, character ->
                character.isDigit() || (character == '.' && raw.indexOf('.') == index)
            }
            val totalAmount = newSaleCart.cartSummary().total
            val tendered = normalized.toDoubleOrNull() ?: 0.0
            val change = (tendered - totalAmount).coerceAtLeast(0.0)
            paymentState = paymentState.copy(
                amountTendered = normalized,
                amountError = null,
                change = "UGX ${change.toInt()}",
            )
        }

        fun continueEmployeeForm() {
            val state = employeeFormState
            val firstNameError = "First name is required".takeIf { state.firstName.trim().isEmpty() }
            val lastNameError = "Last name is required".takeIf { state.lastName.trim().isEmpty() }
            val roleError = "Choose a role".takeIf { employeeRoles.none { it.name == state.role } }
            val phoneError = when {
                state.phone.isBlank() -> "Phone number is required"
                state.phone.length < 9 -> "Use at least 9 digits"
                else -> null
            }
            val passwordError = when {
                state.password.isEmpty() -> "Password is required"
                state.password.length < 6 -> "Use at least 6 characters"
                else -> null
            }
            val confirmPasswordError = when {
                state.confirmPassword.isEmpty() -> "Confirm the password"
                state.confirmPassword != state.password -> "Passwords do not match"
                else -> null
            }
            val hasError = listOf(
                firstNameError, lastNameError, roleError, phoneError, passwordError, confirmPasswordError,
            ).any { it != null }
            employeeFormState = state.copy(
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                roleError = roleError,
                phoneError = phoneError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                submitError = if (hasError) "Check the highlighted account details." else null,
                currentStep = if (hasError) 0 else 1,
            )
        }

        fun submitEmployeeForm() {
            continueEmployeeForm()
            if (employeeFormState.currentStep == 0) return

            val email = employeeFormState.email.trim()
            val emailLooksValid = email.isBlank() || (email.contains("@") && email.contains("."))
            if (!emailLooksValid) {
                employeeFormState = employeeFormState.copy(
                    emailError = "Enter a valid email address",
                    submitError = "Check the highlighted contact detail.",
                )
                return
            }

            val selectedRole = employeeRoles.firstOrNull { it.name == employeeFormState.role }
            if (selectedRole == null) {
                employeeFormState = employeeFormState.copy(currentStep = 0, roleError = "Choose a valid role")
                return
            }

            val shopId = selectedShopId ?: shops.firstOrNull()?.id
            if (shopId == null) {
                employeeFormState = employeeFormState.copy(submitError = "No shop selected. Please select a shop first.")
                return
            }

            employeeFormState = employeeFormState.copy(isLoading = true)
            scope.launch {
                val request = EmployeeRequest(
                    firstName = employeeFormState.firstName.trim(),
                    lastName = employeeFormState.lastName.trim(),
                    password = employeeFormState.password,
                    phone = countryDialCode(employeeFormState.countryCode) + employeeFormState.phone,
                    title = employeeFormState.title,
                    role = selectedRole.id,
                    shopId = shopId,
                    kinName = employeeFormState.kinName.trim().ifEmpty { null },
                    kinPhone = employeeFormState.kinPhone.trim().ifEmpty { null },
                    residence = employeeFormState.residence.trim().ifEmpty { null },
                    email = email.ifEmpty { null },
                )
                when (val result = employeeRepository.createEmployee(request)) {
                    is ApiResult.Success -> {
                        employeeFormState = EmployeeFormUiState()
                        employeeRoles = emptyList()
                        destination = Destination.Employees
                    }
                    is ApiResult.Error -> employeeFormState = employeeFormState.copy(
                        isLoading = false,
                        submitError = result.message,
                    )
                    is ApiResult.NetworkError -> employeeFormState = employeeFormState.copy(
                        isLoading = false,
                        submitError = result.message,
                    )
                }
            }
        }

        fun openSupplierEditor(supplier: SupplierDto?) {
            editingSupplier = supplier
            supplierEditorForm = SupplierFormUiState(
                name = supplier?.name.orEmpty(),
                contactPerson = supplier?.contactPerson.orEmpty(),
                phone = supplier?.phone.orEmpty(),
                email = supplier?.email.orEmpty(),
                address = supplier?.address.orEmpty(),
            )
        }

        fun saveSupplierForm() {
            val form = supplierEditorForm ?: return
            val nameError = "Supplier name is required".takeIf { form.name.trim().isEmpty() }
            val phoneError = "Phone number is required".takeIf { form.phone.trim().isEmpty() }
            val email = form.email.trim()
            val emailError = if (email.isNotBlank() && !(email.contains("@") && email.contains("."))) {
                "Enter a valid email address"
            } else {
                null
            }
            if (nameError != null || phoneError != null || emailError != null) {
                supplierEditorForm = form.copy(
                    nameError = nameError,
                    phoneError = phoneError,
                    emailError = emailError,
                    submitError = "Check the highlighted supplier details.",
                )
                return
            }

            val shopId = selectedShopId ?: shops.firstOrNull()?.id
            if (shopId == null) {
                supplierEditorForm = form.copy(submitError = "No shop selected")
                return
            }

            supplierEditorForm = form.copy(isLoading = true, submitError = null)
            val existing = editingSupplier
            scope.launch {
                val result = if (existing == null) {
                    supplierRepository.createSupplier(
                        shopId,
                        AddSupplierRequest(
                            name = form.name.trim(),
                            contactPerson = form.contactPerson.trim().ifEmpty { null },
                            email = email.ifEmpty { null },
                            phone = form.phone.trim(),
                            address = form.address.trim().ifEmpty { null },
                        ),
                    )
                } else {
                    supplierRepository.updateSupplier(
                        shopId,
                        existing.id,
                        UpdateSupplierRequest(
                            name = form.name.trim(),
                            contactPerson = form.contactPerson.trim().ifEmpty { null },
                            email = email.ifEmpty { null },
                            phone = form.phone.trim(),
                            address = form.address.trim().ifEmpty { null },
                        ),
                    )
                }
                when (result) {
                    is ApiResult.Success -> {
                        suppliersSource = if (existing == null) {
                            listOf(result.data) + suppliersSource
                        } else {
                            suppliersSource.map { if (it.id == existing.id) result.data else it }
                        }
                        suppliersState = suppliersState.copy(suppliers = suppliersSource.map { it.toSupplierItemUi() })
                        supplierEditorForm = null
                        editingSupplier = null
                    }
                    is ApiResult.Error -> supplierEditorForm = form.copy(isLoading = false, submitError = result.message)
                    is ApiResult.NetworkError -> supplierEditorForm = form.copy(isLoading = false, submitError = result.message)
                }
            }
        }

        // Mirrors confirmDeleteSupplier/deleteSupplier on Android: the
        // confirmation step itself (supplierPendingDelete + the AlertDialog
        // in App()) is what the earlier commonMain pass deliberately left
        // out rather than wiring a no-confirmation delete.
        fun deleteSupplierConfirmed() {
            val supplier = supplierPendingDelete ?: return
            val shopId = selectedShopId ?: shops.firstOrNull()?.id
            if (shopId == null) {
                supplierEditorForm = supplierEditorForm?.copy(submitError = "No shop selected")
                supplierPendingDelete = null
                return
            }
            supplierEditorForm = supplierEditorForm?.copy(isLoading = true, submitError = null)
            scope.launch {
                when (val result = supplierRepository.deleteSupplier(shopId, supplier.id)) {
                    is ApiResult.Success -> {
                        suppliersSource = suppliersSource.filterNot { it.id == supplier.id }
                        suppliersState = suppliersState.copy(suppliers = suppliersSource.map { it.toSupplierItemUi() })
                        supplierEditorForm = null
                        editingSupplier = null
                    }
                    is ApiResult.Error -> supplierEditorForm = supplierEditorForm?.copy(isLoading = false, submitError = result.message)
                    is ApiResult.NetworkError -> supplierEditorForm = supplierEditorForm?.copy(isLoading = false, submitError = result.message)
                }
                supplierPendingDelete = null
            }
        }

        // Mirrors ShopCreationActivity.validatePrimaryStep.
        fun validateShopFormPrimaryStep() {
            val state = shopFormState
            val nameError = when {
                state.name.trim().isEmpty() -> "Shop name is required"
                state.name.trim().length < 2 -> "Use at least 2 characters"
                else -> null
            }
            val addressError = "Address is required".takeIf { state.address.trim().isEmpty() }
            val typeError = "Choose a shop type".takeIf { shopFormTypes.none { it.name.equals(state.type, ignoreCase = true) } }
            val hasError = listOf(nameError, addressError, typeError).any { it != null }
            shopFormState = state.copy(
                nameError = nameError,
                addressError = addressError,
                typeError = typeError,
                currentStep = if (hasError) 0 else 1,
                submitError = if (hasError) "Check the highlighted shop details." else null,
            )
        }

        // Mirrors ShopCreationActivity.handleShopCreationSuccess: changing
        // selectedShopId re-triggers the dashboard's own LaunchedEffect,
        // which reloads shops/totals for the new shop — same effect as
        // Android's restart-into-MainActivity.
        fun handleShopCreationSuccess(shop: ShopDto) {
            sessionProvider.saveCurrentShopId(shop.id)
            shopFormState = ShopFormUiState()
            destination = Destination.MainTabs
            selectedTab = AppTab.Dashboard
            selectedShopId = shop.id
        }

        // Mirrors ShopCreationActivity.createShop/performCreateShop.
        fun submitShopForm() {
            validateShopFormPrimaryStep()
            if (shopFormState.currentStep == 0 || shopFormState.isLoading) return

            val accountId = sessionProvider.accountId()
            if (accountId.isNullOrBlank()) {
                shopFormState = shopFormState.copy(submitError = "Session expired. Please log in again.")
                return
            }
            val selectedType = shopFormTypes.firstOrNull { it.name.equals(shopFormState.type, ignoreCase = true) }
            if (selectedType == null) {
                shopFormState = shopFormState.copy(currentStep = 0, typeError = "Choose a valid shop type")
                return
            }

            shopFormState = shopFormState.copy(isLoading = true, submitError = null)
            scope.launch {
                val result = shopsRepository.createShop(
                    CreateShopRequest(
                        name = shopFormState.name.trim(),
                        address = shopFormState.address.trim(),
                        shopTypeId = selectedType.id,
                        registrationNumber = shopFormState.registrationNumber.trim().ifEmpty { null },
                        taxIdentificationNumber = shopFormState.tin.trim().ifEmpty { null },
                        description = shopFormState.description.trim().ifEmpty { " " },
                        accountId = accountId,
                    ),
                )
                when (result) {
                    is ApiResult.Success -> handleShopCreationSuccess(result.data)
                    is ApiResult.Error -> shopFormState = shopFormState.copy(isLoading = false, submitError = result.message)
                    is ApiResult.NetworkError -> shopFormState = shopFormState.copy(isLoading = false, submitError = result.message)
                }
            }
        }

        // Mirrors UserRolesViewModel exactly, fake delay and all — see its
        // own doc comment: roles are sample/placeholder data, Save Changes
        // never persisted anywhere on Android either (a real roles API
        // doesn't exist yet on either platform).
        fun userRolePermissionCategories(role: DemoUserRole): List<PermissionCategoryUi> {
            fun item(id: String, name: String, displayName: String) =
                PermissionItemUi(id = id, name = name, displayName = displayName, isChecked = role.permissions.contains(name))
            return listOf(
                PermissionCategoryUi(
                    "Customers",
                    listOf(
                        item("cust_create", "customers_create", "Customers Create"),
                        item("cust_delete", "customers_delete", "Customers Delete"),
                        item("cust_edit", "customers_edit", "Customers Edit"),
                        item("cust_view", "customers_view", "Customers View"),
                    ),
                ),
                PermissionCategoryUi(
                    "Financial Statement",
                    listOf(
                        item("fs_create", "financial_statement_create", "Financial Statement Create"),
                        item("fs_delete", "financial_statement_delete", "Financial Statement Delete"),
                        item("fs_edit", "financial_statement_edit", "Financial Statement Edit"),
                        item("fs_view", "financial_statement_view", "Financial Statement View"),
                    ),
                ),
                PermissionCategoryUi(
                    "Stock",
                    listOf(
                        item("stock_create", "stock_create", "Stock Create"),
                        item("stock_delete", "stock_delete", "Stock Delete"),
                        item("stock_edit", "stock_edit", "Stock Edit"),
                        item("stock_view", "stock_view", "Stock View"),
                    ),
                ),
                PermissionCategoryUi(
                    "Expense Categories",
                    listOf(
                        item("exp_cat_create", "expense_categories_create", "Expense Categories Create"),
                        item("exp_cat_delete", "expense_categories_delete", "Expense Categories Delete"),
                        item("exp_cat_edit", "expense_categories_edit", "Expense Categories Edit"),
                        item("exp_cat_view", "expense_categories_view", "Expense Categories View"),
                    ),
                ),
                PermissionCategoryUi(
                    "Procurement",
                    listOf(
                        item("proc_create", "procurement_create", "Procurement Create"),
                        item("proc_delete", "procurement_delete", "Procurement Delete"),
                        item("proc_edit", "procurement_edit", "Procurement Edit"),
                        item("proc_view", "procurement_view", "Procurement View"),
                    ),
                ),
                PermissionCategoryUi(
                    "Sales",
                    listOf(
                        item("sales_create", "sales_create", "Sales Create"),
                        item("sales_delete", "sales_delete", "Sales Delete"),
                        item("sales_edit", "sales_edit", "Sales Edit"),
                        item("sales_view", "sales_view", "Sales View"),
                    ),
                ),
                PermissionCategoryUi(
                    "Stock Categories",
                    listOf(
                        item("stock_cat_create", "stock_categories_create", "Stock Categories Create"),
                        item("stock_cat_delete", "stock_categories_delete", "Stock Categories Delete"),
                        item("stock_cat_edit", "stock_categories_edit", "Stock Categories Edit"),
                        item("stock_cat_view", "stock_categories_view", "Stock Categories View"),
                    ),
                ),
                PermissionCategoryUi(
                    "Suppliers",
                    listOf(
                        item("supp_create", "suppliers_create", "Suppliers Create"),
                        item("supp_delete", "suppliers_delete", "Suppliers Delete"),
                        item("supp_edit", "suppliers_edit", "Suppliers Edit"),
                        item("supp_view", "suppliers_view", "Suppliers View"),
                    ),
                ),
            )
        }

        fun loadUserRoles() {
            userRolesState = userRolesState.copy(
                isLoading = true,
                shopLabel = shops.firstOrNull { it.id == selectedShopId }?.name.orEmpty(),
            )
            userRolesSource = listOf(
                DemoUserRole(
                    id = "1",
                    name = "Manager",
                    description = "Manager with operational privileges",
                    permissions = listOf("customers_view", "sales_view", "stock_view"),
                ),
                DemoUserRole(
                    id = "2",
                    name = "Staff",
                    description = "Staff member with basic privileges",
                    permissions = listOf("sales_view", "stock_view"),
                ),
            )
            userRolesState = userRolesState.copy(
                isLoading = false,
                roles = userRolesSource.map { it.toUserRoleUi() },
                emptyMessage = if (userRolesSource.isEmpty()) "No roles found" else null,
            )
        }

        fun filterUserRolesEditSearch(query: String) {
            val edit = userRolesState.editState ?: return
            val filtered = if (query.isBlank()) {
                userRolesEditCategories
            } else {
                userRolesEditCategories.mapNotNull { category ->
                    val matches = category.permissions.filter {
                        it.displayName.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true)
                    }
                    if (matches.isNotEmpty()) category.copy(permissions = matches) else null
                }
            }
            userRolesState = userRolesState.copy(
                editState = edit.copy(
                    searchQuery = query,
                    categories = filtered,
                    selectedCount = userRolesEditCategories.sumOf { category -> category.permissions.count { it.isChecked } },
                    totalCount = userRolesEditCategories.sumOf { it.permissions.size },
                ),
            )
        }

        fun editUserRolePermissions(role: UserRoleUi) {
            val fullRole = userRolesSource.firstOrNull { it.id == role.id } ?: return
            userRolesEditCategories = userRolePermissionCategories(fullRole)
            userRolesState = userRolesState.copy(
                editState = EditPermissionsState(
                    role = role,
                    categories = userRolesEditCategories,
                    selectedCount = userRolesEditCategories.sumOf { category -> category.permissions.count { it.isChecked } },
                    totalCount = userRolesEditCategories.sumOf { it.permissions.size },
                ),
            )
        }

        fun toggleUserRolePermission(categoryName: String, permissionId: String) {
            userRolesEditCategories = userRolesEditCategories.map { category ->
                if (category.name != categoryName) {
                    category
                } else {
                    category.copy(
                        permissions = category.permissions.map { permission ->
                            if (permission.id == permissionId) permission.copy(isChecked = !permission.isChecked) else permission
                        },
                    )
                }
            }
            filterUserRolesEditSearch(userRolesState.editState?.searchQuery.orEmpty())
        }

        fun saveUserRoleChanges() {
            val edit = userRolesState.editState ?: return
            userRolesState = userRolesState.copy(editState = edit.copy(isSaving = true))
            scope.launch {
                delay(1000)
                val selectedPermissions = userRolesEditCategories.flatMap { it.permissions }.filter { it.isChecked }.map { it.name }
                userRolesSource = userRolesSource.map { role ->
                    if (role.id == edit.role.id) role.copy(permissions = selectedPermissions) else role
                }
                userRolesState = userRolesState.copy(roles = userRolesSource.map { it.toUserRoleUi() }, editState = null)
            }
        }

        // Mirrors ContactUsFragment.submitSupportMessage + ContactUsViewModel
        // .sendSupportMessage: same field/email validation, but no real
        // backend call on Android either (CreateSupportTicketRequest exists
        // but is never actually used) — just a fake success flip.
        fun submitSupportMessage(message: SharedSupportMessageUi) {
            if (message.name.isBlank() || message.email.isBlank() || message.subject.isBlank() ||
                message.category.isBlank() || message.message.isBlank()
            ) {
                return
            }
            if (!message.email.contains("@") || !message.email.substringAfter("@").contains(".")) return
            isContactUsSubmitting = true
            isContactUsSubmitting = false
            contactUsResetKey++
        }

        suspend fun refreshAiAssistant() {
            aiAssistantState = aiAssistantState.copy(isLoading = true, errorMessage = null)
            val readiness = aiAssistantRepository.getReadiness().getOrNull()
            if (readiness?.isReady == false) {
                aiAssistantState = aiAssistantState.copy(
                    isLoading = false,
                    errorMessage = readiness.message.ifBlank {
                        "Osebo AI is getting ready. Please try again shortly."
                    },
                )
                return
            }

            // Mirrors AiAssistantViewModel.refresh(): a readiness failure must not block
            // conversations, since readiness may be gated behind operational-role checks
            // that don't apply to an otherwise-authorized user.
            aiAssistantRepository.getConversations()
                .onSuccess { conversations ->
                    aiAssistantState = aiAssistantState.copy(
                        conversations = conversations.sortedByDescending(AiConversation::updatedAt),
                        isLoading = false,
                    )
                }
                .onFailure { error ->
                    aiAssistantState = aiAssistantState.copy(isLoading = false, errorMessage = error.aiUserMessage())
                }
        }

        LaunchedEffect(authState, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.AiAssistant) return@LaunchedEffect
            refreshAiAssistant()
        }

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.SubscriptionPackages) return@LaunchedEffect

            val currentShop = shops.find { it.id == (selectedShopId ?: shops.firstOrNull()?.id) }
            subscriptionPackagesState = subscriptionPackagesState.copy(
                isLoading = true,
                errorMessage = null,
                shopName = currentShop?.name ?: "Your shop",
                currentPlan = currentShop?.takeIf { it.hasActiveSubscription }?.subscriptionTypeString,
            )
            val shopId = currentShop?.id
            if (shopId == null) {
                subscriptionPackagesState = subscriptionPackagesState.copy(
                    isLoading = false,
                    errorMessage = "No shop selected",
                )
                return@LaunchedEffect
            }
            when (val result = subscriptionRepository.getPackages(shopId)) {
                is ApiResult.Success -> {
                    subscriptionPackagesSource = result.data
                    subscriptionPackagesState = subscriptionPackagesState.copy(
                        isLoading = false,
                        bundles = result.data.map { it.toBundleUi() },
                    )
                }
                is ApiResult.Error -> subscriptionPackagesState = subscriptionPackagesState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
                is ApiResult.NetworkError -> subscriptionPackagesState = subscriptionPackagesState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }
        }

        fun updateSubscriptionSelection(ids: Set<String>) {
            val selectedBundles = subscriptionPackagesState.bundles.filter { it.id in ids }
            val currentShop = shops.find { it.id == (selectedShopId ?: shops.firstOrNull()?.id) }
            val hasActiveSub = currentShop?.hasActiveSubscription == true
            val isTrialEligible = ids.isNotEmpty() && !hasActiveSub && selectedBundles.any { it.hasFreeTrial }
            val totalLabel = when {
                ids.isEmpty() -> "UGX 0"
                selectedBundles.any { it.isCustomPricing } -> "Contact sales"
                isTrialEligible -> "UGX 0"
                else -> formatSubscriptionAmount(subscriptionPackagesSource.filter { it.id in ids }.sumOf { it.monthlyAmount })
            }
            subscriptionPackagesState = subscriptionPackagesState.copy(
                selectedIds = ids,
                totalLabel = totalLabel,
                isTrialEligible = isTrialEligible,
            )
        }

        fun refreshSubscriptionPackages() {
            val shopId = selectedShopId ?: shops.firstOrNull()?.id
            if (shopId == null) {
                subscriptionPackagesState = subscriptionPackagesState.copy(errorMessage = "No shop selected")
                return
            }
            scope.launch {
                subscriptionPackagesState = subscriptionPackagesState.copy(isLoading = true, errorMessage = null)
                when (val result = subscriptionRepository.getPackages(shopId)) {
                    is ApiResult.Success -> {
                        subscriptionPackagesSource = result.data
                        subscriptionPackagesState = subscriptionPackagesState.copy(
                            isLoading = false,
                            bundles = result.data.map { it.toBundleUi() },
                        )
                    }
                    is ApiResult.Error -> subscriptionPackagesState = subscriptionPackagesState.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                    is ApiResult.NetworkError -> subscriptionPackagesState = subscriptionPackagesState.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
            }
        }

        fun openSubscriptionCheckout() {
            val selected = subscriptionPackagesState.selectedIds
            if (selected.isEmpty()) return
            val selectedBundles = subscriptionPackagesState.bundles.filter { it.id in selected }
            val shopId = selectedShopId ?: shops.firstOrNull()?.id
            val shopName = shops.find { it.id == shopId }?.name ?: "Your shop"
            val totalMonthly = subscriptionPackagesSource.filter { it.id in selected }.sumOf { it.monthlyAmount }
            subscriptionCheckoutPackageIds = selected.toList()
            subscriptionCheckoutIsTrial = subscriptionPackagesState.isTrialEligible
            subscriptionPaymentState = SubscriptionPaymentUiState(
                shopName = shopName,
                packageName = selectedBundles.joinToString(" + ") { it.name },
                monthlyAmount = totalMonthly,
                months = 1,
            )
            destination = Destination.SubscriptionPayment
        }

        suspend fun finishSubscriptionCheckout() {
            when (val shopsResult = api.getShops()) {
                is ApiResult.Success -> shops = shopsResult.data.data.orEmpty()
                else -> Unit
            }
            subscriptionPaymentState = SubscriptionPaymentUiState()
            destination = Destination.SubscriptionPackages
        }

        suspend fun pollSubscriptionPayment(paymentId: String) {
            repeat(60) {
                when (val result = subscriptionRepository.pollPaymentStatus(paymentId)) {
                    is ApiResult.Success -> {
                        val status = result.data
                        when {
                            status.isActive -> {
                                finishSubscriptionCheckout()
                                return
                            }
                            status.isFailed -> {
                                subscriptionPaymentState = subscriptionPaymentState.copy(
                                    isLoading = false,
                                    submitError = "Payment failed. Please try again.",
                                )
                                return
                            }
                            status.isCancelled -> {
                                subscriptionPaymentState = subscriptionPaymentState.copy(
                                    isLoading = false,
                                    submitError = "Payment was cancelled.",
                                )
                                return
                            }
                            else -> Unit
                        }
                    }
                    is ApiResult.Error, is ApiResult.NetworkError -> Unit
                }
                delay(5000)
            }
            subscriptionPaymentState = subscriptionPaymentState.copy(
                isLoading = false,
                submitError = "Still waiting for payment confirmation. Check your phone and try again if needed.",
            )
        }

        fun submitSubscriptionPayment() {
            val formState = subscriptionPaymentState
            if (formState.monthlyAmount <= 0) {
                subscriptionPaymentState = formState.copy(
                    submitError = "This plan needs a custom quote — contact sales@osebo.ai.",
                )
                return
            }
            val digits = formState.phone.filter { it.isDigit() }
            val phoneError = validateSubscriptionPhone(digits)
            if (phoneError != null) {
                subscriptionPaymentState = formState.copy(phoneError = phoneError)
                return
            }
            val shopId = selectedShopId ?: shops.firstOrNull()?.id
            if (shopId == null) {
                subscriptionPaymentState = formState.copy(submitError = "No shop selected")
                return
            }
            val formattedPhone = formatSubscriptionPhone(digits)
            subscriptionPaymentState = formState.copy(isLoading = true, submitError = null, phoneError = null)
            scope.launch {
                when (
                    val result = subscriptionRepository.createSubscription(
                        shopId = shopId,
                        packageIds = subscriptionCheckoutPackageIds,
                        phoneNumber = formattedPhone,
                        months = formState.months,
                        isTrial = subscriptionCheckoutIsTrial,
                    )
                ) {
                    is ApiResult.Success -> {
                        val paymentId = result.data.paymentId
                        if (subscriptionCheckoutIsTrial || paymentId.isNullOrBlank()) {
                            finishSubscriptionCheckout()
                        } else {
                            pollSubscriptionPayment(paymentId)
                        }
                    }
                    is ApiResult.Error -> subscriptionPaymentState = subscriptionPaymentState.copy(
                        isLoading = false,
                        submitError = result.message,
                    )
                    is ApiResult.NetworkError -> subscriptionPaymentState = subscriptionPaymentState.copy(
                        isLoading = false,
                        submitError = result.message,
                    )
                }
            }
        }

        fun transactionCategories(): List<String> =
            listOf("All") + transactionsSource.map { Transaction.getCategoryDisplayName(it.category) }.distinct()

        fun applyTransactionFilters() {
            val minAmount = transactionFilterState.minAmount.toDoubleOrNull()
            val maxAmount = transactionFilterState.maxAmount.toDoubleOrNull()
            val category = transactionFilterState.category
            val filtered = transactionsSource.filter { transaction ->
                val matchesCategory = category == "All" ||
                    Transaction.getCategoryDisplayName(transaction.category) == category
                val matchesMin = minAmount == null || transaction.amount >= minAmount
                val matchesMax = maxAmount == null || transaction.amount <= maxAmount
                matchesCategory && matchesMin && matchesMax
            }
            transactionsState = filtered.toTransactionsUiState()
            isTransactionFilterOpen = false
        }

        fun clearTransactionFilters() {
            transactionFilterState = TransactionFilterUiState()
            transactionsState = transactionsSource.toTransactionsUiState()
        }

        fun performLogout() {
            isMoreMenuOpen = false
            sessionProvider.clear()
            sessionProvider.onSessionExpiredListener?.invoke()
        }

        // Mirrors AccountViewModel.loadAccountData, minus Android's local
        // SQLite/SharedPreferences cache fallback (no equivalent cache
        // exists in commonMain yet) — a load failure just leaves profile
        // null, which SharedAccountScreen already renders as an
        // "unavailable, try again" card.
        fun loadAccount() {
            accountState = accountState.copy(isLoading = true)
            scope.launch {
                when (val result = accountRepository.getAccount()) {
                    is ApiResult.Success -> accountState = accountState.copy(isLoading = false, profile = result.data)
                    is ApiResult.Error -> accountState = accountState.copy(isLoading = false)
                    is ApiResult.NetworkError -> accountState = accountState.copy(isLoading = false)
                }
            }
        }

        fun toggleTwoFactorAuth(enabled: Boolean) {
            accountState = accountState.copy(isLoading = true)
            scope.launch {
                when (val result = accountRepository.updateTwoFactorAuth(enabled)) {
                    is ApiResult.Success -> accountState = accountState.copy(isLoading = false, profile = result.data)
                    is ApiResult.Error -> accountState = accountState.copy(isLoading = false)
                    is ApiResult.NetworkError -> accountState = accountState.copy(isLoading = false)
                }
            }
        }

        fun toggleLoginNotifications(enabled: Boolean) {
            accountState = accountState.copy(isLoading = true)
            scope.launch {
                when (val result = accountRepository.updateLoginNotifications(enabled)) {
                    is ApiResult.Success -> accountState = accountState.copy(isLoading = false, profile = result.data)
                    is ApiResult.Error -> accountState = accountState.copy(isLoading = false)
                    is ApiResult.NetworkError -> accountState = accountState.copy(isLoading = false)
                }
            }
        }

        // Mirrors confirmAccountAction, but logs out on success instead of
        // Android's own behavior (a generic "updated successfully" toast
        // with no navigation away) — staying signed in against a deleted
        // or deactivated account would just produce broken API calls from
        // here on, so this closes the now-invalid session instead.
        fun confirmAccountAction() {
            val action = accountConfirmation ?: return
            accountConfirmation = null
            accountState = accountState.copy(isLoading = true)
            scope.launch {
                val result = when (action) {
                    SharedAccountConfirmation.DEACTIVATE -> accountRepository.deactivateAccount()
                    SharedAccountConfirmation.DELETE -> accountRepository.deleteAccount()
                }
                when (result) {
                    is ApiResult.Success -> performLogout()
                    is ApiResult.Error -> accountState = accountState.copy(isLoading = false)
                    is ApiResult.NetworkError -> accountState = accountState.copy(isLoading = false)
                }
            }
        }

        // Mirrors AccountFragment's edit-account/payment-method dialog
        // result listeners exactly: local-only, no real persistence -
        // neither EditAccountDialogFragment nor PaymentMethodDialogFragment
        // call a backend on Android either.
        fun saveEditedAccount(details: SharedEditableAccountDetails) {
            accountState = accountState.copy(
                profile = accountState.profile?.copy(
                    businessName = details.businessName,
                    businessType = details.businessType,
                    registrationNumber = details.registrationNumber,
                    taxId = details.taxId,
                    address = details.address,
                ),
            )
            isEditAccountOpen = false
        }

        fun savePaymentMethod(method: String) {
            accountState = accountState.copy(profile = accountState.profile?.copy(paymentMethod = method))
            isPaymentMethodOpen = false
        }

        fun startAiConversation() {
            aiAssistantState = aiAssistantState.copy(
                activeConversationId = null,
                messages = emptyList(),
                errorMessage = null,
            )
        }

        fun openAiConversation(conversationId: String) {
            if (aiAssistantState.isLoading) return
            scope.launch {
                aiAssistantState = aiAssistantState.copy(isLoading = true, errorMessage = null)
                aiAssistantRepository.getConversation(conversationId)
                    .onSuccess { conversation ->
                        aiAssistantState = aiAssistantState.copy(
                            activeConversationId = conversation.id,
                            messages = conversation.messages,
                            isLoading = false,
                        )
                    }
                    .onFailure { error ->
                        aiAssistantState = aiAssistantState.copy(isLoading = false, errorMessage = error.aiUserMessage())
                    }
            }
        }

        fun archiveAiConversation(conversationId: String) {
            scope.launch {
                aiAssistantRepository.archiveConversation(conversationId)
                    .onSuccess {
                        aiAssistantState = aiAssistantState.copy(
                            conversations = aiAssistantState.conversations.filterNot { it.id == conversationId },
                            activeConversationId = aiAssistantState.activeConversationId
                                .takeUnless { it == conversationId },
                            messages = if (aiAssistantState.activeConversationId == conversationId) {
                                emptyList()
                            } else {
                                aiAssistantState.messages
                            },
                        )
                    }
                    .onFailure { error ->
                        aiAssistantState = aiAssistantState.copy(errorMessage = error.aiUserMessage())
                    }
            }
        }

        fun retryAiAssistant() {
            scope.launch { refreshAiAssistant() }
        }

        fun sendAiMessage(rawMessage: String) {
            val message = rawMessage.trim()
            if (message.isBlank() || aiAssistantState.isSending) return

            scope.launch {
                val optimisticMessage = AiChatMessage(
                    id = "local-${Clock.System.now().toEpochMilliseconds()}",
                    isFromUser = true,
                    text = message,
                )
                aiAssistantState = aiAssistantState.copy(
                    messages = aiAssistantState.messages + optimisticMessage,
                    isSending = true,
                    errorMessage = null,
                )

                val conversationId = aiAssistantState.activeConversationId ?: run {
                    val title = message.replace("\n", " ").take(48)
                    aiAssistantRepository.createConversation(title)
                        .onSuccess { conversation ->
                            aiAssistantState = aiAssistantState.copy(
                                activeConversationId = conversation.id,
                                conversations = listOf(conversation) + aiAssistantState.conversations,
                            )
                        }
                        .map { it.id }
                        .getOrElse { error ->
                            aiAssistantState = aiAssistantState.copy(
                                messages = aiAssistantState.messages.filterNot { it.id == optimisticMessage.id },
                                isSending = false,
                                errorMessage = error.aiUserMessage(),
                            )
                            return@launch
                        }
                }

                val sendResult = aiAssistantRepository.sendMessage(conversationId, message)
                val sendError = sendResult.exceptionOrNull()
                if (sendError != null) {
                    aiAssistantState = aiAssistantState.copy(
                        messages = aiAssistantState.messages.filterNot { it.id == optimisticMessage.id },
                        isSending = false,
                        errorMessage = sendError.aiUserMessage(),
                    )
                    return@launch
                }

                val canonicalConversation = aiAssistantRepository.getConversation(conversationId).getOrNull()
                val responseMessages = canonicalConversation?.messages
                    ?.takeIf(List<AiChatMessage>::isNotEmpty)
                    ?: run {
                        val received = sendResult.getOrDefault(emptyList())
                        if (received.isEmpty()) {
                            aiAssistantState.messages
                        } else {
                            (aiAssistantState.messages + received).distinctBy { chatMessage ->
                                chatMessage.id.takeUnless { it.startsWith("local-") }
                                    ?: "${chatMessage.isFromUser}:${chatMessage.text}"
                            }
                        }
                    }
                aiAssistantState = aiAssistantState.copy(
                    messages = responseMessages,
                    isSending = false,
                    conversations = canonicalConversation?.let { updated ->
                        (aiAssistantState.conversations.filterNot { it.id == updated.id } + updated)
                            .sortedByDescending(AiConversation::updatedAt)
                    } ?: aiAssistantState.conversations,
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Bound once here because authState is a `by remember` delegate —
            // Kotlin can't smart-cast a delegated var's payload directly off
            // the `is` check below, only off a plain local val like this one.
            when (val currentAuthState = authState) {
                AuthState.Splash -> SplashScreen(
                    onFinished = {
                        val token = sessionProvider.authToken()
                        authState = if (!token.isNullOrBlank()) {
                            AuthState.LoggedIn(token)
                        } else {
                            AuthState.LoggedOut
                        }
                    },
                )

                AuthState.LoggedOut -> WelcomeScreen(
                    onLogin = { authState = AuthState.EnteringCredentials },
                    onRegister = {
                        signUpState = SignUpUiState()
                        signUpUserId = null
                        signUpPhone = ""
                        authState = AuthState.SigningUp
                    },
                )

                AuthState.EnteringCredentials -> LoginScreen(
                    state = loginState,
                    onUsernameChange = { loginState = loginState.copy(username = it, errorMessage = null) },
                    onPasswordChange = { loginState = loginState.copy(password = it, errorMessage = null) },
                    onSignInClick = ::attemptSignIn,
                    onBackClick = {
                        loginState = LoginUiState()
                        authState = AuthState.LoggedOut
                    },
                )

                AuthState.SigningUp -> SignUpScreen(
                    state = signUpState,
                    onStateChange = { signUpState = it },
                    onPrimaryAction = { if (signUpState.isOtpMode) verifyAndSignUp() else requestSignUpOtp() },
                    onResendOtp = ::requestSignUpOtp,
                    onSignInClick = {
                        signUpState = SignUpUiState()
                        signUpUserId = null
                        signUpPhone = ""
                        authState = AuthState.EnteringCredentials
                    },
                )

                is AuthState.SelectingAccount -> {
                    val preAuthToken = currentAuthState.preAuthToken
                    SelectAccountScreen(
                        state = selectAccountState,
                        onAccountSelected = { accountId ->
                            selectAccountState = selectAccountState.copy(
                                selectedAccountId = accountId,
                                errorMessage = null,
                            )
                        },
                        onContinueClick = {
                            val accountId = selectAccountState.selectedAccountId
                            if (accountId != null) attemptSelectAccount(preAuthToken, accountId)
                        },
                        onBackToSignInClick = {
                            selectAccountState = SelectAccountUiState()
                            authState = AuthState.EnteringCredentials
                        },
                    )
                }

                is AuthState.LoggedIn -> when (destination) {
                    Destination.MainTabs -> {
                        when (selectedTab) {
                            AppTab.Dashboard -> DashboardScreen(
                                state = dashboardState,
                                onShopPickerClick = { isShopPickerOpen = true },
                                onReportsClick = { destination = Destination.Reports },
                                onAddProductClick = { destination = Destination.AddProduct },
                                onNewSaleClick = { destination = Destination.NewSale },
                                onAddEmployeeClick = { destination = Destination.AddEmployee },
                            )
                            AppTab.Sales -> SalesScreen(
                                state = salesState,
                                onNewSaleClick = { destination = Destination.NewSale },
                                onReportsClick = { destination = Destination.Reports },
                                onRefreshClick = {},
                                onSaleClick = {},
                            )
                            AppTab.Inventory -> InventoryScreen(
                                state = inventoryState,
                                onQueryChange = { inventoryState = inventoryState.copy(query = it) },
                                onAddProductClick = { destination = Destination.AddProduct },
                                onStockTransfersClick = {},
                                onAddStockClick = { destination = Destination.AddStock },
                                onRemoveStockClick = { destination = Destination.RemoveStock },
                                onProductClick = {},
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                        ) {
                            BottomNavBar(
                                items = listOf(
                                    NavItem(painterResource(Res.drawable.iconsax_nav_home), AppTab.Dashboard.label),
                                    NavItem(painterResource(Res.drawable.iconsax_nav_inventory), AppTab.Inventory.label),
                                    NavItem(painterResource(Res.drawable.iconsax_nav_sales), AppTab.Sales.label),
                                    NavItem(painterResource(Res.drawable.iconsax_more), "More"),
                                ),
                                selectedIndex = selectedTab.ordinal,
                                onItemSelected = { index ->
                                    if (index == AppTab.entries.size) {
                                        isMoreMenuOpen = true
                                    } else {
                                        selectedTab = AppTab.entries[index]
                                    }
                                },
                            )
                            MoreMenu(
                                expanded = isMoreMenuOpen,
                                onDismiss = { isMoreMenuOpen = false },
                                // Mirrors MainActivity.refreshMoreMenuSections' visibility
                                // rules exactly, Suppliers-gated-by-VIEW_INVENTORY included
                                // (that's what Android's own code checks, not VIEW_SUPPLIERS).
                                sections = listOfNotNull(
                                    SharedMoreMenuSection(
                                        "Shop",
                                        listOfNotNull(
                                            MoreMenuEntry("Shops") {
                                                isMoreMenuOpen = false
                                                destination = Destination.Shops
                                            }.takeIf { sessionProvider.isShopOwner() },
                                            MoreMenuEntry("Shop Home") {
                                                isMoreMenuOpen = false
                                                destination = if (selectedShopId != null) {
                                                    Destination.ShopDashboard
                                                } else {
                                                    Destination.Shops
                                                }
                                            }.takeIf { shops.isNotEmpty() },
                                        ),
                                    ).takeIf { it.entries.isNotEmpty() },
                                    SharedMoreMenuSection(
                                        "Business",
                                        listOfNotNull(
                                            MoreMenuEntry("Finance") {
                                                isMoreMenuOpen = false
                                                destination = Destination.Finance
                                            }.takeIf { shops.isNotEmpty() && sessionProvider.hasPermission(PermissionType.VIEW_FINANCE) },
                                        ),
                                    ).takeIf { it.entries.isNotEmpty() },
                                    SharedMoreMenuSection(
                                        "People",
                                        listOfNotNull(
                                            MoreMenuEntry("Employees") {
                                                isMoreMenuOpen = false
                                                destination = Destination.Employees
                                            }.takeIf { shops.isNotEmpty() && sessionProvider.hasPermission(PermissionType.VIEW_EMPLOYEES) },
                                            MoreMenuEntry("Suppliers") {
                                                isMoreMenuOpen = false
                                                destination = Destination.Suppliers
                                            }.takeIf { shops.isNotEmpty() && sessionProvider.hasPermission(PermissionType.VIEW_INVENTORY) },
                                            MoreMenuEntry("Customers") {
                                                isMoreMenuOpen = false
                                                destination = Destination.Customers
                                            }.takeIf { shops.isNotEmpty() && sessionProvider.hasPermission(PermissionType.VIEW_CUSTOMERS) },
                                        ),
                                    ).takeIf { it.entries.isNotEmpty() },
                                    SharedMoreMenuSection(
                                        "Account & Support",
                                        listOfNotNull(
                                            MoreMenuEntry("Subscription") {
                                                isMoreMenuOpen = false
                                                destination = Destination.SubscriptionPackages
                                            }.takeIf { shops.isNotEmpty() && sessionProvider.isShopOwner() },
                                            MoreMenuEntry("User Roles") {
                                                isMoreMenuOpen = false
                                                loadUserRoles()
                                                destination = Destination.UserRoles
                                            }.takeIf { shops.isNotEmpty() && sessionProvider.isShopOwner() },
                                            MoreMenuEntry("Account") {
                                                isMoreMenuOpen = false
                                                loadAccount()
                                                destination = Destination.Account
                                            }.takeIf {
                                                shops.isNotEmpty() &&
                                                    (sessionProvider.isShopOwner() || sessionProvider.hasPermission(PermissionType.VIEW_EMPLOYEES))
                                            },
                                            MoreMenuEntry("Contact Us") {
                                                isMoreMenuOpen = false
                                                destination = Destination.ContactUs
                                            },
                                            MoreMenuEntry("Logout", ::performLogout),
                                        ),
                                    ),
                                ),
                            )
                        }

                        // Android exposes Osebo AI as its own dedicated circular button
                        // next to UserBottomNavigation, not through the More menu — see
                        // UserBottomNavigation.kt's onOseboAiClick. Floating it above the
                        // bar here keeps BottomNavBar's own fillMaxWidth layout untouched.
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 28.dp, bottom = 92.dp)
                                .size(52.dp)
                                .background(Color(0xFF0783CF), CircleShape)
                                .clickable { destination = Destination.AiAssistant },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.oseboicon),
                                contentDescription = "Open Osebo AI",
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }

                    Destination.Finance -> FinanceScreen(
                        state = financeState,
                        periods = listOf(
                            "Today", "This Week", "This Month",
                            "Last Month", "This Quarter", "This Year",
                        ),
                        onBackClick = { destination = Destination.MainTabs },
                        onPeriodSelected = { financeState = financeState.copy(selectedPeriod = it) },
                        onRefreshClick = {},
                        onAddExpenseClick = { destination = Destination.AddExpense },
                        onNewSaleClick = { destination = Destination.NewSale },
                        onReportsClick = { destination = Destination.Reports },
                        onCategoriesClick = { destination = Destination.ExpenseCategories },
                        onStatementClick = { destination = Destination.FinancialStatement },
                        onExportClick = {},
                        onSettingsClick = { destination = Destination.FinanceSettings },
                        onViewAllClick = { destination = Destination.AllTransactions },
                        onTransactionClick = {},
                        onTransactionAction = { _, _ -> },
                    )

                    Destination.Customers -> CustomersScreen(
                        state = customersState,
                        onBackClick = { destination = Destination.MainTabs },
                    )

                    Destination.Reports -> ReportsScreen(
                        state = reportsState,
                        periods = listOf("Daily", "Weekly", "Monthly", "Quarterly", "Yearly"),
                        onBackClick = { destination = Destination.MainTabs },
                        onPeriodSelected = { reportsState = reportsState.copy(selectedPeriod = it) },
                    )

                    Destination.AddStock -> AddStockScreen(
                        state = addStockState,
                        mode = StockAdjustmentMode.Add,
                        onBackClick = { destination = Destination.MainTabs },
                        onQueryChange = { addStockState = addStockState.copy(query = it) },
                        onQuantityChange = { id, raw -> updateStockQuantity(StockAdjustmentMode.Add, id, raw) },
                        onDecreaseQuantity = { adjustStockQuantity(StockAdjustmentMode.Add, it, increase = false) },
                        onIncreaseQuantity = { adjustStockQuantity(StockAdjustmentMode.Add, it, increase = true) },
                        onAddStockClick = { performStockAdjustment(StockAdjustmentMode.Add) },
                    )

                    Destination.RemoveStock -> AddStockScreen(
                        state = addStockState,
                        mode = StockAdjustmentMode.Remove,
                        onBackClick = { destination = Destination.MainTabs },
                        onQueryChange = { addStockState = addStockState.copy(query = it) },
                        onQuantityChange = { id, raw -> updateStockQuantity(StockAdjustmentMode.Remove, id, raw) },
                        onDecreaseQuantity = { adjustStockQuantity(StockAdjustmentMode.Remove, it, increase = false) },
                        onIncreaseQuantity = { adjustStockQuantity(StockAdjustmentMode.Remove, it, increase = true) },
                        onAddStockClick = { performStockAdjustment(StockAdjustmentMode.Remove) },
                    )

                    Destination.AddProduct -> AddProductScreen(
                        state = addProductState,
                        categories = productCategories.map { it.name },
                        units = listOf("Piece", "kg", "g", "Litre", "ml", "Meter", "Box", "Pack"),
                        onStateChange = { addProductState = it },
                        onBackClick = { destination = Destination.MainTabs },
                        onUploadImageClick = {},
                        onSaveClick = ::saveAddProductForm,
                    )

                    Destination.AiAssistant -> OseboAiScreen(
                        firstName = sessionProvider.userFirstName() ?: "there",
                        uiState = aiAssistantState,
                        onBackClick = { destination = Destination.MainTabs },
                        onOpenConversation = ::openAiConversation,
                        onArchiveConversation = ::archiveAiConversation,
                        onStartNewConversation = ::startAiConversation,
                        onRetry = ::retryAiAssistant,
                        onSubmit = ::sendAiMessage,
                    )

                    Destination.SubscriptionPackages -> SubscriptionPackagesScreen(
                        state = subscriptionPackagesState,
                        onBackClick = { destination = Destination.Shops },
                        onRefreshClick = ::refreshSubscriptionPackages,
                        onSelectionChange = ::updateSubscriptionSelection,
                        onCancelClick = { updateSubscriptionSelection(emptySet()) },
                        onCheckoutClick = ::openSubscriptionCheckout,
                    )

                    Destination.SubscriptionPayment -> SubscriptionPaymentScreen(
                        state = subscriptionPaymentState,
                        isSheet = false,
                        onStateChange = { subscriptionPaymentState = it },
                        onDismiss = { destination = Destination.SubscriptionPackages },
                        onSubmit = ::submitSubscriptionPayment,
                    )

                    Destination.NewSale -> NewSaleScreen(
                        state = newSaleState,
                        onBackClick = { destination = Destination.MainTabs },
                        onSearchChange = ::searchNewSaleProducts,
                        onScanClick = { isBarcodeScannerActive = true },
                        onCustomerSelected = ::selectNewSaleCustomer,
                        onAddProduct = ::addProductToNewSale,
                        onQuantityChanged = ::changeNewSaleQuantity,
                        onRemoveItem = ::removeNewSaleItem,
                        onDiscountApplied = ::applyNewSaleDiscount,
                        onClearCart = ::clearNewSaleCart,
                        onHoldSale = ::holdNewSale,
                        onCheckout = ::proceedToPaymentScreen,
                        onSuccessMessageShown = { newSaleState = newSaleState.copy(successMessage = null) },
                    )

                    Destination.Payment -> PaymentScreen(
                        state = paymentState,
                        onBackClick = { destination = Destination.NewSale },
                        onAmountChange = ::updatePaymentAmount,
                        onMethodSelected = { paymentState = paymentState.copy(selectedMethod = it) },
                        onPhoneChange = { paymentState = paymentState.copy(phoneNumber = it) },
                        onReferenceChange = { paymentState = paymentState.copy(reference = it) },
                        onNotesChange = { paymentState = paymentState.copy(notes = it) },
                        onCompleteClick = ::processPaymentSubmit,
                        onConfirmOverpayment = {
                            paymentState = paymentState.copy(showOverpaymentConfirmation = false)
                            proceedWithPaymentSubmit()
                        },
                        onDismissOverpayment = {
                            paymentState = paymentState.copy(showOverpaymentConfirmation = false)
                        },
                    )

                    Destination.Receipt -> ReceiptScreen(
                        state = receiptState,
                        onBackClick = { destination = Destination.MainTabs; selectedTab = AppTab.Sales },
                        onPrintClick = {},
                        onShareClick = {},
                        onNewSaleClick = { destination = Destination.NewSale },
                    )

                    Destination.AddEmployee -> EmployeeFormScreen(
                        state = employeeFormState,
                        titles = listOf("Mr", "Mrs", "Ms", "Dr", "Prof", "Rev"),
                        roles = employeeRoles.map { it.name },
                        countryCodes = listOf("UG (+256)", "KE (+254)", "TZ (+255)", "RW (+250)", "SS (+211)"),
                        onStateChange = { employeeFormState = it },
                        onContinue = ::continueEmployeeForm,
                        onSubmit = ::submitEmployeeForm,
                        onDismiss = { destination = Destination.MainTabs },
                    )

                    Destination.Employees -> EmployeesScreen(
                        state = employeesState,
                        onRefreshClick = {},
                        onAddEmployeeClick = { destination = Destination.AddEmployee },
                        onAttendanceClick = {},
                        onScheduleClick = {},
                        onPayrollClick = {},
                        onPerformanceClick = {},
                        onViewAllClick = {},
                        onEmployeeClick = {},
                        onImportClick = {},
                        onExportClick = {},
                        onRolesClick = {},
                    )

                    Destination.Suppliers -> {
                        SuppliersScreen(
                            state = suppliersState,
                            onRefreshClick = {},
                            onAddSupplierClick = { openSupplierEditor(null) },
                            onSupplierClick = { id ->
                                suppliersSource.firstOrNull { it.id == id }?.let { openSupplierEditor(it) }
                            },
                            onSupplierOptionsClick = { id ->
                                suppliersSource.firstOrNull { it.id == id }?.let { openSupplierEditor(it) }
                            },
                        )
                        supplierEditorForm?.let { form ->
                            SupplierFormDialog(
                                state = form,
                                isEditMode = editingSupplier != null,
                                onStateChange = { supplierEditorForm = it },
                                onDismiss = { if (!form.isLoading) supplierEditorForm = null },
                                onSubmit = ::saveSupplierForm,
                                onDelete = { editingSupplier?.let { supplierPendingDelete = it } },
                            )
                        }
                        supplierPendingDelete?.let { supplier ->
                            AlertDialog(
                                onDismissRequest = { supplierPendingDelete = null },
                                title = { Text("Delete supplier") },
                                text = { Text("Are you sure you want to delete ${supplier.name}?") },
                                confirmButton = {
                                    TextButton(onClick = ::deleteSupplierConfirmed) { Text("Delete") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { supplierPendingDelete = null }) { Text("Cancel") }
                                },
                            )
                        }
                    }

                    Destination.Shops -> ShopsScreen(
                        state = shopsListState,
                        onAddShopClick = { destination = Destination.ShopForm },
                        onRefreshClick = {},
                        onShopClick = { id ->
                            selectedShopId = id
                            sessionProvider.saveCurrentShopId(id)
                            destination = Destination.ShopDashboard
                        },
                        onSubscribeClick = { id ->
                            selectedShopId = id
                            sessionProvider.saveCurrentShopId(id)
                            destination = Destination.SubscriptionPackages
                        },
                    )

                    Destination.ShopForm -> ShopFormScreen(
                        state = shopFormState,
                        shopTypes = shopFormTypes.map { it.name },
                        onStateChange = { shopFormState = it },
                        onBackClick = { destination = Destination.Shops },
                        onContinue = ::validateShopFormPrimaryStep,
                        onSave = ::submitShopForm,
                    )

                    Destination.ShopDashboard -> ShopDashboardScreen(
                        state = shopDashboardState,
                        onBackClick = { destination = Destination.Shops },
                        onRefreshClick = {},
                        onEmployeesClick = { destination = Destination.Employees },
                        onSuppliersClick = { destination = Destination.Suppliers },
                        onCustomersClick = { destination = Destination.Customers },
                        onSalesClick = { destination = Destination.MainTabs; selectedTab = AppTab.Sales },
                        onNewSaleClick = { destination = Destination.NewSale },
                        onAddProductClick = { destination = Destination.AddProduct },
                        onAddExpenseClick = { destination = Destination.AddExpense },
                        onInventoryClick = { destination = Destination.MainTabs; selectedTab = AppTab.Inventory },
                        onProductClick = {},
                    )

                    Destination.AddExpense -> AddExpenseScreen(
                        state = addExpenseState,
                        onStateChange = { addExpenseState = it },
                        onBackClick = { destination = Destination.MainTabs },
                        onDateClick = { isExpenseDatePickerOpen = true },
                        // Mirrors Android's own showAttachmentOptions exactly:
                        // a picker dialog that just toasts the chosen option's
                        // name back — no real camera/gallery attachment
                        // happens there either.
                        onAttachReceiptClick = {},
                        onSaveClick = ::saveExpense,
                    )

                    if (isExpenseDatePickerOpen) {
                        val datePickerState = rememberDatePickerState()
                        DatePickerDialog(
                            onDismissRequest = { isExpenseDatePickerOpen = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val millis = datePickerState.selectedDateMillis
                                    if (millis != null) {
                                        val date = Instant.fromEpochMilliseconds(millis)
                                            .toLocalDateTime(TimeZone.UTC).date
                                        addExpenseDate = date.toString()
                                        addExpenseState = addExpenseState.copy(dateLabel = date.toDisplayLabel())
                                    }
                                    isExpenseDatePickerOpen = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { isExpenseDatePickerOpen = false }) { Text("Cancel") }
                            },
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    Destination.ExpenseCategories -> ExpenseCategoriesScreen(
                        categories = expenseCategoriesUi,
                        isLoading = isExpenseCategoriesLoading,
                        onBackClick = { destination = Destination.MainTabs },
                        onCreateCategory = ::createExpenseCategory,
                        onUpdateCategory = ::updateExpenseCategory,
                        onDeleteCategory = ::deleteExpenseCategory,
                    )

                    Destination.FinancialStatement -> FinancialStatementScreen(
                        state = financialStatementState,
                        periods = listOf("This Week", "This Month", "Last Month", "This Quarter", "This Year"),
                        onBackClick = { destination = Destination.MainTabs },
                        onPeriodSelected = {
                            financialStatementState = financialStatementState.copy(selectedPeriod = it)
                        },
                        onMetricClick = {},
                        onExportClick = {},
                        onPrintClick = {},
                    )

                    Destination.FinanceSettings -> FinanceSettingsScreen(
                        onBackClick = { destination = Destination.MainTabs },
                        onSaveClick = {},
                    )

                    Destination.AllTransactions -> {
                        TransactionsScreen(
                            state = transactionsState,
                            onBackClick = { destination = Destination.MainTabs },
                            onRefreshClick = {},
                            onExportClick = {},
                            onAddIncomeClick = {},
                            onAddExpenseClick = { destination = Destination.AddExpense },
                            onAddTransferClick = {},
                            onTransactionClick = {},
                            onTransactionAction = { _, _ -> },
                            onAdvancedFilterClick = { isTransactionFilterOpen = true },
                        )
                        if (isTransactionFilterOpen) {
                            Dialog(
                                onDismissRequest = { isTransactionFilterOpen = false },
                                properties = DialogProperties(usePlatformDefaultWidth = false),
                            ) {
                                TransactionFilterScreen(
                                    state = transactionFilterState,
                                    categories = transactionCategories(),
                                    onStateChange = { transactionFilterState = it },
                                    onStartDateClick = {},
                                    onEndDateClick = {},
                                    onClear = ::clearTransactionFilters,
                                    onApply = ::applyTransactionFilters,
                                    onDismiss = { isTransactionFilterOpen = false },
                                )
                            }
                        }
                    }

                    Destination.UserRoles -> UserRolesScreen(
                        state = userRolesState,
                        onBack = { destination = Destination.MainTabs },
                        onRefresh = ::loadUserRoles,
                        onEditPermissions = ::editUserRolePermissions,
                        onDismissEdit = { userRolesState = userRolesState.copy(editState = null) },
                        onEditSearchQueryChange = ::filterUserRolesEditSearch,
                        onPermissionToggle = ::toggleUserRolePermission,
                        onSaveChanges = ::saveUserRoleChanges,
                    )

                    Destination.ContactUs -> {
                        val uriHandler = LocalUriHandler.current
                        SharedContactUsScreen(
                            contact = contactInfo,
                            isLoading = isContactUsSubmitting,
                            resetKey = contactUsResetKey,
                            onBack = { destination = Destination.MainTabs },
                            onCall = { uriHandler.openUri("tel:${contactInfo.phone}") },
                            onEmail = { uriHandler.openUri("mailto:${contactInfo.email}") },
                            onWhatsApp = {
                                uriHandler.openUri("https://wa.me/${contactInfo.whatsapp.filter { it.isDigit() }}")
                            },
                            onMaps = {
                                uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=${contactInfo.address.replace(" ", "+")}")
                            },
                            onFaq = { faqDetailTarget = it },
                            onViewAllFaqs = { destination = Destination.FaqList },
                            onSubmit = ::submitSupportMessage,
                        )
                    }

                    Destination.FaqList -> FaqScreen(
                        items = defaultFaqItems,
                        onBack = { destination = Destination.ContactUs },
                        onFaqClick = { faqDetailTarget = it },
                    )

                    Destination.Account -> SharedAccountScreen(
                        state = accountState,
                        confirmation = accountConfirmation,
                        onBack = { destination = Destination.MainTabs },
                        onRefresh = ::loadAccount,
                        onEditAccount = { isEditAccountOpen = true },
                        onUpdatePayment = { isPaymentMethodOpen = true },
                        onTwoFactorChanged = ::toggleTwoFactorAuth,
                        onLoginNotificationsChanged = ::toggleLoginNotifications,
                        onManageSessions = {},
                        onDeactivateAccount = { accountConfirmation = SharedAccountConfirmation.DEACTIVATE },
                        onDeleteAccount = { accountConfirmation = SharedAccountConfirmation.DELETE },
                        onDismissConfirmation = { accountConfirmation = null },
                        onConfirmAction = ::confirmAccountAction,
                    )
                }
            }

            faqDetailTarget?.let { id ->
                FaqDetailDialog(
                    faqId = id,
                    onDismiss = { faqDetailTarget = null },
                    onContactSupport = {
                        faqDetailTarget = null
                        destination = Destination.ContactUs
                    },
                )
            }

            if (isEditAccountOpen) {
                Dialog(
                    onDismissRequest = { isEditAccountOpen = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                ) {
                    val profile = accountState.profile
                    SharedEditAccountScreen(
                        initial = SharedEditableAccountDetails(
                            businessName = profile?.businessName.orEmpty(),
                            businessType = profile?.businessType.orEmpty(),
                            registrationNumber = profile?.registrationNumber.orEmpty(),
                            taxId = profile?.taxId.orEmpty(),
                            address = profile?.address.orEmpty(),
                        ),
                        onClose = { isEditAccountOpen = false },
                        onSave = ::saveEditedAccount,
                    )
                }
            }

            if (isPaymentMethodOpen) {
                Dialog(
                    onDismissRequest = { isPaymentMethodOpen = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                ) {
                    SharedPaymentMethodScreen(
                        initialMethod = accountState.profile?.paymentMethod.orEmpty(),
                        onClose = { isPaymentMethodOpen = false },
                        onSave = ::savePaymentMethod,
                    )
                }
            }

            if (isBarcodeScannerActive) {
                Dialog(
                    onDismissRequest = { isBarcodeScannerActive = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                ) {
                    BarcodeScannerView(
                        onResult = { code ->
                            isBarcodeScannerActive = false
                            addProductByBarcode(code)
                        },
                        onCancel = { isBarcodeScannerActive = false },
                    )
                }
            }

            // Anchored separately from DashboardScreen's own header button
            // (which only exposes onShopPickerClick, no slot for the menu
            // itself) — padding here approximates that button's position,
            // same tradeoff ShopPickerOverlay makes on Android.
            if (authState is AuthState.LoggedIn &&
                destination == Destination.MainTabs &&
                selectedTab == AppTab.Dashboard
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 56.dp, end = 28.dp),
                ) {
                    ShopPickerMenu(
                        shops = shops.map { ShopPickerItem(id = it.id, name = it.name) },
                        selectedShopId = selectedShopId,
                        expanded = isShopPickerOpen,
                        onDismiss = { isShopPickerOpen = false },
                        onSelect = { id ->
                            selectedShopId = id
                            isShopPickerOpen = false
                            id?.let { sessionProvider.saveCurrentShopId(it) }
                        },
                    )
                }
            }
        }
    }
}

private fun Throwable?.aiUserMessage(): String =
    this?.message?.takeIf(String::isNotBlank) ?: "Osebo AI is unavailable. Please try again."
