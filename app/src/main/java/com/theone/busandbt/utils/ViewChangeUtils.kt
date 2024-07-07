package com.theone.busandbt.utils

import android.content.Context
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.theone.busandbt.extension.setTextAppearanceCompat
import com.google.android.material.tabs.TabLayout

/**
 * 뷰 변화 관련 유틸
 */
object ViewChangeUtils {

    /**
     *시스템 바를 확장합니다.
     */
    fun initExpandSystemBar(window: Window?) {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // 확장할 플래그
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS  // 현재 플래그
        )
    }

    /**
     * 상태 바의 높이를 가져오는 함수
     */
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    /**
     * 네비게이션 바의 높이를 가져오는 함수
     */
    fun getNaviBarHeight(context: Context): Int {
        val resourceId: Int =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    /**
     * 확장한 시스템바를 초기화 해주는 함수
     */
    fun initSystemBarReset(requireActivity: FragmentActivity) {
        requireActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    /**
     * 탭 textAppearance 업데이트 할 때 사용하는 함수
     */
    fun updateTextViewAppearance(
        requireContext: Context,
        tab: TabLayout.Tab,
        styleRes: Int,
        tabLayout: TabLayout
    ) {
        (tabLayout.getChildAt(0) as ViewGroup).getChildAt(tab.position).apply {
            (this as LinearLayout).getChildAt(1)?.apply {
                (this as TextView).apply {
                    setTextAppearanceCompat(styleRes)
                }
            }
        }
    }
}