package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentImageDetailBinding
import com.theone.busandbt.dialog.ImageDetailDialog
import com.theone.busandbt.fragment.DataBindingFragment

/**
 * 상점이나 메뉴 이미지 출력 화면
 */
class ImageDetailFragment(
    private val position: Int,
    private val imageUrlList: List<String>
) :
    DataBindingFragment<FragmentImageDetailBinding>() {
    override val layoutId: Int = R.layout.fragment_image_detail

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            shopDetailsImg.clipToOutline = true
            Glide.with(this@ImageDetailFragment)
                .load(imageUrlList[position])
                .into(shopDetailsImg)
            shopDetailsImg.setOnClickListener {
                ImageDetailDialog().apply {
                    arguments = bundleOf("imageUrlList" to imageUrlList.toTypedArray(), "position" to position)
                }.show(
                    childFragmentManager, null
                )
            }
        }
    }
}