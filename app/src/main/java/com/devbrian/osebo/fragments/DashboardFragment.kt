package com.devbrian.osebo.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentDashboardBinding
import com.devbrian.osebo.ui.viewmodels.DashboardViewModel
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var topStockAdapter: TopStockAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()

        viewModel.loadDashboardData()
    }

    private fun setupRecyclerView() {
        topStockAdapter = TopStockAdapter { item ->
            val bundle = Bundle().apply {
                putString("productId", item.id)
                putString("product_name", item.name)
            }
            findNavController().navigate(R.id.productDetailsFragment, bundle)
        }

        binding.rvTopStock.apply {
            // FIX: Create a NEW horizontal LinearLayoutManager here.
            // The XML sets a default vertical one via app:layoutManager — this overrides it.
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = topStockAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
        }
    }

    private fun setupClickListeners() {
        binding.cardEmployees.setOnClickListener {
            findNavController().navigate(R.id.employeesFragment)
        }
        binding.cardSuppliers.setOnClickListener {
            findNavController().navigate(R.id.suppliersFragment)
        }
        binding.cardCustomers.setOnClickListener {
            findNavController().navigate(R.id.customersFragment)
        }
        binding.cardSales.setOnClickListener {
            findNavController().navigate(R.id.salesFragment)
        }
        binding.fabNewSale.setOnClickListener {
            findNavController().navigate(R.id.newSaleFragment)
        }
        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.addProductFragment)
        }
        binding.fabAddExpense.setOnClickListener {
            findNavController().navigate(R.id.addExpenseFragment)
        }
        binding.tvViewAllTopStock.setOnClickListener {
            findNavController().navigate(R.id.inventoryFragment)
        }
        binding.btnRetry.setOnClickListener {
            binding.errorLayout.visibility = View.GONE
            viewModel.refreshData()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collect { state ->
                when (state) {
                    is DashboardViewModel.DashboardState.Loading -> showLoading(true)
                    is DashboardViewModel.DashboardState.Success -> {
                        showLoading(false)
                        updateDashboardData(state.data)
                    }
                    is DashboardViewModel.DashboardState.Error -> {
                        showLoading(false)
                        handleError(state.message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timeSeriesData.collect { timeSeries ->
                timeSeries?.let { updateChart(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topStockItems.collect { items ->
                Log.d("DashboardFrag", "📱 Received ${items.size} top stock items")

                val adapterItems = items.map { dto ->
                    TopStockItem(
                        id = dto.id,
                        name = dto.name,
                        quantity = dto.totalQuantitySold,
                        sales = dto.totalSalesAmount
                    )
                }

                // FIX: submitList handles notifyDataSetChanged internally — no need to call it again
                topStockAdapter.submitList(adapterItems)

                // FIX: Show section based on whether items exist
                if (adapterItems.isEmpty()) {
                    binding.topStockSection.visibility = View.GONE
                } else {
                    binding.topStockSection.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isOffline.collect { isOffline ->
                if (isOffline) showOfflineIndicator()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lastUpdated.collect { lastUpdated ->
                lastUpdated?.let {
                    binding.tvLastUpdated.text = "Last updated: $it"
                    binding.tvLastUpdated.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateDashboardData(data: DashboardViewModel.DashboardData) {
        binding.tvEmployeesCount.text = data.employeesCount.toString()
        binding.tvSuppliersCount.text = data.suppliersCount.toString()
        binding.tvCustomersCount.text = data.customersCount.toString()
        binding.tvTotalSales.text = formatCurrency(data.totalSales)

        val profit = data.totalSales * 0.3
        binding.tvProfit.text = formatCurrency(profit)
    }

    private fun updateChart(timeSeries: DashboardViewModel.TimeSeriesDto) {
        val totalSales = timeSeries.sales.sum()
        val totalExpenses = timeSeries.expenses.sum()
        val profit = totalSales - totalExpenses

        binding.tvChartSummary.text = """
            📊 Sales: ${formatCurrency(totalSales)}
            💰 Expenses: ${formatCurrency(totalExpenses)}
            💵 Profit: ${formatCurrency(profit)}
        """.trimIndent()
    }

    private fun formatCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("UGX %.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("UGX %.1fK", amount / 1_000)
            else -> String.format("UGX %,.0f", amount)
        }
    }

    private fun showLoading(show: Boolean) {
        binding.swipeRefresh.isRefreshing = show
        if (show) {
            binding.shimmerLayout.visibility = View.VISIBLE
            binding.shimmerLayout.startShimmer()
            binding.contentLayout.visibility = View.GONE
            binding.errorLayout.visibility = View.GONE
        } else {
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
            binding.contentLayout.visibility = View.VISIBLE
        }
    }

    private fun handleError(message: String) {
        val hasCachedData = binding.tvEmployeesCount.text != "0" ||
                binding.tvTotalSales.text != "UGX 0"

        if (hasCachedData) {
            showOfflineIndicator()
        } else {
            binding.errorLayout.visibility = View.VISIBLE
            binding.contentLayout.visibility = View.GONE
            binding.shimmerLayout.visibility = View.GONE
            binding.tvErrorMessage.text = message
        }
    }

    private fun showOfflineIndicator() {
        Snackbar.make(binding.root, "You're offline. Showing cached data.", Snackbar.LENGTH_INDEFINITE)
            .setAction("Refresh") {
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    viewModel.refreshData()
                } else {
                    Toast.makeText(requireContext(), "Still offline", Toast.LENGTH_SHORT).show()
                }
            }
            .setActionTextColor(resources.getColor(R.color.colorPrimary, null))
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDataIfNeeded()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ─── Adapter ─────────────────────────────────────────────────────────────────

class TopStockAdapter(
    private val onItemClick: (TopStockItem) -> Unit
) : RecyclerView.Adapter<TopStockAdapter.ViewHolder>() {

    private var items = listOf<TopStockItem>()

    fun submitList(newItems: List<TopStockItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_stock, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvSales: TextView = itemView.findViewById(R.id.tvSales)

        // FIX: Accept position as a parameter instead of using adapterPosition inside bind()
        // adapterPosition can return -1 if the view is being recycled
        // In TopStockAdapter ViewHolder bind() — replace setBackgroundColor with:
        fun bind(item: TopStockItem, position: Int) {
            tvProductName.text = item.name
            tvQuantity.text = "Qty: ${item.quantity}"
            tvSales.text = when {
                item.sales >= 1_000_000 -> String.format("UGX %.1fM", item.sales / 1_000_000)
                item.sales >= 1_000    -> String.format("UGX %.1fK", item.sales / 1_000)
                else                   -> String.format("UGX %,.0f", item.sales)
            }

            val colors = listOf(
                R.color.avatar_blue,
                R.color.avatar_green,
                R.color.avatar_orange,
                R.color.avatar_purple,
                R.color.avatar_red
            )
            // FIX: cast itemView to MaterialCardView and use setCardBackgroundColor
            val card = itemView as com.google.android.material.card.MaterialCardView
            card.setCardBackgroundColor(
                itemView.context.getColor(colors[position % colors.size])
            )

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}


data class TopStockItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val sales: Double
)


fun Int.withAlpha(alpha: Float): Int {
    // Clamp alpha to valid range then apply over the existing RGB
    val a = (alpha.coerceIn(0f, 1f) * 255).toInt()
    return (this and 0x00FFFFFF) or (a shl 24)
}