package com.theone.busandbt.extension

import android.widget.TextView
import androidx.annotation.StyleRes
import com.google.android.material.tabs.TabLayout
import com.theone.busandbt.R
import com.theone.busandbt.utils.ViewChangeUtils.updateTextViewAppearance

/**
 * 탭레이아웃의 확장함수
 * 탭이 선택 됐을 때 addOnTabSelectedListener 이벤트에 등록하여
 * 선택 된 탭의 폰트를 바꿔준다
 */
fun TabLayout.eventRegistrationTabSelectedDifferenceFont(
    tabUnSelect: Int = R.style.tabUnselectedTextAppearance,
    tabSelect: Int = R.style.tabSelectedTextAppearance
) {

    // 시작하자마자 현재 선택된 탭에 대한 처리
    val selectedTab = getTabAt(selectedTabPosition)
    selectedTab?.let {
        updateTextViewAppearance(
            it.view.context,
            it,
            tabSelect,
            this@eventRegistrationTabSelectedDifferenceFont
        )
    }

    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) = updateTextViewAppearance(
            tab.view.context,
            tab,
            tabSelect,
            this@eventRegistrationTabSelectedDifferenceFont
        )

        override fun onTabUnselected(tab: TabLayout.Tab) = updateTextViewAppearance(
            tab.view.context,
            tab,
            tabUnSelect,
            this@eventRegistrationTabSelectedDifferenceFont
        )

        override fun onTabReselected(tab: TabLayout.Tab) {}
    })
}

fun TabLayout.setTextAtChild(position: Int, text: CharSequence) {
    val tab = getTabAt(position)
    tab?.text = text
}

fun TabLayout.setTextAppearanceAtChild(position: Int, @StyleRes styleRes: Int) {
    val tab = getTabAt(position)
    val textView = tab?.view?.getChildAt(1) as? TextView ?: return
    textView.setTextAppearanceCompat(styleRes)
}
