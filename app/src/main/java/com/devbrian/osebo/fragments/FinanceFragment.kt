package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.TransactionAdapter
import com.devbrian.osebo.databinding.FragmentFinanceBinding
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.ui.viewmodels.FinanceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class FinanceFragment : Fragment() {
    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter

    private val viewModel: FinanceViewModel by viewModels()

    private val periods = arrayOf(
        "Today",
        "This Week",
        "This Month",
        "Last Month",
        "This Quarter",
        "This Year"
    )

    private var selectedPeriod = "This Month"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if shop is selected
        if (!viewModel.hasShopSelected()) {
            showNoShopSelectedDialog()
            return
        }

        setupToolbar()
        setupPeriodSpinner()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Load initial data
        loadFinancialData()
    }

    private fun showNoShopSelectedDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("No Shop Selected")
            .setMessage("Please select a shop to view financial information.")
            .setPositiveButton("Select Shop") { _, _ ->
                findNavController().navigate(R.id.action_financeFragment_to_shopsFragment)
            }
            .setNegativeButton("Cancel") { _, _ ->
                findNavController().popBackStack()
            }
            .setCancelable(false)
            .show()
    }

    private fun observeViewModel() {
        // Observe transactions
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                showNoTransactionsState()
            } else {
                showTransactionsState()
                transactionAdapter.submitTransactionList(transactions)
                updateFinancialSummary(transactions)
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe financial statement
        viewModel.financialStatement.observe(viewLifecycleOwner) { statement ->
            updateFinancialStatementUI(statement)
        }

        // Observe shop name
        viewModel.currentShopName.observe(viewLifecycleOwner) { shopName ->
            binding.toolbar.subtitle = shopName
        }
    }

    private fun updateFinancialStatementUI(statement: com.devbrian.osebo.models.FinancialStatement) {
        val formatter = NumberFormat.getNumberInstance(Locale.US)

        binding.tvTotalIncome.text = "UGX ${formatter.format(statement.sales.toInt())}"
        binding.tvTotalExpenses.text = "UGX ${formatter.format(statement.expenses.toInt())}"
        binding.tvNetProfit.text = "UGX ${formatter.format(statement.netProfit.toInt())}"

        val profitMargin = if (statement.sales > 0) {
            (statement.netProfit / statement.sales * 100)
        } else 0.0

        binding.tvProfitPeriod.text = "$selectedPeriod • ${String.format("%.1f", profitMargin)}% profit margin"
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_filter -> {
                    showFilterDialog()
                    true
                }
                R.id.action_export -> {
                    exportFinancialData()
                    true
                }
                R.id.action_refresh -> {
                    refreshData()
                    true
                }
                R.id.action_settings -> {
                    navigateToFinanceSettings()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupPeriodSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            periods
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriod.adapter = adapter

        val defaultPosition = periods.indexOf(selectedPeriod)
        if (defaultPosition != -1) {
            binding.spinnerPeriod.setSelection(defaultPosition)
        }

        binding.spinnerPeriod.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedPeriod = periods[position]
                loadFinancialData()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onItemClick = { transaction ->
                showTransactionDetails(transaction)
            },
            onMoreOptionsClick = { transaction, anchorView ->
                showTransactionOptionsMenu(transaction, anchorView)
            }
        )

        transactionAdapter.showAttachments = true
        transactionAdapter.showNotes = true
        transactionAdapter.currencySymbol = "UGX"

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.cardAddExpense.setOnClickListener {
            navigateToAddExpense()
        }

        binding.cardViewReports.setOnClickListener {
            navigateToReports()
        }

        binding.cardCategories.setOnClickListener {
            navigateToExpenseCategories()
        }

        binding.cardStatement.setOnClickListener {
            navigateToFinancialStatement()
        }

        binding.cardExport.setOnClickListener {
            exportFinancialData()
        }

        binding.cardSettings.setOnClickListener {
            navigateToFinanceSettings()
        }

        binding.tvViewAllTransactions.setOnClickListener {
            navigateToAllTransactions()
        }

        binding.btnAddFirstTransaction.setOnClickListener {
            navigateToAddExpense()
        }

        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionMenu()
        }
    }

    private fun loadFinancialData() {
        // Load based on selected period
        val (startDate, endDate) = getDateRangeForPeriod(selectedPeriod)
        viewModel.loadTransactions(startDate, endDate)
        viewModel.loadFinancialStatement(getPeriodForApi(selectedPeriod))
    }

    private fun getDateRangeForPeriod(period: String): Pair<String?, String?> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return when (period) {
            "Today" -> {
                val today = dateFormat.format(calendar.time)
                Pair(today, today)
            }
            "This Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val end = dateFormat.format(calendar.time)
                Pair(start, end)
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = dateFormat.format(calendar.time)
                Pair(start, end)
            }
            "Last Month" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = dateFormat.format(calendar.time)
                Pair(start, end)
            }
            "This Quarter" -> {
                val quarter = calendar.get(Calendar.MONTH) / 3
                calendar.set(Calendar.MONTH, quarter * 3)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.MONTH, quarter * 3 + 2)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = dateFormat.format(calendar.time)
                Pair(start, end)
            }
            "This Year" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                val end = dateFormat.format(calendar.time)
                Pair(start, end)
            }
            else -> Pair(null, null)
        }
    }

    private fun getPeriodForApi(period: String): String {
        return when (period) {
            "Today" -> "daily"
            "This Week" -> "weekly"
            "This Month" -> "monthly"
            "Last Month" -> "monthly"
            "This Quarter" -> "quarterly"
            "This Year" -> "yearly"
            else -> "monthly"
        }
    }

    private fun updateFinancialSummary(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.type == Transaction.TYPE_INCOME }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == Transaction.TYPE_EXPENSE }.sumOf { it.amount }
        val netProfit = totalIncome - totalExpenses
        val profitMargin = if (totalIncome > 0) (netProfit / totalIncome * 100) else 0.0

        val formatter = NumberFormat.getNumberInstance(Locale.US)

        binding.tvTotalIncome.text = "UGX ${formatter.format(totalIncome.toInt())}"
        binding.tvTotalExpenses.text = "UGX ${formatter.format(totalExpenses.toInt())}"
        binding.tvNetProfit.text = "UGX ${formatter.format(netProfit.toInt())}"
        binding.tvProfitPeriod.text = "$selectedPeriod • ${String.format("%.1f", profitMargin)}% profit margin"
    }

    private fun showNoTransactionsState() {
        binding.llNoTransactions.visibility = View.VISIBLE
        binding.rvTransactions.visibility = View.GONE
    }

    private fun showTransactionsState() {
        binding.llNoTransactions.visibility = View.GONE
        binding.rvTransactions.visibility = View.VISIBLE
    }

    private fun showTransactionDetails(transaction: Transaction) {
        Toast.makeText(
            requireContext(),
            "Transaction: ${transaction.description}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showTransactionOptionsMenu(transaction: Transaction, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_transaction_item, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_details -> {
                    showTransactionDetails(transaction)
                    true
                }
                R.id.action_edit_transaction -> {
                    editTransaction(transaction)
                    true
                }
                R.id.action_duplicate -> {
                    duplicateTransaction(transaction)
                    true
                }
                R.id.action_export_receipt -> {
                    exportReceipt(transaction)
                    true
                }
                R.id.action_mark_completed -> {
                    markTransactionCompleted(transaction)
                    true
                }
                R.id.action_delete_transaction -> {
                    deleteTransaction(transaction)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showAddTransactionMenu() {
        val popup = PopupMenu(requireContext(), binding.fabAddTransaction)
        popup.menuInflater.inflate(R.menu.menu_add_transaction, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_expense -> {
                    navigateToAddExpense()
                    true
                }
                R.id.action_quick_sale -> {
                    navigateToQuickSale()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    // Navigation methods
    private fun navigateToAddExpense() {
        findNavController().navigate(R.id.action_financeFragment_to_addExpenseFragment)
    }

    private fun navigateToQuickSale() {
        findNavController().navigate(R.id.action_financeFragment_to_newSaleFragment)
    }

    private fun navigateToReports() {
        Toast.makeText(requireContext(), "Reports coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToExpenseCategories() {
        findNavController().navigate(R.id.action_financeFragment_to_expenseCategoriesFragment)
    }

    private fun navigateToFinancialStatement() {
        findNavController().navigate(R.id.action_financeFragment_to_financialStatementFragment)
    }

    private fun navigateToAllTransactions() {
        findNavController().navigate(R.id.action_financeFragment_to_transactionsFragment)
    }

    private fun navigateToFinanceSettings() {
        Toast.makeText(requireContext(), "Finance Settings coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun editTransaction(transaction: Transaction) {
        Toast.makeText(requireContext(), "Edit Transaction: ${transaction.id}", Toast.LENGTH_SHORT).show()
    }

    private fun duplicateTransaction(transaction: Transaction) {
        Toast.makeText(requireContext(), "Duplicate Transaction: ${transaction.id}", Toast.LENGTH_SHORT).show()
    }

    private fun exportReceipt(transaction: Transaction) {
        Toast.makeText(requireContext(), "Export Receipt: ${transaction.id}", Toast.LENGTH_SHORT).show()
    }

    private fun markTransactionCompleted(transaction: Transaction) {
        Toast.makeText(requireContext(), "Mark Completed: ${transaction.id}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(requireContext(), "Deleted: ${transaction.id}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFilterDialog() {
        Toast.makeText(requireContext(), "Filter coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun exportFinancialData() {
        Toast.makeText(requireContext(), "Export coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun refreshData() {
        loadFinancialData()
        Toast.makeText(requireContext(), "Refreshing data...", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_finance, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            R.id.action_export -> {
                exportFinancialData()
                true
            }
            R.id.action_refresh -> {
                refreshData()
                true
            }
            R.id.action_settings -> {
                navigateToFinanceSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}