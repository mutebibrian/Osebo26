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
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.request.SelectAccountRequest
import com.devbrian.osebo.data.repository.SharedCustomerRepository
import com.devbrian.osebo.data.repository.SharedDashboardRepository
import com.devbrian.osebo.data.repository.currentDateLabel
import com.devbrian.osebo.data.repository.currentGreeting
import com.devbrian.osebo.data.settings.SettingsStoreSessionProvider
import com.devbrian.osebo.data.settings.createSettingsStore
import com.devbrian.osebo.ui.components.BottomNavBar
import com.devbrian.osebo.ui.components.NavItem
import com.devbrian.osebo.ui.screens.AddExpenseScreen
import com.devbrian.osebo.ui.screens.AddExpenseUiState
import com.devbrian.osebo.ui.screens.AddStockScreen
import com.devbrian.osebo.ui.screens.AddStockUiState
import com.devbrian.osebo.ui.screens.CustomersScreen
import com.devbrian.osebo.ui.screens.CustomersUiState
import com.devbrian.osebo.ui.screens.DashboardScreen
import com.devbrian.osebo.ui.screens.DashboardUiState
import com.devbrian.osebo.ui.screens.ExpenseCategoriesScreen
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
        val scope = rememberCoroutineScope()

        var authState by remember { mutableStateOf<AuthState>(AuthState.Splash) }
        var loginState by remember { mutableStateOf(LoginUiState()) }
        var selectAccountState by remember { mutableStateOf(SelectAccountUiState()) }
        var selectedTab by remember { mutableStateOf(AppTab.Dashboard) }
        var destination by remember { mutableStateOf<Destination>(Destination.MainTabs) }
        var customersState by remember { mutableStateOf(CustomersUiState()) }
        var hasLoadedCustomers by remember { mutableStateOf(false) }
        var dashboardState by remember { mutableStateOf(DashboardUiState()) }
        var hasLoadedDashboard by remember { mutableStateOf(false) }

        remember(sessionProvider) {
            sessionProvider.onSessionExpiredListener = {
                authState = AuthState.LoggedOut
                loginState = LoginUiState()
                selectAccountState = SelectAccountUiState()
                customersState = CustomersUiState()
                hasLoadedCustomers = false
                dashboardState = DashboardUiState()
                hasLoadedDashboard = false
            }
        }

        LaunchedEffect(authState) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (hasLoadedCustomers) return@LaunchedEffect
            hasLoadedCustomers = true

            customersState = customersState.copy(isLoading = true, errorMessage = null)

            val shopsResult = api.getShops()
            val shopId = when (shopsResult) {
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

        LaunchedEffect(authState) {
            if (authState !is AuthState.LoggedIn) return@LaunchedEffect
            if (hasLoadedDashboard) return@LaunchedEffect
            hasLoadedDashboard = true

            dashboardState = dashboardState.copy(
                greeting = currentGreeting(),
                userName = sessionProvider.userFirstName() ?: "User",
                currentDate = currentDateLabel(),
                isLoading = true,
            )

            when (val result = dashboardRepository.loadShopsAndTotals()) {
                is ApiResult.Success -> {
                    val data = result.data
                    dashboardState = dashboardState.copy(
                        isLoading = false,
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
                                onReportsClick = { destination = Destination.Reports },
                                onAddProductClick = { destination = Destination.AddStock },
                            )
                            AppTab.Sales -> SalesScreen(
                                state = SalesUiState(),
                                onNewSaleClick = {},
                                onReportsClick = { destination = Destination.Reports },
                                onRefreshClick = {},
                                onSaleClick = {},
                            )
                            AppTab.Inventory -> InventoryScreen(
                                state = InventoryUiState(),
                                onProductsClick = {},
                                onStockTransfersClick = {},
                                onAddStockClick = { destination = Destination.AddStock },
                                onRemoveStockClick = {},
                                onBackToInventory = {},
                                onProductClick = {},
                            )
                            AppTab.Finance -> FinanceScreen(
                                state = FinanceUiState(),
                                periods = listOf("This Week", "This Month", "This Year"),
                                onPeriodSelected = {},
                                onRefreshClick = {},
                                onAddExpenseClick = { destination = Destination.AddExpense },
                                onNewSaleClick = {},
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
                        state = ReportsUiState(),
                        periods = listOf("Weekly", "Monthly", "Yearly"),
                        onBackClick = { destination = Destination.MainTabs },
                        onPeriodSelected = {},
                    )

                    Destination.AddStock -> AddStockScreen(
                        state = AddStockUiState(),
                        onBackClick = { destination = Destination.MainTabs },
                        onQueryChange = {},
                        onQuantityChange = { _, _ -> },
                        onDecreaseQuantity = {},
                        onIncreaseQuantity = {},
                        onAddStockClick = {},
                    )

                    Destination.AddExpense -> AddExpenseScreen(
                        state = AddExpenseUiState(),
                        onStateChange = {},
                        onBackClick = { destination = Destination.MainTabs },
                        onDateClick = {},
                        onAttachReceiptClick = {},
                        onSaveClick = {},
                    )

                    Destination.ExpenseCategories -> ExpenseCategoriesScreen(
                        categories = emptyList(),
                        isLoading = false,
                        onBackClick = { destination = Destination.MainTabs },
                        onCreateCategory = { _, _ -> },
                        onUpdateCategory = { _, _, _ -> },
                        onDeleteCategory = {},
                    )

                    Destination.FinancialStatement -> FinancialStatementScreen(
                        state = FinancialStatementUiState(),
                        periods = listOf("This Week", "This Month", "This Year"),
                        onBackClick = { destination = Destination.MainTabs },
                        onPeriodSelected = {},
                        onMetricClick = {},
                        onExportClick = {},
                        onPrintClick = {},
                    )

                    Destination.FinanceSettings -> FinanceSettingsScreen(
                        onBackClick = { destination = Destination.MainTabs },
                        onSaveClick = {},
                    )

                    Destination.AllTransactions -> TransactionsScreen(
                        state = TransactionsUiState(),
                        onBackClick = { destination = Destination.MainTabs },
                        onRefreshClick = {},
                        onExportClick = {},
                        onAddIncomeClick = {},
                        onAddExpenseClick = {},
                        onAddTransferClick = {},
                        onTransactionClick = {},
                        onTransactionAction = { _, _ -> },
                    )
                }
            }
        }
    }
}
