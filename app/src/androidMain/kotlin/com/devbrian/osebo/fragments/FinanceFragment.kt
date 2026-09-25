package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.models.FinancialStatement
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.ui.screens.FinanceScreen
import com.devbrian.osebo.ui.screens.FinanceTransactionAction
import com.devbrian.osebo.ui.screens.FinanceTransactionUi
import com.devbrian.osebo.ui.screens.FinanceUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.FinanceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FinanceFragment : Fragment() {
    private val viewModel: FinanceViewModel by viewModel()
    private var uiState by mutableStateOf(FinanceUiState())
    private var latestTransactions: List<Transaction> = emptyList()

    private val periods = listOf(
        "Today",
        "This Week",
        "This Month",
        "Last Month",
        "This Quarter",
        "This Year",
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                FinanceScreen(
                    state = uiState,
                    periods = periods,
                    onPeriodSelected = ::selectPeriod,
                    onRefreshClick = ::refreshData,
                    onAddExpenseClick = { navigateTo(R.id.addExpenseFragment) },
                    onNewSaleClick = { navigateTo(R.id.newSaleFragment) },
                    onReportsClick = { navigateTo(R.id.reportsFragment) },
                    onCategoriesClick = { navigateTo(R.id.expenseCategoriesFragment) },
                    onStatementClick = { navigateTo(R.id.financialStatementFragment) },
                    onExportClick = ::exportFinancialData,
                    onSettingsClick = { navigateTo(R.id.financeSettingsFragment) },
                    onViewAllClick = { navigateTo(R.id.transactionsFragment) },
                    onTransactionClick = { transactionId ->
                        findTransaction(transactionId)?.let(::showTransactionDetails)
                    },
                    onTransactionAction = ::handleTransactionAction,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.hasShopSelected()) {
            showNoShopSelectedDialog()
            return
        }

        observeViewModel()
        loadFinancialData()
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            latestTransactions = transactions
            uiState = uiState.copy(transactions = transactions.map(::toTransactionUi))
            updateSummaryFromTransactions(transactions)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            uiState = uiState.copy(isLoading = isLoading)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.financialStatement.observe(viewLifecycleOwner) { statement ->
            updateSummaryFromStatement(statement)
        }

        viewModel.currentShopName.observe(viewLifecycleOwner) { shopName ->
            uiState = uiState.copy(shopName = shopName)
        }
    }

    private fun selectPeriod(period: String) {
        if (period == uiState.selectedPeriod) return
        uiState = uiState.copy(selectedPeriod = period)
        loadFinancialData()
    }

    private fun loadFinancialData() {
        val (startDate, endDate) = getDateRangeForPeriod(uiState.selectedPeriod)
        viewModel.loadTransactions(startDate, endDate)
        viewModel.loadFinancialStatement(startDate, endDate)
    }

    private fun refreshData() {
        loadFinancialData()
        showMessage("Refreshing data...")
    }

    private fun updateSummaryFromStatement(statement: FinancialStatement) {
        val margin = if (statement.sales > 0) statement.netProfit / statement.sales * 100 else 0.0
        uiState = uiState.copy(
            totalIncome = formatFullCurrency(statement.sales),
            totalExpenses = formatFullCurrency(statement.expenses),
            netProfit = formatFullCurrency(statement.netProfit),
            profitMargin = String.format(Locale.US, "%.1f%% profit margin", margin),
            isProfitPositive = statement.netProfit >= 0,
        )
    }

    private fun updateSummaryFromTransactions(transactions: List<Transaction>) {
        val income = transactions
            .filter { it.type.equals(Transaction.TYPE_INCOME, ignoreCase = true) }
            .sumOf { it.amount }
        val expenses = transactions
            .filter { it.type.equals(Transaction.TYPE_EXPENSE, ignoreCase = true) }
            .sumOf { it.amount }
        val profit = income - expenses
        val margin = if (income > 0) profit / income * 100 else 0.0
        uiState = uiState.copy(
            totalIncome = formatFullCurrency(income),
            totalExpenses = formatFullCurrency(expenses),
            netProfit = formatFullCurrency(profit),
            profitMargin = String.format(Locale.US, "%.1f%% profit margin", margin),
            isProfitPositive = profit >= 0,
        )
    }

    private fun toTransactionUi(transaction: Transaction): FinanceTransactionUi {
        val isIncome = transaction.type.equals(Transaction.TYPE_INCOME, ignoreCase = true)
        return FinanceTransactionUi(
            id = transaction.id,
            description = transaction.description.ifBlank {
                Transaction.getTypeDisplayName(transaction.type)
            },
            category = Transaction.getCategoryDisplayName(transaction.category),
            date = transaction.date,
            status = Transaction.getStatusDisplayName(transaction.status),
            amount = "${if (isIncome) "+" else "−"} ${formatCurrency(transaction.amount)}",
            isIncome = isIncome,
        )
    }

    private fun handleTransactionAction(transactionId: String, action: FinanceTransactionAction) {
        val transaction = findTransaction(transactionId) ?: return
        when (action) {
            FinanceTransactionAction.ViewDetails -> showTransactionDetails(transaction)
            FinanceTransactionAction.Edit -> showMessage("Edit: ${transaction.id}")
            FinanceTransactionAction.Duplicate -> showMessage("Duplicate: ${transaction.id}")
            FinanceTransactionAction.ExportReceipt -> showMessage("Export receipt: ${transaction.id}")
            FinanceTransactionAction.MarkCompleted -> showMessage("Marked completed: ${transaction.id}")
            FinanceTransactionAction.Delete -> confirmDelete(transaction)
        }
    }

    private fun showTransactionDetails(transaction: Transaction) {
        showMessage("Transaction: ${transaction.description}\nAmount: ${formatCurrency(transaction.amount)}")
    }

    private fun confirmDelete(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ -> showMessage("Deleted: ${transaction.id}") }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun findTransaction(id: String): Transaction? = latestTransactions.firstOrNull { it.id == id }

    private fun showNoShopSelectedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("No Shop Selected")
            .setMessage("Please select a shop to view financial information.")
            .setPositiveButton("Select Shop") { _, _ -> navigateTo(R.id.shopsFragment) }
            .setNegativeButton("Cancel") { _, _ -> findNavController().popBackStack() }
            .setCancelable(false)
            .show()
    }

    private fun navigateTo(destinationId: Int) {
        runCatching { findNavController().navigate(destinationId) }
            .onFailure { showMessage("Unable to open this page") }
    }

    private fun exportFinancialData() {
        showMessage("Export coming soon")
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun formatFullCurrency(amount: Double): String =
        "UGX ${NumberFormat.getNumberInstance(Locale.US).format(amount.toInt())}"

    private fun formatCurrency(amount: Double): String = when {
        amount >= 1_000_000 -> String.format(Locale.US, "UGX %.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format(Locale.US, "UGX %.1fK", amount / 1_000)
        else -> String.format(Locale.US, "UGX %,.0f", amount)
    }

    private fun getDateRangeForPeriod(period: String): Pair<String?, String?> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return when (period) {
            "Today" -> {
                val today = dateFormat.format(calendar.time)
                today to today
            }
            "This Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                start to dateFormat.format(calendar.time)
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                start to dateFormat.format(calendar.time)
            }
            "Last Month" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                start to dateFormat.format(calendar.time)
            }
            "This Quarter" -> {
                val quarter = calendar.get(Calendar.MONTH) / 3
                calendar.set(Calendar.MONTH, quarter * 3)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.MONTH, quarter * 3 + 2)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                start to dateFormat.format(calendar.time)
            }
            "This Year" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                start to dateFormat.format(calendar.time)
            }
            else -> null to null
        }
    }

}
