package com.theone.busandbt.utils

import android.content.res.Resources

/**
 * 단위 변환기
 */
@Deprecated("SizeUtils로 대체할 것")
object UnitConverter {
    //dp -> px 컨버터 : xml에서만 사용가능한 dp를 코드에서 사용하기 위해서 px로 변환
    fun dpToPx(dp: Int) = (dp * Resources.getSystem().displayMetrics.density).toInt()
}