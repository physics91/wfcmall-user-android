package com.theone.busandbt.dialog

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.theone.busandbt.R
import com.theone.busandbt.databinding.DialogMenuDetailsImgBinding
import com.theone.busandbt.adapter.ImageListAdapter

/**
 * 메뉴 최상단 사진 클릭 시 이미지 보여주는 다이어로그
 */
class MenuImageDialog : DataBindingDialog<DialogMenuDetailsImgBinding>() {

    override val layoutId: Int = R.layout.dialog_menu_details_img

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        val imageUrlArray = args.getStringArray("imageUrlList") ?: emptyArray()
        with(binding) {
            exit.setOnClickListener {
                dismiss()
            }
            menuImgViewPager.adapter = ImageListAdapter(imageUrlArray)
            menuImgViewPager.apply {
                //옆으로 스와이핑할때 1/3 2/3 3/3 포지션 마다 숫자 바뀌기
                menuImgViewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int,
                    ) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                        Log.d("TEST onPageScrolled", position.toString())
                    }

                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        Log.d("TEST onPageSelected", position.toString())
                        count.text = (position + 1).toString()
                    }
                })
            }
        }
    }
}