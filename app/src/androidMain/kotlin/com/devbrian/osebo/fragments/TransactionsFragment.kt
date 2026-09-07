package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.TransactionAdapter
import com.devbrian.osebo.databinding.FragmentTransactionsBinding
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.ui.viewmodels.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.loadTransactions()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_filter -> {
                    showFilterDialog()
                    true
                }
                R.id.action_export -> {
                    exportTransactions()
                    true
                }
                R.id.action_search -> {
                    showSearch()
                    true
                }
                else -> false
            }
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
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionMenu()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshTransactions()
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                showEmptyState()
            } else {
                showTransactionsState()
                transactionAdapter.submitTransactionList(transactions)
                updateSummary(transactions)
            }
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.filteredTransactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                showEmptyState()
            } else {
                showTransactionsState()
                transactionAdapter.submitTransactionList(transactions)
                updateSummary(transactions)
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
    }

    private fun updateSummary(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.type == Transaction.TYPE_INCOME }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == Transaction.TYPE_EXPENSE }.sumOf { it.amount }
        val netProfit = totalIncome - totalExpenses

        val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)

        binding.tvTotalIncome.text = "UGX ${formatter.format(totalIncome.toInt())}"
        binding.tvTotalExpenses.text = "UGX ${formatter.format(totalExpenses.toInt())}"
        binding.tvNetProfit.text = "UGX ${formatter.format(netProfit.toInt())}"

        val profitColor = if (netProfit >= 0) {
            requireContext().getColor(R.color.green_success)
        } else {
            requireContext().getColor(R.color.red_error)
        }
        binding.tvNetProfit.setTextColor(profitColor)
    }


    private fun showEmptyState() {
        binding.llEmpty.visibility = View.VISIBLE
        binding.rvTransactions.visibility = View.GONE
    }

    private fun showTransactionsState() {
        binding.llEmpty.visibility = View.GONE
        binding.rvTransactions.visibility = View.VISIBLE
    }



    private fun showTransactionDetails(transaction: Transaction) {
        Toast.makeText(
            requireContext(),
            "Transaction: ${transaction.description}\nAmount: UGX ${transaction.amount}",
            Toast.LENGTH_LONG
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
                else -> false
            }
        }

        popup.show()
    }

    private fun showFilterDialog() {
        val filterDialog = FilterDialogFragment()
        filterDialog.setOnFilterAppliedListener { filters ->
            viewModel.applyFilters(filters)
        }
        filterDialog.show(parentFragmentManager, "filter_dialog")
    }

    private fun showSearch() {
        
        Toast.makeText(requireContext(), "Search coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun exportTransactions() {
        Toast.makeText(requireContext(), "Exporting transactions...", Toast.LENGTH_SHORT).show()
        viewModel.exportTransactions()
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

    private fun deleteTransaction(transaction: Transaction) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTransaction(transaction.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToAddIncome() {
        Toast.makeText(requireContext(), "Add Income coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAddExpense() {
        findNavController().navigate(R.id.action_transactionsFragment_to_addExpenseFragment)
    }

    private fun navigateToAddTransfer() {
        Toast.makeText(requireContext(), "Add Transfer coming soon", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_transactions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
