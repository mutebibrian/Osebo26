package com.devbrian.osebo.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.TransactionAdapter
import com.devbrian.osebo.databinding.FragmentFinanceBinding
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.ui.viewmodels.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FinanceFragment : Fragment() {
    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter

    private val viewModel: FinanceViewModel by viewModel()

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

        
        if (!viewModel.hasShopSelected()) {
            showNoShopSelectedDialog()
            return
        }

        setupToolbar()
        setupPeriodSpinner()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        
        loadFinancialData()
    }

    private fun showNoShopSelectedDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("No Shop Selected")
            .setMessage("Please select a shop to view financial information.")
            .setPositiveButton("Select Shop") { _, _ ->
                findNavController().navigate(R.id.shopsFragment)
            }
            .setNegativeButton("Cancel") { _, _ ->
                findNavController().popBackStack()
            }
            .setCancelable(false)
            .show()
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                showNoTransactionsState()
            } else {
                showTransactionsState()
                transactionAdapter.submitTransactionList(transactions)
                updateFinancialSummary(transactions)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.financialStatement.observe(viewLifecycleOwner) { statement ->
            updateFinancialStatementUI(statement)
        }

        viewModel.currentShopName.observe(viewLifecycleOwner) { shopName ->
            binding.toolbar.subtitle = shopName
        }
    }

    private fun updateFinancialStatementUI(statement: com.devbrian.osebo.models.FinancialStatement) {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        binding.tvTotalIncome.text = "UGX ${formatter.format(statement.sales.toInt())}"
        binding.tvTotalExpenses.text = "UGX ${formatter.format(statement.expenses.toInt())}"

        val netProfit = statement.netProfit
        binding.tvNetProfit.text = "UGX ${formatter.format(netProfit.toInt())}"

        val profitColor = if (netProfit >= 0) {
            requireContext().getColor(R.color.green_success)
        } else {
            requireContext().getColor(R.color.red_error)
        }
        binding.tvNetProfit.setTextColor(profitColor)

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
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriod.adapter = adapter

        val defaultPosition = periods.indexOf(selectedPeriod)
        if (defaultPosition != -1) {
            binding.spinnerPeriod.setSelection(defaultPosition)
        }

        binding.spinnerPeriod.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
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
            Log.d("FinanceFragment", "Add Expense clicked - navigating to addExpenseFragment")
            findNavController().navigate(R.id.addExpenseFragment)
        }

        binding.cardViewReports.setOnClickListener {
            Log.d("FinanceFragment", "View Reports clicked - navigating to reportsFragment")
            findNavController().navigate(R.id.reportsFragment)
        }

        binding.cardCategories.setOnClickListener {
            Log.d("FinanceFragment", "Categories clicked - navigating to expenseCategoriesFragment")
            findNavController().navigate(R.id.expenseCategoriesFragment)
        }

        binding.cardStatement.setOnClickListener {
            Log.d("FinanceFragment", "Statement clicked - navigating to financialStatementFragment")
            findNavController().navigate(R.id.financialStatementFragment)
        }

        binding.cardExport.setOnClickListener {
            Log.d("FinanceFragment", "Export clicked")
            exportFinancialData()
        }

        binding.cardSettings.setOnClickListener {
            Log.d("FinanceFragment", "Settings clicked - navigating to financeSettingsFragment")
            findNavController().navigate(R.id.financeSettingsFragment)
        }

        binding.tvViewAllTransactions.setOnClickListener {
            Log.d("FinanceFragment", "View All Transactions clicked - navigating to transactionsFragment")
            findNavController().navigate(R.id.transactionsFragment)
        }

        binding.btnAddFirstTransaction.setOnClickListener {
            Log.d("FinanceFragment", "Add First Transaction clicked - navigating to addExpenseFragment")
            findNavController().navigate(R.id.addExpenseFragment)
        }

        binding.fabAddTransaction.setOnClickListener {
            Log.d("FinanceFragment", "FAB clicked - showing menu")
            showAddTransactionMenu()
        }
    }

    private fun loadFinancialData() {
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

        val profitColor = if (netProfit >= 0) {
            requireContext().getColor(R.color.green_success)
        } else {
            requireContext().getColor(R.color.red_error)
        }
        binding.tvNetProfit.setTextColor(profitColor)
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
        Toast.makeText(requireContext(), "Transaction: ${transaction.description}\nAmount: ${formatCurrency(transaction.amount)}", Toast.LENGTH_SHORT).show()
    }

    private fun formatCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("UGX %.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("UGX %.1fK", amount / 1_000)
            else -> String.format("UGX %,.0f", amount)
        }
    }

    private fun showTransactionOptionsMenu(transaction: Transaction, anchorView: View) {
        val popup = android.widget.PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_transaction_item, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_details -> { showTransactionDetails(transaction); true }
                R.id.action_edit_transaction -> { editTransaction(transaction); true }
                R.id.action_duplicate -> { duplicateTransaction(transaction); true }
                R.id.action_export_receipt -> { exportReceipt(transaction); true }
                R.id.action_mark_completed -> { markTransactionCompleted(transaction); true }
                R.id.action_delete_transaction -> { deleteTransaction(transaction); true }
                else -> false
            }
        }
        popup.show()
    }

    private fun showAddTransactionMenu() {
        val popup = android.widget.PopupMenu(requireContext(), binding.fabAddTransaction)
        popup.menuInflater.inflate(R.menu.menu_add_transaction, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_expense -> { findNavController().navigate(R.id.addExpenseFragment); true }
                R.id.action_quick_sale -> { findNavController().navigate(R.id.newSaleFragment); true }
                else -> false
            }
        }
        popup.show()
    }

    private fun editTransaction(transaction: Transaction) { Toast.makeText(requireContext(), "Edit: ${transaction.id}", Toast.LENGTH_SHORT).show() }
    private fun duplicateTransaction(transaction: Transaction) { Toast.makeText(requireContext(), "Duplicate: ${transaction.id}", Toast.LENGTH_SHORT).show() }
    private fun exportReceipt(transaction: Transaction) { Toast.makeText(requireContext(), "Export receipt: ${transaction.id}", Toast.LENGTH_SHORT).show() }
    private fun markTransactionCompleted(transaction: Transaction) { Toast.makeText(requireContext(), "Marked completed: ${transaction.id}", Toast.LENGTH_SHORT).show() }

    private fun deleteTransaction(transaction: Transaction) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ -> Toast.makeText(requireContext(), "Deleted: ${transaction.id}", Toast.LENGTH_SHORT).show() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFilterDialog() { Toast.makeText(requireContext(), "Filter coming soon", Toast.LENGTH_SHORT).show() }
    private fun exportFinancialData() { Toast.makeText(requireContext(), "Export coming soon", Toast.LENGTH_SHORT).show() }
    private fun refreshData() { loadFinancialData(); Toast.makeText(requireContext(), "Refreshing data...", Toast.LENGTH_SHORT).show() }
    private fun navigateToFinanceSettings() { findNavController().navigate(R.id.financeSettingsFragment) }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_finance, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> { showFilterDialog(); true }
            R.id.action_export -> { exportFinancialData(); true }
            R.id.action_refresh -> { refreshData(); true }
            R.id.action_settings -> { navigateToFinanceSettings(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
