package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.models.Sale
import com.devbrian.osebo.ui.screens.SaleItemUi
import com.devbrian.osebo.ui.screens.SalesScreen
import com.devbrian.osebo.ui.screens.SalesUiState
import com.devbrian.osebo.ui.screens.TopSellingProductUi
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.SalesViewModel
import com.devbrian.osebo.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.koin.androidx.viewmodel.ext.android.viewModel

class SalesFragment : Fragment() {
    private val viewModel: SalesViewModel by viewModel()
    private var uiState by mutableStateOf(SalesUiState())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OseboTheme {
                    SalesScreen(
                        state = uiState,
                        onNewSaleClick = ::navigateToNewSale,
                        onReportsClick = ::showReportsMessage,
                        onRefreshClick = viewModel::refreshSales,
                        onSaleClick = ::showSaleDetails,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        viewModel.refreshSales()
    }

    private fun setupObservers() {
        viewModel.recentSales.observe(viewLifecycleOwner) { sales ->
            uiState = uiState.copy(recentSales = sales.map(::toSaleItemUi))
        }

        viewModel.todaySalesTotal.observe(viewLifecycleOwner) { total ->
            uiState = uiState.copy(todaySales = CurrencyFormatter.formatFull(total))
        }

        viewModel.todaySalesCount.observe(viewLifecycleOwner) { count ->
            uiState = uiState.copy(todayTransactions = count)
        }

        viewModel.monthSalesTotal.observe(viewLifecycleOwner) { total ->
            uiState = uiState.copy(monthSales = CurrencyFormatter.formatFull(total))
        }

        viewModel.monthSalesCount.observe(viewLifecycleOwner) { count ->
            uiState = uiState.copy(monthTransactions = count)
        }

        viewModel.salesGrowth.observe(viewLifecycleOwner) { growth ->
            uiState = uiState.copy(growthPercentage = growth)
        }

        viewModel.topSellingProducts.observe(viewLifecycleOwner) { products ->
            uiState = uiState.copy(
                topProducts = products.map { product ->
                    TopSellingProductUi(
                        id = product.id,
                        name = product.name,
                        unitsSold = "${product.totalQuantitySold} unit${if (product.totalQuantitySold == 1) "" else "s"} sold",
                        salesAmount = CurrencyFormatter.formatFull(product.totalSalesAmount),
                    )
                },
            )
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            uiState = uiState.copy(isLoading = isLoading)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun toSaleItemUi(sale: Sale): SaleItemUi {
        val date = parseSaleDate(sale.date)
        return SaleItemUi(
            id = sale.id,
            customerName = sale.customerName.ifBlank { "Walk-in Customer" },
            amount = CurrencyFormatter.formatFull(sale.amount),
            date = date?.let { DISPLAY_DATE_FORMAT.format(it) } ?: sale.getFormattedDate(),
            time = date?.let { DISPLAY_TIME_FORMAT.format(it) }.orEmpty(),
            itemsLabel = "${sale.itemsCount} item${if (sale.itemsCount == 1) "" else "s"}",
            paymentMethod = sale.paymentMethod
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                ?: "Cash",
            status = sale.status,
        )
    }

    private fun navigateToNewSale() {
        findNavController().navigate(R.id.newSaleFragment)
    }

    private fun showReportsMessage() {
        Toast.makeText(requireContext(), "Reports coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun showSaleDetails(saleId: String) {
        Toast.makeText(requireContext(), "Sale details for ${saleId.takeLast(8)}", Toast.LENGTH_SHORT).show()
    }

    private fun parseSaleDate(rawDate: String): Date? {
        rawDate.toLongOrNull()?.let { timestamp ->
            val milliseconds = if (timestamp < 10_000_000_000L) timestamp * 1_000 else timestamp
            return Date(milliseconds)
        }

        return INPUT_DATE_FORMATS.firstNotNullOfOrNull { format ->
            runCatching { format.parse(rawDate) }.getOrNull()
        }
    }

    companion object {
        private val DISPLAY_DATE_FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        private val DISPLAY_TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val INPUT_DATE_FORMATS = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
        )
    }
}
