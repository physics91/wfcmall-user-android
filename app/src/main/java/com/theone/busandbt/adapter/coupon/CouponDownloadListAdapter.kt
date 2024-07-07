package com.theone.busandbt.adapter.coupon

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemCouponDownloadBinding
import com.theone.busandbt.dto.coupon.ShopCouponListItem
import com.theone.busandbt.extension.setMargin
import com.theone.busandbt.extension.toCouponUseDateString
import com.theone.busandbt.extension.toLocalDateTime
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.busandbt.code.CouponUseType
import com.busandbt.code.DeliveryType
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.absoluteValue

/**
 * 쿠폰 다운로드 팝업 어뎁터
 */
class CouponDownloadListAdapter(
    couponList: List<ShopCouponListItem>
) :
    DataBindingListAdapter<ItemCouponDownloadBinding, ShopCouponListItem>() {

    companion object {
        private val SETTINGS_MAP = mapOf(
            DownloadStatus.ALREADY_DOWNLOAD to DownloadSettings(
                R.drawable.bg_coupon_already_download,
                R.drawable.ic_coupon_already_download,
                R.drawable.img_coupon_already_download,
                SizeUtils.dp2px(66f),
                "받기 완료!",
                R.color.couponDisabled
            ),
            DownloadStatus.EXHAUSTED to DownloadSettings(
                R.drawable.bg_coupon_exhausted,
                R.drawable.ic_coupon_exhausted,
                R.drawable.img_coupon_exhausted,
                SizeUtils.dp2px(53f),
                "전량 소진",
                R.color.couponDisabled
            ),
            DownloadStatus.DOWNLOADABLE to DownloadSettings(
                R.drawable.bg_coupon_downloadable,
                R.drawable.ic_coupon_downloadable,
                R.drawable.img_coupon_downloadable,
                SizeUtils.dp2px(64f),
                "쿠폰 받기",
                R.color.mainTextColor
            ),
        )
    }

    init {
        _items.addAll(couponList)
    }

    override val viewHolderLayoutId: Int = R.layout.item_coupon_download

    override fun doBind(
        binding: ItemCouponDownloadBinding,
        item: ShopCouponListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            coupon = item
            val downloadStatus = when {
                item.downloaded -> DownloadStatus.ALREADY_DOWNLOAD
                item.remainCount < 1 -> DownloadStatus.EXHAUSTED
                else -> DownloadStatus.DOWNLOADABLE
            }
            val settings = SETTINGS_MAP[downloadStatus] ?: return
            couponDownloadBackground.setImageResource(settings.backgroundResId)
            couponDownloadButton.setImageResource(settings.downloadButtonResId)
            couponDecoImageView.setImageResource(settings.decoResId)
            couponDownloadTextView.text = settings.downloadText
            couponDiscountCostTextView.setTextColor(ColorUtils.getColor(settings.discountCostTextColorResId))
            couponDecoImageView.layoutParams.width = settings.decoWidth

            packagingBadge.setMargin(
                left = if (item.deliveryTypeList.contains(DeliveryType.INSTANT.id)) SizeUtils.dp2px(
                    3f
                ) else 0
            )
            val now = LocalDateTime.now()
            val issuedDateTime = when (downloadStatus) {
                DownloadStatus.ALREADY_DOWNLOAD -> item.issuedDateTime?.toLocalDateTime()
                else -> now
            } ?: return
            val useEndDateTime = when (item.couponUseType) {
                CouponUseType.DAYS.id -> {
                    if (item.availableDays <= 0) return
                    issuedDateTime.plusDays(item.availableDays.toLong())
                }

                CouponUseType.PERIOD.id -> {
                    if (item.useEndDateTime == null) return
                    item.useEndDateTime.toLocalDateTime()
                }

                else -> return
            }
            root.isClickable = downloadStatus == DownloadStatus.ALREADY_DOWNLOAD
            val d = Duration.between(useEndDateTime, now)
            couponDeadlineTextView.text = "${d.toDays().absoluteValue}일 남음"
            couponDateTextView.text =
                "(${useEndDateTime?.toCouponUseDateString()}까지)"
        }
    }

    /**
     * @param decoWidth 캐릭터 이미지 사이즈가 각각 다르기 때문에 여기에 넣어서 구분해서 상태별로 width를 바꾸기 위함 (더 좋은 방법이 있으면 교체할것)
     */
    private class DownloadSettings(
        @DrawableRes val backgroundResId: Int,
        @DrawableRes val downloadButtonResId: Int,
        @DrawableRes val decoResId: Int,
        val decoWidth: Int,
        val downloadText: String,
        @ColorRes val discountCostTextColorResId: Int,
    )

    private enum class DownloadStatus {
        ALREADY_DOWNLOAD, // 이미 받은 쿠폰
        EXHAUSTED, // 전량 소진
        DOWNLOADABLE, // 다운로드 가능
        ;
    }
}