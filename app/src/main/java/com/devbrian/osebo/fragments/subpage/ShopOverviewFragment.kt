package com.devbrian.osebo.fragments.subpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devbrian.osebo.databinding.FragmentShopOverviewBinding
import com.devbrian.osebo.data.models.Shop
import java.text.NumberFormat
import java.util.*

class ShopOverviewFragment : Fragment() {

    private var _binding: FragmentShopOverviewBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_SHOP = "shop"

        fun newInstance(shop: Shop): ShopOverviewFragment {
            val fragment = ShopOverviewFragment()
            val args = Bundle()
            args.putSerializable(ARG_SHOP, shop)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var shop: Shop

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shop = arguments?.getSerializable(ARG_SHOP) as? Shop ?: return

        displayOverview()
    }

    private fun displayOverview() {
        val formatter = NumberFormat.getNumberInstance(Locale.US)

        
        binding.tvTotalRevenue.text = formatCurrency(shop.totalRevenue)
        binding.tvTotalExpenses.text = formatCurrency(shop.totalExpenses)
        binding.tvTotalProfit.text = formatCurrency(shop.profit)
        binding.tvTotalProducts.text = formatter.format(shop.totalProducts)
        binding.tvTotalEmployees.text = formatter.format(shop.totalEmployees)

        
        val profitMargin = if (shop.totalRevenue > 0) {
            (shop.profit / shop.totalRevenue) * 100
        } else {
            0.0
        }
        binding.tvProfitMargin.text = String.format("%.1f%%", profitMargin)

        
        binding.profitProgress.progress = profitMargin.toInt()
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
