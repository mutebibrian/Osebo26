package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.SaleAdapter
import com.devbrian.osebo.databinding.FragmentSalesBinding
import com.devbrian.osebo.models.Sale

class SalesFragment : Fragment() {
    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!
    private lateinit var saleAdapter: SaleAdapter

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
        loadSalesData()
    }

    private fun setupRecyclerView() {
        // FIX: Use parentheses and the named argument 'onItemClick ='
        saleAdapter = SaleAdapter(onItemClick = { sale ->
            // Handle sale item click
            showSaleDetails(sale)
        })

        binding.rvRecentSales.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = saleAdapter
            setHasFixedSize(true)
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

        binding.fabNewSale.setOnClickListener {
            navigateToNewSale()
        }
    }

    private fun loadSalesData() {
        // TODO: Load from ViewModel/Repository
        val recentSales = listOf(
            Sale(
                id = "SALE001",
                customerName = "John Doe",
                amount = 45000.0,
                date = "Dec 23, 2025",
                itemsCount = 3,
                status = "Completed"
            ),
            Sale(
                id = "SALE002",
                customerName = "Jane Smith",
                amount = 32000.0,
                date = "Dec 22, 2025",
                itemsCount = 2,
                status = "Completed"
            ),
            Sale(
                id = "SALE003",
                customerName = "Robert Johnson",
                amount = 78000.0,
                date = "Dec 21, 2025",
                itemsCount = 5,
                status = "Pending"
            )
        )

        saleAdapter.submitList(recentSales)

        // Update summary
        binding.tvTodaySales.text = "UGX 45,000"
        binding.tvMonthSales.text = "UGX 900,000"
    }

    private fun navigateToNewSale() {
        Toast.makeText(requireContext(), "Navigate to New Sale", Toast.LENGTH_SHORT).show()
        // Navigate to NewSaleActivity
    }

    private fun navigateToReports() {
        Toast.makeText(requireContext(), "Navigate to Reports", Toast.LENGTH_SHORT).show()
        // Navigate to ReportsActivity
    }

    private fun navigateToAllSales() {
        Toast.makeText(requireContext(), "Navigate to All Sales", Toast.LENGTH_SHORT).show()
        // Navigate to AllSalesActivity
    }

    private fun showSaleDetails(sale: Sale) {
        Toast.makeText(requireContext(), "Showing details for ${sale.id}", Toast.LENGTH_SHORT).show()
        // Show sale details dialog or navigate to detail screen
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