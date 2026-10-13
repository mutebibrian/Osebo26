package com.devbrian.osebo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.createOseboHttpClient
import com.devbrian.osebo.data.remote.dto.request.CreateExpenseRequest
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.request.SelectAccountRequest
import com.devbrian.osebo.data.remote.dto.response.ProductDto
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.repository.SharedCustomerRepository
import com.devbrian.osebo.data.repository.SharedDashboardRepository
import com.devbrian.osebo.data.repository.SharedFinanceRepository
import com.devbrian.osebo.data.repository.SharedInventoryRepository
import com.devbrian.osebo.data.repository.SharedReportsRepository
import com.devbrian.osebo.data.repository.SharedSalesRepository
import com.devbrian.osebo.data.repository.dateRangeForPeriod
import com.devbrian.osebo.data.repository.SaleCartItem
import com.devbrian.osebo.data.repository.cartSummary
import com.devbrian.osebo.data.remote.api.SupplierDto
import com.devbrian.osebo.data.remote.dto.request.AddSupplierRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateSupplierRequest
import com.devbrian.osebo.data.repository.SharedEmployeeRepository
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
import com.devbrian.osebo.data.remote.dto.request.EmployeeRequest
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.models.Employee
import com.devbrian.osebo.models.Role
import com.devbrian.osebo.models.ExpenseCategory
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.data.repository.currentDateLabel
import com.devbrian.osebo.data.repository.currentGreeting
import com.devbrian.osebo.data.settings.SettingsStoreSessionProvider
import com.devbrian.osebo.data.settings.createSettingsStore
import com.devbrian.osebo.ui.components.BottomNavBar
import com.devbrian.osebo.ui.components.NavItem
import com.devbrian.osebo.ui.components.ShopPickerItem
import com.devbrian.osebo.ui.components.ShopPickerMenu
import com.devbrian.osebo.ui.screens.AddExpenseScreen
import com.devbrian.osebo.ui.screens.AddExpenseUiState
import com.devbrian.osebo.ui.screens.AddStockProductUi
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
import com.devbrian.osebo.ui.screens.SuppliersScreen
import com.devbrian.osebo.ui.screens.SuppliersUiState
import com.devbrian.osebo.ui.screens.SupplierFormDialog
import com.devbrian.osebo.ui.screens.SupplierFormUiState
import com.devbrian.osebo.ui.screens.ExpenseCategoryOption
import com.devbrian.osebo.ui.screens.ExpenseCategoryUi
import com.devbrian.osebo.ui.screens.NewSaleScreen
import com.devbrian.osebo.ui.screens.NewSaleUiState
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
import com.devbrian.osebo.ui.screens.ReportsScreen
import com.devbrian.osebo.ui.screens.ReportsUiState
import com.devbrian.osebo.ui.screens.SalesScreen
import com.devbrian.osebo.ui.screens.SalesUiState
import com.devbrian.osebo.ui.screens.SelectAccountScreen
import com.devbrian.osebo.ui.screens.SelectAccountUiState
import com.devbrian.osebo.ui.screens.SplashScreen
import com.devbrian.osebo.ui.screens.TransactionsScreen
import com.devbrian.osebo.ui.screens.TransactionsUiState
import com.devbrian.osebo.ui.screens.WelcomeScreen
import com.devbrian.osebo.ui.theme.OseboTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private enum class AppTab(val label: String) {
    Dashboard("Dashboard"),
    Sales("Sales"),
    Inventory("Inventory"),
    Finance("Finance"),
    Customers("Customers"),
}

/**
 * Screens pushed on top of the tab shell (not peer tabs — each has its own
 * back button wired to return to MainTabs). Reached from an existing
 * callback on one of the 5 tab screens below; see the when-blocks for which
 * callback triggers which push.
 *
 * Shops, Suppliers, UserRoles, Subscription, SubscriptionPackages, and
 * ShopDashboard aren't reachable yet — none of the 5 tab screens has an
 * existing callback that leads to them, and inventing one (a settings menu,
 * a drawer, a 6th tab) is a real navigation-design decision, not something
 * to guess at here. Employees is excluded for a different reason: it has
 * no back-navigation affordance of its own (no onBack param, no back icon
 * in its body) — pushing it would trap the user with no way back.
 */
private sealed class Destination {
    data object MainTabs : Destination()
    data object Reports : Destination()
    data object AddStock : Destination()
    data object RemoveStock : Destination()
    data object NewSale : Destination()
    data object Payment : Destination()
    data object AddEmployee : Destination()
    data object Employees : Destination()
    data object Suppliers : Destination()
    data object AddExpense : Destination()
    data object ExpenseCategories : Destination()
    data object FinancialStatement : Destination()
    data object FinanceSettings : Destination()
    data object AllTransactions : Destination()
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
        val scope = rememberCoroutineScope()

