package com.theone.busandbt.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.viewpager2.widget.ViewPager2
import com.theone.busandbt.R
import com.theone.busandbt.adapter.ImageListAdapter
import com.theone.busandbt.databinding.DialogMenuDetailsImgBinding

/**
 * 이미지 상세보기 팝업
 */
class ImageDetailDialog : DataBindingDialog<DialogMenuDetailsImgBinding>() {

    override val layoutId: Int = R.layout.dialog_menu_details_img

    override fun getTheme(): Int = R.style.dialogFullScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.window?.run {
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val windowManager =
                requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = windowManager.defaultDisplay
            val deviceSize = Point()
            display.getSize(deviceSize)
            val params = dialog!!.window!!.attributes
            params.width = deviceSize.x
            params.horizontalMargin = 0.0f
            dialog!!.window!!.attributes = params
        } catch (e: Exception) {
            // regardless
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        val imageUrlArray = args.getStringArray("imageUrlList") ?: emptyArray()
        val position = args.getInt("position", 0)
        with(binding) {
            allNum.text = imageUrlArray.size.toString()
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
            menuImgViewPager.post {
                menuImgViewPager.setCurrentItem(position, false)
            }
        }
    }
}