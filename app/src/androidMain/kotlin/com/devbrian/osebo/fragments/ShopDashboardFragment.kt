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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.FragmentShopDashboardBinding
import com.devbrian.osebo.ui.viewmodels.DashboardViewModel
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShopDashboardFragment : Fragment() {

    private val args: ShopDashboardFragmentArgs by navArgs()

    private var _binding: FragmentShopDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var topStockAdapter: TopStockAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        val shopId = args.shopId
        Log.d("ShopDashboard", "Loading dashboard for shop: $shopId")

        
        val prefs = PreferenceManager.getInstance(requireContext())
        prefs.saveCurrentShopId(shopId)
        prefs.saveCurrentShopUuid(shopId)

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        setupBottomNavigation()
        observeViewModel()

        viewModel.loadDashboardData()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    
                    true
                }
                R.id.nav_sales -> {
                    findNavController().navigate(R.id.salesFragment)
                    true
                }
                R.id.nav_expenses -> {
                    findNavController().navigate(R.id.financeFragment)
                    true
                }
                R.id.nav_restock -> {
                    findNavController().navigate(R.id.inventoryFragment)
                    true
                }
                else -> false
            }
        }
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

                topStockAdapter.submitList(adapterItems)

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

        fun bind(item: TopStockItem, position: Int) {
            tvProductName.text = item.name
            tvQuantity.text = "Qty: ${item.quantity}"
            tvSales.text = when {
                item.sales >= 1_000_000 -> String.format("UGX %.1fM", item.sales / 1_000_000)
                item.sales >= 1_000 -> String.format("UGX %.1fK", item.sales / 1_000)
                else -> String.format("UGX %,.0f", item.sales)
            }

            val colors = listOf(
                R.color.avatar_blue,
                R.color.avatar_green,
                R.color.avatar_orange,
                R.color.avatar_purple,
                R.color.avatar_red
            )
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
