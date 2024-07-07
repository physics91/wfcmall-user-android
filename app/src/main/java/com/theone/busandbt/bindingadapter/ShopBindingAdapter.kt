package com.theone.busandbt.bindingadapter

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.theone.busandbt.R
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.desc
import com.theone.busandbt.extension.toCommonMoneyForm
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.spanned.TypefaceSpanCompat
import com.busandbt.code.DeliveryType
import com.busandbt.code.PaymentType

/**
 * 상점 관련 기능의 BindingAdapter
 * TODO 별점, 주문완료시간, 배달료 범위,
 */
object ShopBindingAdapter {

    private val DISCOUNT_COLOR_SPAN = ForegroundColorSpan(MAIN_COLOR)

    /**
     * 할인 매장 쿠폰 할인 텍스트 형식을 지정한다.
     */
    @BindingAdapter("maxDiscountCost")
    @JvmStatic
    fun maxDiscountCostTextForm(view: TextView, maxDiscountCost: Int) {
        //쿠폰에 5000원 할인 부분 색을 변경해준다.
        val base = view.context.getString(R.string.maxDiscountCostFormat, maxDiscountCost)
        val ssb = SpannableStringBuilder(base)
        ssb.setSpan(
            DISCOUNT_COLOR_SPAN,
            3, // base의 형식이 변경되면 여기도 변경돼야 하는지 체크해야함
            base.indexOf('원') + 1, // base의 형식이 변경되면 여기도 변경돼야 하는지 체크해야함
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = ssb
    }

    /**
     * 쿠폰 다운로드 할인 텍스트 형식을 지정한다.
     */
    @BindingAdapter("maxDiscountCostForCouponDownload")
    @JvmStatic
    fun maxDiscountCostForCouponDownloadTextForm(view: TextView, maxDiscountCost: Int) {
        val base =
            view.context.getString(R.string.maxDiscountCostForCouponDownloadFormat, maxDiscountCost.toMoneyFormat())
        val ssb = SpannableStringBuilder(base)
        val startIndex = 3
        val endIndex = base.indexOf('원') + 1
        ssb.setSpan(
            DISCOUNT_COLOR_SPAN,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb.setSpan(
            TypefaceSpanCompat(
                ResourcesCompat.getFont(
                    view.context,
                    R.font.sult_bold
                ) ?: return,
                MAIN_COLOR
            ),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = ssb
    }

    /**
     * 최소 주문 금액 텍스트 형식을 지정한다.
     */
    @BindingAdapter("minOrderCost")
    @JvmStatic
    fun minOrderCostTextForm(view: TextView, minOrderCost: Int) {
        view.text =
            view.context.getString(R.string.minOrderCostFormat, minOrderCost.toMoneyFormat())
    }

    /**
     * 최소 주문 금액 텍스트 형식을 지정한다.
     */
    @BindingAdapter("deliveryTypeIdText")
    @JvmStatic
    fun deliveryTypeIdTextForm(view: TextView, deliveryTypeId: Int) {
        view.text = when (deliveryTypeId) {
            DeliveryType.INSTANT.id -> "배달"
            DeliveryType.PACKAGING.id -> "포장"
            DeliveryType.BUNDLE.id -> "묶음배송"
            DeliveryType.PARCEL.id -> "택배배송"
            else -> ""
        }
    }

    /**
     * 리뷰 별점/개수 표시
     */
    @BindingAdapter("star", "reviewCount", requireAll = true)
    @JvmStatic
    fun reviewText(view: TextView, star: Double, reviewCount: Int) {
        view.text = if (reviewCount > 999) "${star}(999+)" else "${star}($reviewCount)"
    }

    /**
     * 찜 개수 표시
     */
    @BindingAdapter("likeCount")
    @JvmStatic
    fun likeCount(view: TextView, likeCount: Int) {
        view.text = if (likeCount > 999) "999+" else "$likeCount"
    }

    /**
     * 주문 완료 시간 표시
     */
    @BindingAdapter("orderDoneMinutes")
    @JvmStatic
    fun orderDoneMinutesForm(view: TextView, orderDoneMinutes: Int) {
        view.text = view.context.getString(
            R.string.orderDoneMinutesFormat,
            (orderDoneMinutes - 5).takeIf { it >= 0 } ?: 0,
            orderDoneMinutes + 5
        )
    }

    /**
     * 배달비 범위 표시
     */
    @BindingAdapter("minDeliveryCost", "maxDeliveryCost", requireAll = true)
    @JvmStatic
    fun deliveryCostRangeForm(view: TextView, minDeliveryCost: Int, maxDeliveryCost: Int) {
        view.text =
            if (minDeliveryCost == maxDeliveryCost) minDeliveryCost.toCommonMoneyForm() else view.context.getString(
                R.string.deliveryCostRangeFormat,
                minDeliveryCost.toMoneyFormat(),
                maxDeliveryCost.toMoneyFormat()
            )
    }

    /**
     * 배달비 ,제거 범위 표시
     */
    @BindingAdapter("minDeliveryCostNotComma", "maxDeliveryCostNotComma", requireAll = true)
    @JvmStatic
    fun deliveryCostNotCommaRangeForm(view: TextView, minDeliveryCost: Int, maxDeliveryCost: Int) {
        view.text =
            if (minDeliveryCost == maxDeliveryCost) minDeliveryCost.toString() + "원" else view.context.getString(
                R.string.deliveryCostDecimalRangeFormat,
                minDeliveryCost,
                maxDeliveryCost
            )
    }

    /**
     * 배달 유형 마크
     */
    @BindingAdapter("deliveryTypeList")
    @JvmStatic
    fun deliveryTypeListForm(view: TextView, deliveryTypeList: List<Int>) {
        view.visibility = View.VISIBLE
        view.text = with(deliveryTypeList) {
            when {
                containsAll(
                    listOf(
                        DeliveryType.INSTANT.id,
                        DeliveryType.PACKAGING.id
                    )
                ) -> "배달·포장"
                else -> {
                    view.visibility = View.GONE
                    ""
                }
            }
        }
    }

    /**
     * 배달비 표시
     */
    @BindingAdapter("deliveryCost")
    @JvmStatic
    fun deliveryCostForm(view: TextView, deliveryCost: Int) {
        view.text = view.context.getString(
            R.string.deliveryCostFormat,
            deliveryCost.toMoneyFormat()
        )
    }

    /**
     * 배송비 표시
     */
    @BindingAdapter("parcelCost")
    @JvmStatic
    fun parcelCostForm(view: TextView, deliveryCost: Int) {
        view.text = view.context.getString(
            R.string.parcelCostFormat,
            deliveryCost.toMoneyFormat()
        )
    }


    /**
     * 결제방법 표시
     */
    @BindingAdapter("paymentTypeIdList")
    @JvmStatic
    fun paymentTypeListForm(view: TextView, paymentTypeIdList: List<Int>) {
        view.text = buildString {
            paymentTypeIdList.forEachIndexed { index, i ->
                try {
                    append(PaymentType.find(i).desc())
                    if (index != paymentTypeIdList.size - 1) append(",")
                } catch (t: Throwable) {
                    debugLog("결제방법", "에러")
                }
            }
        }
    }
}