        var authState by remember { mutableStateOf<AuthState>(AuthState.Splash) }
        var loginState by remember { mutableStateOf(LoginUiState()) }
        var selectAccountState by remember { mutableStateOf(SelectAccountUiState()) }
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
        var expenseCategoriesSource by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }
        var expenseCategoriesUi by remember { mutableStateOf<List<ExpenseCategoryUi>>(emptyList()) }
        var isExpenseCategoriesLoading by remember { mutableStateOf(false) }
        var financialStatementState by remember { mutableStateOf(FinancialStatementUiState()) }
        var transactionsState by remember { mutableStateOf(TransactionsUiState()) }
        var addStockState by remember { mutableStateOf(AddStockUiState()) }
        var addStockSourceProducts by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
        var employeesState by remember { mutableStateOf(EmployeesUiState()) }
        var employeesSource by remember { mutableStateOf<List<Employee>>(emptyList()) }
        var employeeFormState by remember { mutableStateOf(EmployeeFormUiState()) }
        var employeeRoles by remember { mutableStateOf<List<Role>>(emptyList()) }
        var suppliersState by remember { mutableStateOf(SuppliersUiState()) }
        var suppliersSource by remember { mutableStateOf<List<SupplierDto>>(emptyList()) }
        var supplierEditorForm by remember { mutableStateOf<SupplierFormUiState?>(null) }
        var editingSupplier by remember { mutableStateOf<SupplierDto?>(null) }
        var newSaleState by remember { mutableStateOf(NewSaleUiState()) }
        var newSaleCart by remember { mutableStateOf<List<SaleCartItem>>(emptyList()) }
        var newSaleSourceProducts by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
        var newSaleQuery by remember { mutableStateOf("") }
        var newSaleCustomers by remember { mutableStateOf<List<Customer>>(emptyList()) }
        var selectedSaleCustomer by remember { mutableStateOf<Customer?>(null) }
        var paymentState by remember { mutableStateOf(PaymentUiState()) }
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
                newSaleState = NewSaleUiState()
                newSaleCart = emptyList()
                newSaleSourceProducts = emptyList()
                newSaleQuery = ""
                newSaleCustomers = emptyList()
                selectedSaleCustomer = null
                paymentState = PaymentUiState()
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
                is ApiResult.Error -> dashboardState = dashboardState.copy(isLoading = false)
                is ApiResult.NetworkError -> dashboardState = dashboardState.copy(isLoading = false)
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
                    val income = result.data.filter { Transaction.isPositive(it.type) }.sumOf { it.amount }
                    val expenses = result.data.filter { Transaction.isNegative(it.type) }.sumOf { it.amount }
                    val profit = income - expenses
                    transactionsState = transactionsState.copy(
                        isLoading = false,
                        transactions = result.data.map { it.toTransactionListItemUi() },
                        totalIncome = "UGX ${income.toInt()}",
                        totalExpenses = "UGX ${expenses.toInt()}",
                        netProfit = "UGX ${profit.toInt()}",
                        isProfitPositive = profit >= 0,
                    )
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

        LaunchedEffect(authState, selectedShopId, destination) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (destination != Destination.NewSale) return@LaunchedEffect

            val shopsResult = api.getShops()
            val shopId = selectedShopId ?: when (shopsResult) {
                is ApiResult.Success -> shopsResult.data.data?.firstOrNull()?.id
                else -> null
            }
            if (shopId == null) return@LaunchedEffect

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
                is ApiResult.Error -> newSaleState = newSaleState.copy(isLoading = false)
                is ApiResult.NetworkError -> newSaleState = newSaleState.copy(isLoading = false)
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
                        paymentState = PaymentUiState()
                        newSaleCart = emptyList()
                        newSaleQuery = ""
                        destination = Destination.MainTabs
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
                    // Sign-up isn't built for iOS yet — Register is a no-op for now,
                    // matching Welcome's own real button layout on Android.
                    onRegister = {},
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
                                onAddProductClick = { destination = Destination.AddStock },
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
                                onAddProductClick = {},
                                onStockTransfersClick = {},
                                onAddStockClick = { destination = Destination.AddStock },
                                onRemoveStockClick = { destination = Destination.RemoveStock },
                                onProductClick = {},
                            )
                            AppTab.Finance -> FinanceScreen(
                                state = financeState,
                                periods = listOf(
                                    "Today", "This Week", "This Month",
                                    "Last Month", "This Quarter", "This Year",
                                ),
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
                            AppTab.Customers -> CustomersScreen(state = customersState)
                        }

                        BottomNavBar(
                            items = listOf(
                                NavItem(Icons.Default.Dashboard, AppTab.Dashboard.label),
                                NavItem(Icons.Default.ShoppingCart, AppTab.Sales.label),
                                NavItem(Icons.Default.Inventory2, AppTab.Inventory.label),
                                NavItem(Icons.Default.AttachMoney, AppTab.Finance.label),
                                NavItem(Icons.Default.People, AppTab.Customers.label),
                            ),
                            selectedIndex = selectedTab.ordinal,
                            onItemSelected = { index -> selectedTab = AppTab.entries[index] },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                        )
                    }

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

                    Destination.NewSale -> NewSaleScreen(
                        state = newSaleState,
                        onBackClick = { destination = Destination.MainTabs },
                        onSearchChange = ::searchNewSaleProducts,
                        onScanClick = {},
                        onCustomerSelected = ::selectNewSaleCustomer,
                        onAddProduct = ::addProductToNewSale,
                        onQuantityChanged = ::changeNewSaleQuantity,
                        onRemoveItem = ::removeNewSaleItem,
                        onDiscountApplied = ::applyNewSaleDiscount,
                        onClearCart = ::clearNewSaleCart,
                        onHoldSale = {},
                        onCheckout = ::proceedToPaymentScreen,
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
                            )
                        }
                    }

                    Destination.AddExpense -> AddExpenseScreen(
                        state = addExpenseState,
                        onStateChange = { addExpenseState = it },
                        onBackClick = { destination = Destination.MainTabs },
                        onDateClick = {},
                        onAttachReceiptClick = {},
                        onSaveClick = ::saveExpense,
                    )

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

                    Destination.AllTransactions -> TransactionsScreen(
                        state = transactionsState,
                        onBackClick = { destination = Destination.MainTabs },
                        onRefreshClick = {},
                        onExportClick = {},
                        onAddIncomeClick = {},
                        onAddExpenseClick = { destination = Destination.AddExpense },
                        onAddTransferClick = {},
                        onTransactionClick = {},
                        onTransactionAction = { _, _ -> },
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
