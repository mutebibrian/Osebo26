package com.devbrian.osebo.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.response.SalesComparisonDto
import com.devbrian.osebo.data.remote.dto.response.ShopSummaryDto
import com.devbrian.osebo.databinding.FragmentReportsBinding
import com.devbrian.osebo.ui.viewmodels.ReportsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportsViewModel by viewModels()
    private lateinit var preferenceManager: PreferenceManager
    private var salesDataList = mutableListOf<SalesDataPoint>()

    companion object {
        private const val TAG = "ReportsFragment"
    }

    data class SalesDataPoint(
        val period: String,
        val sales: Double,
        val previousSales: Double
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())
        debugShopSelection()
        setupToolbar()
        setupPeriodSelector()
        setupChart()
        observeViewModel()

        val shopIdentifier = preferenceManager.getShopIdentifierForApi()
        if (shopIdentifier.isEmpty()) {
            Log.e(TAG, "No shop selected! Cannot load reports.")
            Toast.makeText(
                requireContext(),
                "No shop selected. Please go to Settings and select a shop.",
                Toast.LENGTH_LONG
            ).show()
            showNoShopSelectedMessage()
            return
        }

        Log.d(TAG, "Shop identifier found: $shopIdentifier. Loading reports...")
        viewModel.loadSalesComparison("monthly")
        viewModel.loadShopSummary()
    }

    private fun setupChart() {
        val barChart = binding.barChart

        // Configure chart appearance
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setPinchZoom(false)
        barChart.legend.isEnabled = true
        barChart.legend.textSize = 12f

        // Configure X-axis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textSize = 11f
        xAxis.setCenterAxisLabels(true)

        // Configure Y-axis (left)
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f
        leftAxis.textSize = 11f
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                return if (value >= 1000) "${formatter.format((value / 1000).toInt())}K" else formatter.format(value.toInt())
            }
        }

        // Disable right axis
        barChart.axisRight.isEnabled = false

        // Animation
        barChart.animateY(1000)

        // Add some padding
        barChart.setExtraOffsets(10f, 10f, 10f, 20f)
    }

    private fun updateChart(currentSales: Double, previousSales: Double, period: String) {
        val entries = ArrayList<BarEntry>()

        // Create data points for current and previous
        entries.add(BarEntry(0f, currentSales.toFloat()))
        entries.add(BarEntry(1f, previousSales.toFloat()))

        val dataSet = BarDataSet(entries, "Sales")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.green_success)
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                return "UGX ${formatter.format(value.toInt())}"
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        binding.barChart.data = barData

        // Set x-axis labels
        val xAxisLabels = arrayListOf("Current Period", "Previous Period")
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)

        binding.barChart.invalidate() // Refresh chart

        Log.d(TAG, "Chart updated with Current: $currentSales, Previous: $previousSales")
    }

    private fun debugShopSelection() {
        Log.d(TAG, "========== SHOP DEBUG ==========")
        val shopUuid = preferenceManager.getCurrentShopUuid()
        val shopId = preferenceManager.getCurrentShopId()
        val shopName = preferenceManager.getCurrentShopName()
        val identifier = preferenceManager.getShopIdentifierForApi()
        val hasShop = preferenceManager.hasCurrentShop()
        val isProperlySelected = preferenceManager.isShopProperlySelected()

        Log.d(TAG, "Shop UUID: '$shopUuid'")
        Log.d(TAG, "Shop ID: '$shopId'")
        Log.d(TAG, "Shop Name: '$shopName'")
        Log.d(TAG, "API Identifier: '$identifier'")
        Log.d(TAG, "Has Shop: $hasShop")
        Log.d(TAG, "Properly Selected: $isProperlySelected")
        Log.d(TAG, "=================================")
        preferenceManager.debugSubscriptionInfo()
    }

    private fun showNoShopSelectedMessage() {
        binding.tvTotalSales.text = "No Shop Selected"
        binding.tvSalesGrowth.text = "Select a shop"
        binding.tvPreviousSales.text = "UGX 0"
        binding.tvTotalRevenue.text = "UGX 0"
        binding.tvTotalExpenses.text = "UGX 0"
        binding.tvNetProfit.text = "UGX 0"
        binding.tvTotalOrders.text = "0"
        binding.tvAverageOrderValue.text = "UGX 0"
        binding.spinnerPeriod.isEnabled = false
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbar.title = "Reports & Analytics"
    }

    private fun setupPeriodSelector() {
        val periods = arrayOf("Daily", "Weekly", "Monthly", "Quarterly", "Yearly")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriod.adapter = adapter

        binding.spinnerPeriod.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val period = when (periods[position]) {
                    "Daily" -> "daily"
                    "Weekly" -> "weekly"
                    "Monthly" -> "monthly"
                    "Quarterly" -> "quarterly"
                    "Yearly" -> "yearly"
                    else -> "monthly"
                }
                Log.d(TAG, "Period changed to: $period")
                if (preferenceManager.getShopIdentifierForApi().isNotEmpty()) {
                    viewModel.loadSalesComparison(period)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.salesComparison.observe(viewLifecycleOwner) { data ->
            data?.let {
                Log.d(TAG, "SalesComparison received: Current=${it.currentSales}, Previous=${it.previousSales}, Growth=${it.growthPercentage}%")
                updateSalesChart(it)
                // Update chart with current and previous sales
                updateChart(it.currentSales, it.previousSales, it.range)
            } ?: run {
                Log.w(TAG, "SalesComparison data is null")
                showNoDataMessage()
            }
        }

        viewModel.shopSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                Log.d(TAG, "ShopSummary received: Total Sales=${it.totalSales}, Today Sales=${it.todaySales}")
                updateSummaryCards(it)
            } ?: run {
                Log.w(TAG, "ShopSummary data is null")
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            Log.d(TAG, "Loading state: $isLoading")
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e(TAG, "Error received: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
                showErrorMessage(it)
            }
        }
    }

    private fun updateSalesChart(data: SalesComparisonDto) {
        try {
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            val totalSales = data.currentSales
            val previousSales = data.previousSales
            val growthPercentage = data.growthPercentage

            binding.tvTotalSales.text = "UGX ${formatter.format(totalSales)}"
            binding.tvPreviousSales.text = "UGX ${formatter.format(previousSales)}"
            binding.tvSalesGrowth.text = String.format("%.1f%%", kotlin.math.abs(growthPercentage))

            if (data.isIncrease) {
                binding.tvSalesGrowth.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_success))
                binding.tvSalesGrowth.text = "↑ ${binding.tvSalesGrowth.text}"
            } else {
                binding.tvSalesGrowth.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_error))
                binding.tvSalesGrowth.text = "↓ ${binding.tvSalesGrowth.text}"
            }

            Log.d(TAG, "Sales chart updated - Current: $totalSales, Previous: $previousSales, Growth: $growthPercentage%")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating sales chart", e)
            binding.tvTotalSales.text = "UGX 0"
            binding.tvPreviousSales.text = "UGX 0"
            binding.tvSalesGrowth.text = "0%"
        }
    }

    private fun updateSummaryCards(summary: ShopSummaryDto) {
        try {
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            val totalRevenue = summary.totalRevenue
            val totalExpenses = summary.totalExpenses
            val netProfit = summary.netProfit
            val totalOrders = summary.totalOrders
            val averageOrderValue = summary.averageOrderValue

            binding.tvTotalRevenue.text = "UGX ${formatter.format(totalRevenue)}"
            binding.tvTotalExpenses.text = "UGX ${formatter.format(totalExpenses)}"
            binding.tvNetProfit.text = "UGX ${formatter.format(netProfit)}"
            binding.tvTotalOrders.text = totalOrders.toString()
            binding.tvAverageOrderValue.text = "UGX ${formatter.format(averageOrderValue)}"

            if (netProfit >= 0) {
                binding.tvNetProfit.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_success))
            } else {
                binding.tvNetProfit.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_error))
            }

            Log.d(TAG, "Summary cards updated - Revenue: $totalRevenue, Profit: $netProfit, Orders: $totalOrders")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating summary cards", e)
        }
    }

    private fun showNoDataMessage() {
        binding.tvTotalSales.text = "No Data"
        binding.tvPreviousSales.text = "UGX 0"
        binding.tvSalesGrowth.text = "0%"
        binding.tvTotalRevenue.text = "UGX 0"
        binding.tvTotalExpenses.text = "UGX 0"
        binding.tvNetProfit.text = "UGX 0"
        binding.tvTotalOrders.text = "0"
        binding.tvAverageOrderValue.text = "UGX 0"
    }

    private fun showErrorMessage(error: String) {
        binding.tvTotalSales.text = "Error"
        binding.tvSalesGrowth.text = "Failed"
        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        val shopIdentifier = preferenceManager.getShopIdentifierForApi()
        if (shopIdentifier.isNotEmpty()) {
            Log.d(TAG, "onResume: Refreshing reports data")
            val selectedPeriod = when(binding.spinnerPeriod.selectedItem?.toString()) {
                "Daily" -> "daily"
                "Weekly" -> "weekly"
                "Monthly" -> "monthly"
                "Quarterly" -> "quarterly"
                "Yearly" -> "yearly"
                else -> "monthly"
            }
            viewModel.loadSalesComparison(selectedPeriod)
            viewModel.loadShopSummary()
        } else {
            Log.w(TAG, "onResume: No shop selected, cannot refresh")
            showNoShopSelectedMessage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}