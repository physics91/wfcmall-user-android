package com.theone.busandbt.bindingadapter

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.ColorUtils
import com.busandbt.code.CardType
import com.theone.busandbt.R
import com.theone.busandbt.extension.toCardDesc

object CardBindingAdapter {
    @JvmStatic
    @BindingAdapter("cardItemType")
    fun setCardItemType(imageView: ImageView, cardType: Int?) {
        val iconRes = when (cardType) {
            CardType.BC.id -> R.drawable.ic_bc_card_text
            CardType.SHINHAN.id -> R.drawable.ic_shinhan_card_text
            CardType.NONGHYUB.id -> R.drawable.ic_nh_card_text
            CardType.KB.id -> R.drawable.ic_kb_card_text
            CardType.LOTTE.id -> R.drawable.ic_lotte_card_text
            CardType.SAMSUNG.id -> R.drawable.ic_samsung_card_text
            CardType.CITY.id -> R.drawable.ic_city_card_text
            CardType.HYUNDAI.id -> R.drawable.ic_hyundai_card_text
            CardType.HANA.id -> R.drawable.ic_hana_card_text
            CardType.WOORI.id -> R.drawable.ic_woori_card_text
            CardType.SU_HYUB.id -> R.drawable.ic_suhyup_card_text
            CardType.K_BANK.id -> R.drawable.ic_kbank_card_text
            CardType.KWANG_JU.id -> R.drawable.ic_kwangju_card_text
            CardType.JEJU.id -> R.drawable.ic_jeju_card_text
            else -> null
        }
        if (iconRes != null) {
            imageView.setImageResource(iconRes)
        } else {
            imageView.setImageDrawable(null)
        }
    }

    @JvmStatic
    @BindingAdapter("cardMiniItemType")
    fun setMiniCardItemType(imageView: ImageView, cardType: Int?) {
        val iconRes = when (cardType) {
            CardType.BC.id -> R.drawable.ic_bc_card_text_mini
            CardType.SHINHAN.id -> R.drawable.ic_shinhan_card_text_mini
            CardType.NONGHYUB.id -> R.drawable.ic_nh_card_text_mini
            CardType.KB.id -> R.drawable.ic_kb_card_text_mini
            CardType.LOTTE.id -> R.drawable.ic_lotte_card_text_mini
            CardType.SAMSUNG.id -> R.drawable.ic_samsung_card_text_mini
            CardType.CITY.id -> R.drawable.ic_city_card_text_mini
            CardType.HYUNDAI.id -> R.drawable.ic_hyundai_card_text_mini
            CardType.HANA.id -> R.drawable.ic_hana_card_text_mini
            CardType.WOORI.id -> R.drawable.ic_woori_card_text_mini
            CardType.SU_HYUB.id -> R.drawable.ic_suhyup_card_text_mini
            CardType.K_BANK.id -> R.drawable.ic_kbank_card_text_mini
            CardType.KWANG_JU.id -> R.drawable.ic_kwangju_card_text_mini
            CardType.JEJU.id -> R.drawable.ic_jeju_card_text_mini
            else -> null
        }
        if (iconRes != null) {
            imageView.setImageResource(iconRes)
        } else {
            imageView.setImageDrawable(null)
        }
    }

    @JvmStatic
    @BindingAdapter("cardBackground")
    fun cardBackground(imageView: ImageView, cardType: Int?) {
        val colorRes = when (cardType) {
            CardType.BC.id -> R.color.bcCard
            CardType.SHINHAN.id -> R.color.shinhanCard
            CardType.NONGHYUB.id -> R.color.nonghyubCard
            CardType.KB.id -> R.color.kbCard
            CardType.LOTTE.id -> R.color.lotteCard
            CardType.SAMSUNG.id -> R.color.samsungCard
            CardType.CITY.id -> R.color.cityCard
            CardType.HYUNDAI.id -> R.color.hyundaiCard
            CardType.HANA.id -> R.color.hanaCard
            CardType.WOORI.id -> R.color.wooriCard
            CardType.SU_HYUB.id -> R.color.suhyubCard
            CardType.K_BANK.id -> R.color.kbankCard
            CardType.KWANG_JU.id -> R.color.kwangjuCard
            CardType.JEJU.id -> R.color.jejuCard
            else -> R.color.bcCard
        }
        imageView.setColorFilter(ColorUtils.getColor(colorRes))
    }

    @JvmStatic
    @BindingAdapter("cardTextType", "cardNumber")
    fun setCardTextType(textView: TextView, cardType: Int?, cardNumber: String?) {
        val cardDescription = cardType?.toCardDesc()
        val formattedCardNumber = cardNumber?.chunked(4)?.joinToString(" ") ?: ""
        textView.text = "$cardDescription $formattedCardNumber"
    }
}