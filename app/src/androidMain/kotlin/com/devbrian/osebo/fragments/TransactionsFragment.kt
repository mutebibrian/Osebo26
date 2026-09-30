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
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.ui.screens.TransactionListAction
import com.devbrian.osebo.ui.screens.TransactionListItemUi
import com.devbrian.osebo.ui.screens.TransactionsScreen
import com.devbrian.osebo.ui.screens.TransactionsUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.TransactionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Locale

class TransactionsFragment : Fragment() {
    private val viewModel: TransactionViewModel by viewModel()
    private var uiState by mutableStateOf(TransactionsUiState())
    private var sourceTransactions: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                TransactionsScreen(
                    state = uiState,
                    onBackClick = { findNavController().navigateUp() },
                    onRefreshClick = viewModel::refreshTransactions,
                    onExportClick = ::exportTransactions,
                    onAddIncomeClick = { showMessage("Add income coming soon") },
                    onAddExpenseClick = {
                        findNavController().navigate(R.id.action_transactionsFragment_to_addExpenseFragment)
                    },
                    onAddTransferClick = { showMessage("Add transfer coming soon") },
                    onTransactionClick = { id -> findTransaction(id)?.let(::showTransactionDetails) },
                    onTransactionAction = ::handleTransactionAction,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.loadTransactions()
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            sourceTransactions = transactions
            updateUi(transactions)
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
    }

    private fun updateUi(transactions: List<Transaction>) {
        val income = transactions
            .filter { it.type.equals(Transaction.TYPE_INCOME, ignoreCase = true) }
            .sumOf { it.amount }
        val expenses = transactions
            .filter { it.type.equals(Transaction.TYPE_EXPENSE, ignoreCase = true) }
            .sumOf { it.amount }
        val net = income - expenses

        uiState = uiState.copy(
            transactions = transactions.map(::toUi),
            totalIncome = formatCurrency(income),
            totalExpenses = formatCurrency(expenses),
            netProfit = formatCurrency(net),
            isProfitPositive = net >= 0,
        )
    }

    private fun toUi(transaction: Transaction): TransactionListItemUi {
        val income = transaction.type.equals(Transaction.TYPE_INCOME, ignoreCase = true)
        return TransactionListItemUi(
            id = transaction.id,
            description = transaction.description.ifBlank { Transaction.getTypeDisplayName(transaction.type) },
            category = Transaction.getCategoryDisplayName(transaction.category),
            date = transaction.date.take(10),
            amount = "${if (income) "+" else "−"} ${formatCurrency(transaction.amount)}",
            rawType = transaction.type,
            typeLabel = Transaction.getTypeDisplayName(transaction.type),
            status = Transaction.getStatusDisplayName(transaction.status),
            isIncome = income,
        )
    }

    private fun handleTransactionAction(id: String, action: TransactionListAction) {
        val transaction = findTransaction(id) ?: return
        when (action) {
            TransactionListAction.ViewDetails -> showTransactionDetails(transaction)
            TransactionListAction.Edit -> showMessage("Edit transaction: ${transaction.id}")
            TransactionListAction.Duplicate -> showMessage("Duplicate transaction: ${transaction.id}")
            TransactionListAction.ExportReceipt -> showMessage("Export receipt: ${transaction.id}")
            TransactionListAction.Delete -> confirmDelete(transaction)
        }
    }

    private fun showTransactionDetails(transaction: Transaction) {
        showMessage("${transaction.description}\n${formatCurrency(transaction.amount)}")
    }

    private fun confirmDelete(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteTransaction(transaction.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportTransactions() {
        viewModel.exportTransactions()
        showMessage("Exporting transactions...")
    }

    private fun findTransaction(id: String): Transaction? = sourceTransactions.firstOrNull { it.id == id }

    private fun formatCurrency(value: Double): String =
        "UGX ${NumberFormat.getNumberInstance(Locale.US).format(value.toLong())}"

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
