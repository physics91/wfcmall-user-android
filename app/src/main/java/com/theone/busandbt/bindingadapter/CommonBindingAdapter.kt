package com.theone.busandbt.bindingadapter

import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.theone.busandbt.R
import com.theone.busandbt.extension.centerVerticalInWindow
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.toMoneyFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 공통적인 특성을 정의한다.
 */
object CommonBindingAdapter {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd")

    /**
     * Glide로 이미지를 로드한다.
     */
    @BindingAdapter("glideImageUrl")
    @JvmStatic
    fun glideImageUrl(target: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) return
        Glide.with(target.context)
            .load(imageUrl)
            .thumbnail(0.1f)
            .into(target)
    }

    /**
     * Glide로 이미지 로딩 실패할 경우 실패 이미지를 띄우도록 한다.
     */
    @BindingAdapter("glideImageUrl", "replacementImage", requireAll = true)
    @JvmStatic
    fun glideImageUrlWithReplacement(
        target: ImageView,
        imageUrl: String,
        replacementImage: Drawable?
    ) {
        if (imageUrl.isEmpty()) {
            target.setImageDrawable(replacementImage)
            return
        }
        Glide.with(target.context)
            .load(imageUrl)
            .thumbnail(0.3f)
            .into(target)
    }

    /**
     * $.$$$원 형식
     */
    @BindingAdapter("commonCost")
    @JvmStatic
    fun commonCost(target: TextView, cost: Int) {
        with(target) {
            text = context.getString(R.string.commonCost, cost.toMoneyFormat())
        }
    }

    /**
     * $.$$$ 형식
     */
    @BindingAdapter("moneyForm")
    @JvmStatic
    fun moneyForm(target: TextView, cost: Int) {
        with(target) {
            text = cost.toMoneyFormat()
        }
    }

    @BindingAdapter("localDateTime")
    @JvmStatic
    fun localDateTime(target: TextView, localDateTime: LocalDateTime) {
        with(target) {
            text = DATE_FORMATTER.format(localDateTime)
        }
    }

    /**
     * true면 [View.VISIBLE] false면 [View.GONE]
     */
    @BindingAdapter("visibleOrGone")
    @JvmStatic
    fun visibleOrGone(target: View, flag: Boolean) {
        target.visibility = if (flag) View.VISIBLE else View.GONE
    }

    /**
     * 실수형을 문자열로 표시한다.
     */
    @BindingAdapter("doubleToText")
    @JvmStatic
    fun doubleToText(target: TextView, value: Double) {
        target.text = value.toString()
    }

    /**
     *  모든 editText의 입력전 힌트 폰트와 입력 후 텍스트 폰트가 다르기 떄문에
     *  editText를 addTextChangedListener 에 등록하여 text가 힌트라면 semiBold 입력된 텍스트라면 medium 으로 폰트를 설정한다.
     */
    @BindingAdapter("fontDifference")
    @JvmStatic
    fun fontDifference(target: EditText, fontCheck: Boolean) {
        with(target) {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(frontNum: Editable?) {
                    val typefaceId =
                        if (fontCheck) if (text.isEmpty()) R.font.sult_semibold else R.font.sult_medium
                        else if (text.isEmpty()) R.font.sult_semibold else R.font.sult_regular
                    typeface = ResourcesCompat.getFont(target.context, typefaceId)
                }
            })
        }
    }

    /**
     * 뷰의 수직 위치를 전체 화면의 중간으로 위치시키게 한다.
     * ConstraintLayout 내부의 뷰만 가능하다.
     */
    @BindingAdapter("centerVerticalInWindow")
    @JvmStatic
    fun centerVerticalInWindow(target: View, flag: Boolean) {
        if (flag) {
            try {
                val fragment = target.findFragment<Fragment>()
                with(fragment) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                            target.centerVerticalInWindow()
                            if (target.isVisible) target.requestLayout()
                        }
                    }
                }
            } catch (t: IllegalStateException) {
                debugLog("centerVerticalInWindow", "프래그먼트에 속한 뷰가 아닙니다.")
            }
        }
    }

    /**
     * timestamp -> YYYY-MM-DD HH:mm:ss -> YYYY.MM.DD 형식
     */
    @BindingAdapter("dateFormat")
    @JvmStatic
    fun setDate(view: TextView, dateString: String?) {
        dateString?.let {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            val formattedDate = outputFormat.format(date)
            view.text = formattedDate
        }
    }

    @BindingAdapter("percentForm")
    @JvmStatic
    fun percentForm(view: TextView, percent: Int) {
        view.text = "$percent%"
    }
}