package com.theone.busandbt.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class StoredPagerAdapter(
    open val fragment: Fragment
) : FragmentStateAdapter(fragment) {

    private val fragmentMap = mutableMapOf<Int, Fragment>()

    override fun createFragment(position: Int): Fragment {
        val result = defineFragment(position)
        fragmentMap[position] = result
        return result
    }

    abstract fun defineFragment(position: Int): Fragment
}