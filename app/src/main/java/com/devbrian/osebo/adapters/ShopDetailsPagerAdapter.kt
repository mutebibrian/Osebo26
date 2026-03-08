package com.devbrian.osebo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.devbrian.osebo.fragments.subpage.ShopOverviewFragment
import com.devbrian.osebo.fragments.subpage.ShopStatisticsFragment
import com.devbrian.osebo.fragments.subpages.ShopSettingsFragment
import com.devbrian.osebo.fragments.subpages.ShopSubscriptionFragment
import com.devbrian.osebo.models.Shop

class ShopDetailsPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val shop: Shop,
    private val tabTitles: Array<String>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = tabTitles.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ShopOverviewFragment.newInstance(shop)
            1 -> ShopSubscriptionFragment.newInstance(shop)
            2 -> ShopStatisticsFragment.newInstance(shop.id)
            3 -> ShopSettingsFragment.newInstance(shop.id)
            else -> ShopOverviewFragment.newInstance(shop)
        }
    }
}