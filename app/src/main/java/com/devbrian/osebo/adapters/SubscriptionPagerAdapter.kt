package com.devbrian.osebo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.devbrian.osebo.fragments.SubscriptionHistoryFragment
import com.devbrian.osebo.fragments.SubscriptionOverviewFragment

class SubscriptionPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val tabTitles: Array<String>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = tabTitles.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SubscriptionOverviewFragment()
            1 -> SubscriptionHistoryFragment()
            else -> SubscriptionOverviewFragment()
        }
    }
}
