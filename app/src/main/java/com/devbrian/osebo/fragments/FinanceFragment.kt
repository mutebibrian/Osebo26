package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.TransactionAdapter
import com.devbrian.osebo.databinding.FragmentFinanceBinding
import com.devbrian.osebo.models.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FinanceFragment : Fragment() {
    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter

    private val periods = arrayOf(
        "Today",
        "This Week",
        "This Month",
        "Last Month",
        "This Quarter",
        "This Year",
        "Custom Range"
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

        setupToolbar()
        setupPeriodSpinner()
        setupRecyclerView()
        setupClickListeners()
        loadFinancialData()
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
        
        binding.cardAddIncome.setOnClickListener {
            navigateToAddIncome()
        }

        binding.cardAddExpense.setOnClickListener {
            navigateToAddExpense()
        }

        binding.cardViewReports.setOnClickListener {
            navigateToReports()
        }

        binding.cardInvoices.setOnClickListener {
            navigateToInvoices()
        }

        binding.cardTax.setOnClickListener {
            navigateToTaxSummary()
        }

        binding.cardBudget.setOnClickListener {
            navigateToBudgetPlanning()
        }

        
        binding.tvViewAllTransactions.setOnClickListener {
            navigateToAllTransactions()
        }

        
        binding.btnAddFirstTransaction.setOnClickListener {
            navigateToAddIncome()
        }

        
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionMenu()
        }
    }

    private fun loadFinancialData() {
        binding.progressBar.visibility = View.VISIBLE

        
        binding.root.postDelayed({
            val transactions = generateSampleTransactions()

            if (transactions.isEmpty()) {
                showNoTransactionsState()
            } else {
                showTransactionsState()
                transactionAdapter.submitTransactionList(transactions)
                updateFinancialSummary(transactions)
            }

            binding.progressBar.visibility = View.GONE
        }, 1000)
    }

    private fun generateSampleTransactions(): List<Transaction> {
        return listOf(
            Transaction(
                id = "TRX001",
                description = "Sale - iPhone 15 Pro",
                amount = 45000.0,
                date = getFormattedDate(-1), 
                type = Transaction.TYPE_INCOME,
                category = Transaction.CATEGORY_SALES,
                paymentMethod = Transaction.PAYMENT_CASH,
                status = Transaction.STATUS_COMPLETED,
                notes = "Payment received from John Doe"
            ),
            Transaction(
                id = "TRX002",
                description = "Office Rent",
                amount = 15000.0,
                date = getFormattedDate(-2),
                type = Transaction.TYPE_EXPENSE,
                category = Transaction.CATEGORY_RENT,
                paymentMethod = Transaction.PAYMENT_BANK_TRANSFER,
                status = Transaction.STATUS_COMPLETED
            ),
            Transaction(
                id = "TRX003",
                description = "Utility Bills",
                amount = 8000.0,
                date = getFormattedDate(-3),
                type = Transaction.TYPE_EXPENSE,
                category = Transaction.CATEGORY_UTILITIES,
                paymentMethod = Transaction.PAYMENT_MOBILE_MONEY,
                status = Transaction.STATUS_PENDING
            ),
            Transaction(
                id = "TRX004",
                description = "Consulting Services",
                amount = 75000.0,
                date = getFormattedDate(-4),
                type = Transaction.TYPE_INCOME,
                category = Transaction.CATEGORY_SALES,
                paymentMethod = Transaction.PAYMENT_CARD,
                status = Transaction.STATUS_COMPLETED
            ),
            Transaction(
                id = "TRX005",
                description = "Internet Subscription",
                amount = 5000.0,
                date = getFormattedDate(-5),
                type = Transaction.TYPE_EXPENSE,
                category = Transaction.CATEGORY_UTILITIES,
                paymentMethod = Transaction.PAYMENT_MOBILE_MONEY,
                status = Transaction.STATUS_COMPLETED
            ),
            Transaction(
                id = "TRX006",
                description = "Inventory Purchase",
                amount = 25000.0,
                date = getFormattedDate(-6),
                type = Transaction.TYPE_EXPENSE,
                category = Transaction.CATEGORY_PURCHASE,
                paymentMethod = Transaction.PAYMENT_BANK_TRANSFER,
                status = Transaction.STATUS_COMPLETED,
                attachmentsCount = 2
            ),
            Transaction(
                id = "TRX007",
                description = "Client Refund",
                amount = 12000.0,
                date = getFormattedDate(-7),
                type = Transaction.TYPE_EXPENSE,
                category = Transaction.CATEGORY_OTHER,
                paymentMethod = Transaction.PAYMENT_CASH,
                status = Transaction.STATUS_REFUNDED,
                notes = "Refund for damaged goods"
            )
        )
    }

    private fun getFormattedDate(daysOffset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun updateFinancialSummary(transactions: List<Transaction>) {
        val totalIncome = transactionAdapter.getTotalIncome()
        val totalExpenses = transactionAdapter.getTotalExpenses()
        val netProfit = totalIncome - totalExpenses
        val profitMargin = if (totalIncome > 0) (netProfit / totalIncome * 100) else 0.0

        val formatter = NumberFormat.getNumberInstance(Locale.US)

        binding.tvTotalIncome.text = "UGX ${formatter.format(totalIncome.toInt())}"
        binding.tvTotalExpenses.text = "UGX ${formatter.format(totalExpenses.toInt())}"
        binding.tvNetProfit.text = "UGX ${formatter.format(netProfit.toInt())}"
        binding.tvProfitPeriod.text = "$selectedPeriod • ${String.format("%.1f", profitMargin)}% profit margin"

        
        binding.tvIncomeChange.text = "↑ 15% from last month"
        binding.tvExpenseChange.text = "↓ 8% from last month"
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
                R.id.action_add_income -> {
                    navigateToAddIncome()
                    true
                }
                R.id.action_add_expense -> {
                    navigateToAddExpense()
                    true
                }
                R.id.action_add_transfer -> {
                    navigateToAddTransfer()
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

    
    private fun navigateToAddIncome() {
        Toast.makeText(requireContext(), "Navigate to Add Income", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAddExpense() {
        Toast.makeText(requireContext(), "Navigate to Add Expense", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAddTransfer() {
        Toast.makeText(requireContext(), "Navigate to Add Transfer", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToQuickSale() {
        Toast.makeText(requireContext(), "Navigate to Quick Sale", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToReports() {
        Toast.makeText(requireContext(), "Navigate to Reports", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToInvoices() {
        Toast.makeText(requireContext(), "Navigate to Invoices", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToTaxSummary() {
        Toast.makeText(requireContext(), "Navigate to Tax Summary", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToBudgetPlanning() {
        Toast.makeText(requireContext(), "Navigate to Budget Planning", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAllTransactions() {
        Toast.makeText(requireContext(), "Navigate to All Transactions", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToFinanceSettings() {
        Toast.makeText(requireContext(), "Navigate to Finance Settings", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(requireContext(), "Show Filter Dialog", Toast.LENGTH_SHORT).show()
    }

    private fun exportFinancialData() {
        Toast.makeText(requireContext(), "Export Financial Data", Toast.LENGTH_SHORT).show()
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

