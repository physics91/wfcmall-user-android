package com.theone.busandbt.extension

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.utils.ViewChangeUtils.getStatusBarHeight


fun View.showMessageBar(message: String) {
    context.showMessageBar(message)
}

fun Context.showMessageBar(message: String) {
    val myToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
    val toastLayout: View =
        LayoutInflater.from(this).inflate(R.layout.toast_message_add_btn, null)
    toastLayout.setPadding(15, 0, 15, 0)
    toastLayout.findViewById<TextView>(R.id.messageTextView).text = message
//    SnackbarUtils.with(toastLayout).show()
    myToast.view = toastLayout
    myToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
    myToast.show()
}

fun View.navigate(
    @IdRes destinationId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null
) {
    val controller = findNavController()
    val dest = controller.currentDestination ?: return
    controller.navigate(
        destinationId,
        args,
        navOptions ?: NavOptions.Builder()
            .setPopUpTo(dest.id, false)
            .build()
    )
}

fun View.playAlphaAnimation() {
    if (alpha == 0f) animate().alpha(0f).alphaBy(1f).setDuration(50L).start()
}

/**
 * 뷰의 수직 위치를 전체 화면의 중간으로 위치시키게 한다.
 * 1. ConstraintLayout 내부의 뷰만 가능하다.
 * 2. onResume 이상에서 동작시킬때는 이걸 실행시킨 후에 requestLayout()을 호출해야한다.
 * 3. visibility가 이미 VISIBLE인 경우 정확히 계산이 되지 않는다. GONE 상태에서 하고 VISIBLE로 전환
 */
fun View.centerVerticalInWindow() {
    val fragment = findFragment<Fragment>()
    val activity = fragment.activity ?: return
    val windowHeight = activity.window?.decorView?.measuredHeight ?: return
    val fragmentHeight = fragment.view?.measuredHeight ?: return
    val bottomMargin = windowHeight - fragmentHeight
    (layoutParams as? ConstraintLayout.LayoutParams)?.bottomMargin = bottomMargin
}

fun View.setMarginsForSystemBars(context: Context) {
    val layoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    layoutParams.updateMargins(
        left = layoutParams.leftMargin,
        top = layoutParams.topMargin + getStatusBarHeight(context),
        right = layoutParams.rightMargin,
        bottom = layoutParams.bottomMargin
    )
}

fun View.setMargin(
    @Px left: Int = marginLeft,
    @Px top: Int = marginTop,
    @Px right: Int = marginRight,
    @Px bottom: Int = marginBottom
) {
    val layoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    layoutParams.updateMargins(
        left, top, right, bottom
    )
}

fun View.setDebounceClickListener(interval: Long = 1000, action: () -> Unit) {
    var lastClickTime = 0L
    this.setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            action.invoke()
        }
    }
}