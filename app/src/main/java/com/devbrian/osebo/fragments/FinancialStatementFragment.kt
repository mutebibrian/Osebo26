package com.devbrian.osebo.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentFinancialStatementBinding
import com.devbrian.osebo.ui.viewmodels.FinancialStatementViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class FinancialStatementFragment : Fragment() {
    private var _binding: FragmentFinancialStatementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FinancialStatementViewModel by viewModels()

    private val periods = arrayOf(
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
        _binding = FragmentFinancialStatementBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupPeriodSpinner()
        setupClickListeners()
        setupChart()
        observeViewModel()

        loadData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_export_excel -> {
                    exportToExcel()
                    true
                }
                R.id.action_print -> {
                    printStatement()
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
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPeriod = periods[position]
                loadData()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupClickListeners() {
        binding.btnExport.setOnClickListener {
            exportToExcel()
        }

        binding.cardSales.setOnClickListener {
            showDetailsDialog("Sales", viewModel.getSalesDetails())
        }

        binding.cardPurchases.setOnClickListener {
            showDetailsDialog("Purchases", viewModel.getPurchasesDetails())
        }

        binding.cardExpenses.setOnClickListener {
            showDetailsDialog("Expenses", viewModel.getExpensesDetails())
        }

        binding.cardInventory.setOnClickListener {
            showDetailsDialog("Inventory", viewModel.getInventoryDetails())
        }
    }

    private fun setupChart() {
        binding.chartFinancial.description.isEnabled = false
        binding.chartFinancial.setTouchEnabled(true)
        binding.chartFinancial.setDragEnabled(true)
        binding.chartFinancial.setScaleEnabled(true)
        binding.chartFinancial.setPinchZoom(true)

        val xAxis = binding.chartFinancial.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -45f

        val leftAxis = binding.chartFinancial.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawZeroLine(true)

        binding.chartFinancial.axisRight.isEnabled = false
        binding.chartFinancial.legend.isEnabled = true
    }

    private fun observeViewModel() {
        viewModel.financialStatement.observe(viewLifecycleOwner) { statement ->
            updateUI(statement)
        }

        viewModel.timeSeriesData.observe(viewLifecycleOwner) { data ->
            updateChart(data)
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

    private fun loadData() {
        val (startDate, endDate) = getDateRangeForPeriod(selectedPeriod)
        viewModel.loadFinancialStatement(startDate, endDate, getPeriodForApi(selectedPeriod))
        viewModel.loadTimeSeriesData(startDate, endDate, getPeriodForApi(selectedPeriod))
    }

    private fun updateUI(statement: com.devbrian.osebo.models.FinancialStatement) {
        val formatter = NumberFormat.getNumberInstance(Locale.US)

        binding.tvSales.text = "UGX ${formatter.format(statement.sales.toInt())}"
        binding.tvPurchases.text = "UGX ${formatter.format(statement.purchases.toInt())}"
        binding.tvExpenses.text = "UGX ${formatter.format(statement.expenses.toInt())}"
        binding.tvGrossMargin.text = "UGX ${formatter.format(statement.grossMargin.toInt())}"
        binding.tvNetProfit.text = "UGX ${formatter.format(statement.netProfit.toInt())}"
        binding.tvInventory.text = "UGX ${formatter.format(statement.inventory.toInt())}"

        val profitMargin = if (statement.sales > 0) {
            (statement.netProfit / statement.sales * 100)
        } else 0.0

        binding.tvProfitMargin.text = "${String.format("%.1f", profitMargin)}%"

        // Set colors based on values
        val profitColor = if (statement.netProfit >= 0) {
            requireContext().getColor(R.color.green_success)
        } else {
            requireContext().getColor(R.color.red_error)
        }
        binding.tvNetProfit.setTextColor(profitColor)

        val marginColor = if (profitMargin >= 0) {
            requireContext().getColor(R.color.green_success)
        } else {
            requireContext().getColor(R.color.red_error)
        }
        binding.tvProfitMargin.setTextColor(marginColor)
    }

    private fun updateChart(timeSeries: List<com.devbrian.osebo.models.TimeSeriesData>) {
        if (timeSeries.isEmpty()) return

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        timeSeries.forEachIndexed { index, data ->
            entries.add(BarEntry(index.toFloat(), data.sales.toFloat()))
            labels.add(data.label)
        }

        val dataSet = BarDataSet(entries, "Revenue")
        dataSet.color = Color.rgb(76, 175, 80)
        dataSet.valueTextSize = 10f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "UGX ${NumberFormat.getNumberInstance(Locale.US).format(value.toInt())}"
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        binding.chartFinancial.data = barData

        val xAxis = binding.chartFinancial.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        binding.chartFinancial.invalidate()
    }

    private fun getDateRangeForPeriod(period: String): Pair<String?, String?> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return when (period) {
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

    private fun showDetailsDialog(title: String, details: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportToExcel() {
        Toast.makeText(requireContext(), "Exporting to Excel...", Toast.LENGTH_SHORT).show()
        viewModel.exportToExcel()
    }

    private fun printStatement() {
        Toast.makeText(requireContext(), "Printing statement...", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_financial_statement, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}