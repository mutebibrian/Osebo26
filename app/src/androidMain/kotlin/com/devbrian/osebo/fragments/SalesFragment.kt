package com.devbrian.osebo.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.SaleAdapter
import com.devbrian.osebo.adapters.TopSellingProductAdapter
import com.devbrian.osebo.databinding.FragmentSalesBinding
import com.devbrian.osebo.ui.viewmodels.SalesViewModel
import com.devbrian.osebo.utils.CurrencyFormatter

class SalesFragment : Fragment() {
    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!
    private lateinit var saleAdapter: SaleAdapter
    private lateinit var topSellingProductAdapter: TopSellingProductAdapter
    private val viewModel: SalesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        setupSwipeRefresh()



        viewModel.loadRecentSales()
        viewModel.loadTopSellingProducts()
    }

    private fun setupRecyclerView() {
        saleAdapter = SaleAdapter(
            onItemClick = { sale ->
                showSaleDetails(sale)
            },
            onViewDetailsClick = { sale ->
                navigateToSaleDetails(sale.id)
            },
            onPrintReceiptClick = { sale ->
                printReceipt(sale)
            }
        )

        binding.rvRecentSales.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = saleAdapter
            setHasFixedSize(true)
        }

        topSellingProductAdapter = TopSellingProductAdapter(
            onItemClick = { product ->
                Toast.makeText(requireContext(), product.name, Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvTopSellingProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = topSellingProductAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.btnNewSale.setOnClickListener {
            navigateToNewSale()
        }

        binding.btnViewReports.setOnClickListener {
            navigateToReports()
        }

        binding.tvViewAll.setOnClickListener {
            navigateToAllSales()
        }

        binding.tvViewAllProducts.setOnClickListener {
            findNavController().navigate(R.id.inventoryFragment)
        }

        binding.fabNewSale.setOnClickListener {
            navigateToNewSale()
        }
    }



    private fun setupObservers() {
        viewModel.recentSales.observe(viewLifecycleOwner) { sales ->
            println("📱 SalesFragment - Received ${sales.size} sales")

            sales.forEachIndexed { index, sale ->
                println("📱 Sale[$index]: ID=${sale.id}, Amount=${sale.amount}, Status=${sale.status}, Date=${sale.date}")
            }

            
            if (sales.isEmpty()) {
                binding.layoutEmptySales.visibility = View.VISIBLE
                binding.rvRecentSales.visibility = View.GONE
            } else {
                binding.layoutEmptySales.visibility = View.GONE
                binding.rvRecentSales.visibility = View.VISIBLE
                saleAdapter.submitList(sales)
            }
        }

        viewModel.todaySalesTotal.observe(viewLifecycleOwner) { total ->
            println("📱 Today's total from ViewModel: $total")
            binding.tvTodaySales.text = CurrencyFormatter.formatFull(total)
        }

        viewModel.monthSalesTotal.observe(viewLifecycleOwner) { total ->
            println("📱 Month's total from ViewModel: $total")
            binding.tvMonthSales.text = CurrencyFormatter.formatFull(total)
        }

        viewModel.monthSalesCount.observe(viewLifecycleOwner) { count ->
            println("📱 Month's count from ViewModel: $count")
            binding.tvMonthTransactions.text = "$count transactions"
        }

        viewModel.topSellingProducts.observe(viewLifecycleOwner) { products ->
            if (products.isEmpty()) {
                binding.tvEmptyTopProducts.visibility = View.VISIBLE
                binding.rvTopSellingProducts.visibility = View.GONE
            } else {
                binding.tvEmptyTopProducts.visibility = View.GONE
                binding.rvTopSellingProducts.visibility = View.VISIBLE
                topSellingProductAdapter.submitList(products.take(5))
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }
    }
    private fun loadSalesData() {
        
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshSales()
            binding.swipeRefresh.isRefreshing = false
        }

        
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary),
            ContextCompat.getColor(requireContext(), R.color.green_success),
            ContextCompat.getColor(requireContext(), R.color.orange_warning)
        )
    }


    private fun navigateToNewSale() {
        findNavController().navigate(R.id.newSaleFragment)
    }

    private fun navigateToReports() {
        
        Toast.makeText(requireContext(), "Reports coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAllSales() {
        val sales = viewModel.recentSales.value
        if (sales.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No sales to display", Toast.LENGTH_SHORT).show()
            return
        }

        
        val salesList = sales.joinToString("\n\n") { sale ->
            """${sale.customerName}
           Amount: ${formatCurrency(sale.amount)}
           Status: ${sale.status}
           Date: ${sale.getFormattedDate()}""".trimIndent()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("All Sales (${sales.size})")
            .setMessage(salesList)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("UGX %.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("UGX %.1fK", amount / 1_000)
            amount == 0.0 -> "UGX 0"
            else -> String.format("UGX %.0f", amount)
        }
    }

    private fun navigateToSaleDetails(saleId: String) {
        
        Toast.makeText(requireContext(), "View details for $saleId", Toast.LENGTH_SHORT).show()
    }

    private fun showSaleDetails(sale: com.devbrian.osebo.models.Sale) {
        Toast.makeText(requireContext(), "Showing details for ${sale.id}", Toast.LENGTH_SHORT).show()
    }

    private fun printReceipt(sale: com.devbrian.osebo.models.Sale) {
        Toast.makeText(requireContext(), "Printing receipt for ${sale.id}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sales, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            R.id.action_export -> {
                exportSales()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        Toast.makeText(requireContext(), "Show Filter Dialog", Toast.LENGTH_SHORT).show()
    }

    private fun exportSales() {
        Toast.makeText(requireContext(), "Export Sales Data", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


