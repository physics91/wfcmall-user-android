package com.theone.busandbt.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 온보딩 어뎁터
 */
class OnBoardingPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val tabs: List<Fragment>
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return tabs.size
    }

    override fun createFragment(position: Int): Fragment {
        return tabs[position]
    }
}