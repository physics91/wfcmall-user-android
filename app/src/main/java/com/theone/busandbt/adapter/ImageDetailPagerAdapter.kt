package com.theone.busandbt.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.theone.busandbt.fragment.shop.ImageDetailFragment

class ImageDetailPagerAdapter(fa: Fragment, private val imageUrlList: List<String>) :
    FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return imageUrlList.size
    }

    override fun createFragment(position: Int): Fragment {
        return ImageDetailFragment(position, imageUrlList)
    }
}