package com.theone.busandbt.extension

import androidx.annotation.IdRes
import androidx.navigation.NavController

/**
 * 일반 popBackStack으로 했을때 홈 화면에서 다른 화면으로 이동안되는 현상 방지하기 위함
 * @param fragmentId fragment id를 지정한다.
 * 백스택의 이 화면이 있는 곳까지 pop을 한다. 같은 fragment가 여러개 있을 경우 해당 fragment 중 가장 최신 fragment까지만 pop한다.
 */
fun NavController.fixedPopBackStack(@IdRes fragmentId: Int) {
    val backList = currentBackStack.value.filter { it.destination.navigatorName == "fragment" }
    val index = backList.indexOfLast { it.destination.id == fragmentId }
    if (index == -1) return
    val count = backList.size - index - 1
    repeat(count) {
        popBackStack()
    }
}