package com.theone.busandbt.dialog

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.setFragmentResult
import com.blankj.utilcode.util.ColorUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.DialogShoppingFoodBinding
import com.theone.busandbt.spanned.TypefaceSpanCompat
import com.busandbt.code.ShopSortType

class ShoppingFoodSelectionDialog(private val defaultSortText: CharSequence) :
    DataBindingBottomDialog<DialogShoppingFoodBinding>(),
    View.OnClickListener {
    override val layoutId: Int = R.layout.dialog_shopping_food

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            //선택된 항목 표시용 조건문
            when (defaultSortText) {
                firstOption.text -> sortChange(
                    "가까운 순",
                    firstOption,
                    firstCheck,
                    false
                )
                secondOption.text -> sortChange(
                    "배달 빠른 순",
                    secondOption,
                    secondCheck,
                    false
                )
                thirdOption.text -> sortChange(
                    "배달비 낮은 순",
                    thirdOption,
                    thirdCheck,
                    false
                )
                fourthOption.text -> sortChange(
                    "주문 많은 순",
                    fourthOption,
                    fourthCheck,
                    false
                )
                fiveOption.text -> sortChange(
                    "별점 높은 순",
                    fiveOption,
                    fiveCheck,
                    false
                )
            }
            firstOption.setOnClickListener(this@ShoppingFoodSelectionDialog)
            secondOption.setOnClickListener(this@ShoppingFoodSelectionDialog)
            thirdOption.setOnClickListener(this@ShoppingFoodSelectionDialog)
            fourthOption.setOnClickListener(this@ShoppingFoodSelectionDialog)
            fiveOption.setOnClickListener(this@ShoppingFoodSelectionDialog)
            exit.setOnClickListener(this@ShoppingFoodSelectionDialog)
        }
    }

    private fun sortChange(sortText: String, sort: TextView, sortImg: View, clicked: Boolean) {
        sort.setBackgroundResource(R.drawable.bg_like_sort_dialog)
        //이전에 선택된 색상 제거
        removeColorSelect()
        val sbb = SpannableStringBuilder(sort.text.toString())
        sortImg.visibility = View.VISIBLE
        sort.text = buildSpannedString with@{
            inSpans(
                TypefaceSpanCompat(
                    ResourcesCompat.getFont(requireContext(), R.font.sult_bold)
                        ?: return@with
                ),
                ForegroundColorSpan(ColorUtils.getColor(R.color.mainColor))
            ) {
                append(sortText)
            }
        }
        //key를 만들어 formation에 보내줌
        //통신사를 클릭하는 것이 아닐시 종료
        if (!clicked) return
        setFragmentResult("sort", bundleOf("sortText" to sort.text.toString(), "shopSortType" to when(sortText) {
            "최신 등록 순" -> ShopSortType.NEAR
          //  "주문 많은 순" -> ShopSortType.FAST_DELIVERY
            "리뷰 많은 순" -> ShopSortType.LOWER_DELIVERY_COST
            "가격 높은 순" -> ShopSortType.MANY_ORDER
            "가격 낮은 순" -> ShopSortType.HIGHER_STAR
            else -> ShopSortType.NEAR
        }.id)
        )
        //클릭 버튼 이벤트가 일어난 후 0.2후 팝업창이 닫힘
        Handler().postDelayed({
            dismiss()
        }, 200)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.firstOption -> sortChange("최신 등록 순", binding.firstOption, binding.firstCheck, true)
            R.id.secondOption -> sortChange(
                "주문 많은 순",
                binding.secondOption,
                binding.secondCheck,
                true
            )
            R.id.thirdOption -> sortChange(
                "리뷰 많은 순",
                binding.thirdOption,
                binding.thirdCheck,
                true
            )
            R.id.fourthOption -> sortChange(
                "가격 높은 순",
                binding.fourthOption,
                binding.fourthCheck,
                true
            )
            R.id.fiveOption -> sortChange("가격 낮은 순", binding.fiveOption, binding.fiveCheck, true)
            R.id.exitBtn -> dismiss()
        }
    }

    //글자색이 주황색인 텍스트뷰를 탐지 및 삭제 함수
    private fun removeColorSelect() {
        when {
            (binding.firstCheck.visibility == View.VISIBLE) -> removeColor(
                binding.firstOption,
                binding.firstCheck
            )
            (binding.secondCheck.visibility == View.VISIBLE) -> removeColor(
                binding.secondOption,
                binding.secondCheck
            )
            (binding.thirdCheck.visibility == View.VISIBLE) -> removeColor(
                binding.thirdOption,
                binding.thirdCheck
            )
            (binding.fourthCheck.visibility == View.VISIBLE) -> removeColor(
                binding.fourthOption,
                binding.fourthCheck
            )
            (binding.fiveCheck.visibility == View.VISIBLE) -> removeColor(
                binding.fiveOption,
                binding.fiveCheck
            )
        }
    }

    //배경색&글자색 제거 및 체크 해제 함수
    private fun removeColor(phone: TextView, img: View) {
        phone.setBackgroundResource(0)
        img.visibility = View.INVISIBLE
        val sbb = SpannableStringBuilder(phone.text.toString())
        sbb.setSpan(
            ForegroundColorSpan(Color.parseColor("#111111")),
            0,
            sbb.lastIndex + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        phone.text = sbb
    }
}