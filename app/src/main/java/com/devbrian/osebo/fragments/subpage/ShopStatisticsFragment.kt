package com.devbrian.osebo.fragments.subpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.TopProductsAdapter
import com.devbrian.osebo.databinding.FragmentShopStatisticsBinding
import com.devbrian.osebo.models.SalesDataPoint
import com.devbrian.osebo.models.TopProduct
import com.devbrian.osebo.ui.viewmodels.StatisticsViewModel
import com.devbrian.osebo.utils.Resource
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ShopStatisticsFragment : Fragment() {

    private var _binding: FragmentShopStatisticsBinding? = null
    private val binding get() = _binding!!

    private val statisticsViewModel: StatisticsViewModel by viewModels()
    private lateinit var topProductsAdapter: TopProductsAdapter

    companion object {
        private const val ARG_SHOP_ID = "shop_id"
        private const val ARG_PERIOD = "period"

        fun newInstance(shopId: String, period: String = "month"): ShopStatisticsFragment {
            val fragment = ShopStatisticsFragment()
            val args = Bundle()
            args.putString(ARG_SHOP_ID, shopId)
            args.putString(ARG_PERIOD, period)
            fragment.arguments = args
            return fragment
        }
    }

    private var shopId: String = ""
    private var currentPeriod: String = "month"
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopId = arguments?.getString(ARG_SHOP_ID) ?: return
        currentPeriod = arguments?.getString(ARG_PERIOD) ?: "month"

        setupToolbar()
        setupRecyclerView()
        setupPeriodSelector()
        setupChart()
        setupClickListeners()
        loadStatistics()
        observeViewModels()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Statistics"
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_refresh -> {
                    refreshData()
                    true
                }
                R.id.action_export -> {
                    showExportDialog()
                    true
                }
                R.id.action_print -> {
                    showPrintDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        topProductsAdapter = TopProductsAdapter { product ->
            onProductSelected(product)
        }

        binding.rvTopProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = topProductsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupPeriodSelector() {
        binding.chipDay.setOnClickListener {
            updatePeriod("day")
        }

        binding.chipWeek.setOnClickListener {
            updatePeriod("week")
        }

        binding.chipMonth.setOnClickListener {
            updatePeriod("month")
        }

        binding.chipYear.setOnClickListener {
            updatePeriod("year")
        }

        // Set initial selection
        when (currentPeriod) {
            "day" -> binding.chipDay.isChecked = true
            "week" -> binding.chipWeek.isChecked = true
            "month" -> binding.chipMonth.isChecked = true
            "year" -> binding.chipYear.isChecked = true
        }
    }

    private fun updatePeriod(period: String) {
        if (currentPeriod != period) {
            currentPeriod = period
            loadStatistics()
        }
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            // X Axis configuration
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

            // Left Axis configuration
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.divider)
            axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            axisLeft.setDrawZeroLine(true)

            // Right Axis configuration
            axisRight.isEnabled = false

            // Legend configuration
            legend.isEnabled = true
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)

            // Empty state
            setNoDataText("No data available")
            setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

            // Value selection listener
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        showDataPointTooltip(it.x.toInt(), it.y)
                    }
                }

                override fun onNothingSelected() {
                    // Hide tooltip
                }
            })
        }
    }

    private fun setupClickListeners() {
        binding.btnViewAllProducts.setOnClickListener {
            navigateToAllProducts()
        }

        binding.cardTotalSales.setOnClickListener {
            showDetailedStats("sales")
        }

        binding.cardTotalOrders.setOnClickListener {
            showDetailedStats("orders")
        }

        binding.cardAvgOrderValue.setOnClickListener {
            showDetailedStats("avg_order")
        }

        binding.cardConversionRate.setOnClickListener {
            showDetailedStats("conversion")
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            statisticsViewModel.loadStatistics(shopId, currentPeriod)
            statisticsViewModel.loadSalesChartData(shopId, currentPeriod)
            statisticsViewModel.loadTopProducts(shopId, currentPeriod)
        }
    }

    private fun observeViewModels() {
        // Observe summary statistics
        statisticsViewModel.statisticsSummary.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { stats ->
                        displaySummaryStats(stats)
                    }
                    binding.progressBar.visibility = View.GONE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.text = resource.message ?: "Failed to load statistics"
                    binding.tvError.visibility = View.VISIBLE
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }
            }
        }

        // Observe sales chart data
        statisticsViewModel.salesChartData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { dataPoints ->
                        println("📊 Received ${dataPoints.size} data points")
                        if (dataPoints.isNotEmpty()) {
                            println("📊 First point: label=${dataPoints[0].label}, value=${dataPoints[0].value}")
                        }
                        updateChart(dataPoints)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Show loading on chart
                }
            }
        }

        // Observe top products
        statisticsViewModel.topProducts.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { products ->
                        topProductsAdapter.submitList(products)
                        binding.tvNoProducts.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                is Resource.Error -> {
                    binding.tvNoProducts.visibility = View.VISIBLE
                    binding.tvNoProducts.text = resource.message ?: "No products found"
                }
                is Resource.Loading -> {
                    // Show loading
                }
            }
        }
    }

    private fun displaySummaryStats(stats: StatisticsViewModel.StatisticsSummary) {
        binding.tvTotalSalesValue.text = numberFormat.format(stats.totalSales)
        binding.tvTotalOrdersValue.text = NumberFormat.getNumberInstance().format(stats.totalOrders)
        binding.tvAvgOrderValue.text = numberFormat.format(stats.averageOrderValue)
        binding.tvConversionRateValue.text = String.format("%.1f%%", stats.conversionRate)

        // Set trend indicators
        setTrendIndicator(binding.tvSalesTrend, binding.ivSalesTrend, stats.salesGrowth)
        setTrendIndicator(binding.tvOrdersTrend, binding.ivOrdersTrend, stats.ordersGrowth)
        setTrendIndicator(binding.tvAvgOrderTrend, binding.ivAvgOrderTrend, stats.avgOrderGrowth)
        setTrendIndicator(binding.tvConversionTrend, binding.ivConversionTrend, stats.conversionGrowth)

        // Update period text
        binding.tvPeriodRange.text = formatPeriodRange(stats.periodStart, stats.periodEnd)
    }

    private fun setTrendIndicator(textView: TextView, imageView: ImageView, growth: Double) {
        when {
            growth > 0 -> {
                textView.text = String.format("+%.1f%%", growth)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green))
                imageView.setImageResource(R.drawable.ic_trend_up)
                imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.success_green))
            }
            growth < 0 -> {
                textView.text = String.format("%.1f%%", growth)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red))
                imageView.setImageResource(R.drawable.ic_trend_down)
                imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.error_red))
            }
            else -> {
                textView.text = "0%"
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                imageView.setImageResource(R.drawable.ic_trend_flat)
                imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }
        }
    }

    private fun updateChart(dataPoints: List<SalesDataPoint>) {
        try {
            // Create entries using for loop
            val entries = ArrayList<Entry>()
            val labelList = ArrayList<String>()

            for (i in dataPoints.indices) {
                val point = dataPoints[i]
                // Explicitly access properties with safe casts
                val value = point.value
                val label = point.label

                entries.add(Entry(i.toFloat(), value.toFloat()))
                labelList.add(label)
            }

            if (entries.isEmpty()) {
                binding.lineChart.clear()
                binding.lineChart.setNoDataText("No sales data for this period")
                return
            }

            val dataSet = LineDataSet(entries, "Sales").apply {
                color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                lineWidth = 2f
                circleRadius = 4f
                setDrawCircleHole(true)
                setCircleHoleColor(ContextCompat.getColor(requireContext(), R.color.white))
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
                valueTextSize = 10f
                setDrawFilled(true)
                fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_chart_fill)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
            }

            val labels = labelList.toTypedArray()
            binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            val lineData = LineData(dataSet)
            binding.lineChart.data = lineData
            binding.lineChart.invalidate()
            binding.lineChart.animateX(1000)

        } catch (e: Exception) {
            println("❌ Error updating chart: ${e.message}")
            e.printStackTrace()
            binding.lineChart.clear()
            binding.lineChart.setNoDataText("Error loading chart data")
        }
    }

    private fun formatPeriodRange(start: Date?, end: Date?): String {
        return when {
            start != null && end != null -> {
                val startStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(start)
                val endStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(end)
                "$startStr - $endStr"
            }
            start != null -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(start)
            else -> "Current period"
        }
    }

    private fun showDataPointTooltip(index: Int, value: Float) {
        val formattedValue = NumberFormat.getCurrencyInstance(Locale.US).format(value)
        Toast.makeText(requireContext(), "Sales: $formattedValue", Toast.LENGTH_SHORT).show()
    }

    private fun onProductSelected(product: TopProduct) {
        Toast.makeText(requireContext(), "Viewing ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAllProducts() {
        Toast.makeText(requireContext(), "View all products", Toast.LENGTH_SHORT).show()
    }

    private fun showDetailedStats(type: String) {
        Toast.makeText(requireContext(), "Viewing $type details", Toast.LENGTH_SHORT).show()
    }

    private fun showExportDialog() {
        val options = arrayOf("PDF", "CSV", "Excel")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Export Statistics")
            .setItems(options) { _, which ->
                val format = options[which]
                exportData(format)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportData(format: String) {
        Toast.makeText(requireContext(), "Exporting as $format...", Toast.LENGTH_SHORT).show()
    }

    private fun showPrintDialog() {
        Toast.makeText(requireContext(), "Preparing print...", Toast.LENGTH_SHORT).show()
    }

    private fun refreshData() {
        loadStatistics()
        Toast.makeText(requireContext(), "Refreshing data...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}