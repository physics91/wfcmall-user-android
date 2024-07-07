package com.theone.busandbt.type

import androidx.annotation.ColorRes
import com.theone.busandbt.R

enum class OperViewType(val title: String, @ColorRes val badgeColorResId: Int) {
    ON_WORK("영업", R.color.shopInfoOnWorkBadge),
    REST("휴식", R.color.shopInfoRestBadge),
    ROUTINE_HOLIDAY("정기휴무", R.color.shopInfoRoutineHolidayBadge),
    TEMP_HOLIDAY("임시휴무", R.color.shopInfoTempHolidayBadge),
    HOLIDAY_INFO("휴무안내", R.color.shopInfoHolidayInfoBadge),
    ;
}