package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.databinding.FragmentFinanceSettingsBinding
import com.devbrian.osebo.ui.viewmodels.FinanceSettingsViewModel
import com.devbrian.osebo.utils.FinancialStatementExportUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FinanceSettingsFragment : Fragment() {
    private var _binding: FragmentFinanceSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FinanceSettingsViewModel by viewModel()

    private val periods = arrayOf("This Week", "This Month", "Last Month", "This Quarter", "This Year")
    private var selectedPeriod = "This Month"
    private var pendingExportFormat: ExportFormat? = null

    private enum class ExportFormat { PDF, EXCEL }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        setupPeriodSpinner()
        setupObservers()
        setupClickListeners()
    }

    private fun setupPeriodSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerExportPeriod.adapter = adapter

        val defaultPosition = periods.indexOf(selectedPeriod)
        if (defaultPosition != -1) {
            binding.spinnerExportPeriod.setSelection(defaultPosition)
        }

        binding.spinnerExportPeriod.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPeriod = periods[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressExport.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.statementReady.observe(viewLifecycleOwner) { statement ->
            val format = pendingExportFormat
            if (statement == null || format == null) return@observe
            pendingExportFormat = null
            viewModel.consumeStatementReady()

            val (startDate, endDate) = getDateRangeForPeriod(selectedPeriod)
            val dateRangeLabel = if (startDate != null && endDate != null) "$startDate to $endDate" else ""

            lifecycleScope.launch {
                try {
                    val file = if (format == ExportFormat.PDF) {
                        FinancialStatementExportUtils.generatePdf(
                            requireContext(), viewModel.getCurrentShopName(), selectedPeriod, dateRangeLabel, statement
                        )
                    } else {
                        FinancialStatementExportUtils.generateCsv(
                            requireContext(), viewModel.getCurrentShopName(), selectedPeriod, dateRangeLabel, statement
                        )
                    }
                    val mimeType = if (format == ExportFormat.PDF) "application/pdf" else "text/csv"
                    FinancialStatementExportUtils.shareFile(requireContext(), file, mimeType)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnExportPdf.setOnClickListener { startExport(ExportFormat.PDF) }
        binding.btnExportExcel.setOnClickListener { startExport(ExportFormat.EXCEL) }

        binding.btnSave.setOnClickListener {
            Toast.makeText(requireContext(), "Saving these settings isn't available yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startExport(format: ExportFormat) {
        pendingExportFormat = format
        val (startDate, endDate) = getDateRangeForPeriod(selectedPeriod)
        viewModel.loadStatementForExport(startDate, endDate, getPeriodForApi(selectedPeriod))
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
            "This Week" -> "weekly"
            "This Month" -> "monthly"
            "Last Month" -> "monthly"
            "This Quarter" -> "quarterly"
            "This Year" -> "yearly"
            else -> "monthly"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
