package com.theone.busandbt.fragment.card

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.api.orderchannel.CardAPI
import com.theone.busandbt.databinding.FragmentCardNumberRegistrationBinding
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.extension.toCardType
import com.theone.busandbt.model.card.CardViewModel
import com.theone.busandbt.view.CustomKeyboard
import com.theone.busandbt.view.edittext.AsteriskPasswordTransformationMethod
import org.koin.android.ext.android.inject


class CardNumberRegistrationFragment :
    SecurityKeyboardFragment<FragmentCardNumberRegistrationBinding>() {

    companion object {
        private const val CARD_BIN_LENGTH = 8
        private const val CARD_NO_LENGTH = 16
    }

    override val layoutId: Int = R.layout.fragment_card_number_registration
    override val securityKeyboard: CustomKeyboard get() = binding.keyboard
    private lateinit var editTextList: List<EditText>
    private val viewModel: CardViewModel by activityViewModels()
    private val cardAPI: CardAPI by inject()
    private var isCorporateCard = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            initCardNoForm()
            initCustomKeyboard()

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                isCorporateCard = checkedId == corporationCard.id
                updateNextButton()
            }

            nextButton.setOnClickListener {
                val innerLoginInfo = loginInfo ?: return@setOnClickListener
                val firstCardNo =
                    cardEditTextInclude.firstCardNumberEditTextView.text.toString().trim()
                val secondCardNo =
                    cardEditTextInclude.secondCardNumberEditTextView.text.toString().trim()
                val binNo = firstCardNo + secondCardNo
                if (binNo.length != CARD_BIN_LENGTH) {
                    showMessageDialog("확인을 위한 카드번호는 앞자리 ${CARD_BIN_LENGTH}자리만 입력해야합니다.")
                    return@setOnClickListener
                }

                val thirdCardNo =
                    cardEditTextInclude.thirdCardNumberEditTextView.text.toString().trim()
                val fourthCardNo =
                    cardEditTextInclude.fourCardNumberEditTextView.text.toString().trim()
                val cardNo = binNo + thirdCardNo + fourthCardNo

                if (cardNo.length != CARD_NO_LENGTH) {
                    showMessageDialog("카드번호 ${CARD_NO_LENGTH}자리를 입력해주세요.")
                    return@setOnClickListener
                }
                safeApiRequest(
                    cardAPI.getCardType(binNo),
                    showFailMessage = true
                ) {
                    viewModel.updateCurrentPage(CardViewModel.CardRegistrationPage.DATE_REGISTRATION)
                    viewModel.setCardNo(cardNo)
                    viewModel.setIsCorpCard(isCorporateCard)
                    if (isCorporateCard) return@safeApiRequest
                    val dateString = innerLoginInfo.birth
                    val parts = dateString.split("-")
                    val year = parts[0].substring(2)
                    val month = parts[1]
                    val day = parts[2]
                    val resultString = year + month + day
                    viewModel.setBirth(resultString)
                }
            }

            rootView.setOnClickListener {
                hide()
            }
        }
    }

    private fun initCustomKeyboard() {
        with(binding) {
            with(cardEditTextInclude) {
                keyboard.associateEditTexts(
                    thirdCardNumberEditTextView,
                    fourCardNumberEditTextView
                )
            }
        }
    }

    private fun initCardNoForm() {
        with(binding.cardEditTextInclude) {
            editTextList = listOf(
                firstCardNumberEditTextView,
                secondCardNumberEditTextView,
                thirdCardNumberEditTextView,
                fourCardNumberEditTextView
            )
            thirdCardNumberEditTextView.transformationMethod =
                AsteriskPasswordTransformationMethod()
            fourCardNumberEditTextView.transformationMethod = AsteriskPasswordTransformationMethod()
            editTextList.forEachIndexed { index, editText ->
                initEditTextAfterChange(editText, index)
                if (index == 2 || index == 3) editText.showSoftInputOnFocus = false
                initEditTextFocusEvent(editText, index)
            }
        }
    }

    private fun initEditTextAfterChange(editView: EditText, index: Int) {
        editView.doAfterTextChanged { editable ->
            if (editable?.length == 4) {
                if (index < editTextList.lastIndex) editTextList[index + 1].requestFocus()
                else hide()
                // 두번째까지 입력완료하고 보안 키패드로 넘어갈때
                with(binding.cardEditTextInclude) {
                    if (editView == secondCardNumberEditTextView) {
                        val firstCardNo =
                            firstCardNumberEditTextView.text.toString().trim()
                        val secondCardNo =
                            secondCardNumberEditTextView.text.toString().trim()
                        val binNo = firstCardNo + secondCardNo
                        safeApiRequest(
                            cardAPI.getCardType(binNo),
                            onFail = { _, _ ->
                                viewModel.selectCardType(null)
                            }
                        ) {
                            viewModel.selectCardType(it.cardType.toCardType())
                        }
                    }
                }
            }
            updateNextButton()
        }
    }

    private fun initEditTextFocusEvent(editView: EditText, index: Int) {
        with(binding) {
            editView.setOnFocusChangeListener { _, hasFocus ->
                cardEditTextInclude.cardNumberBackGround.setBackgroundResource(if (hasFocus) R.drawable.bg_address_focus_edittext else R.drawable.bg_address_edittext_selector)
                if (index == 2 || index == 3) {
                    keyboard.isVisible = hasFocus
                    KeyboardUtils.hideSoftInput(requireActivity())
                    if (hasFocus) keyboard.rearrangeNumbers()
                } else {
                    scrollView.post {
                        scrollView.smoothScrollBy(
                            0,
                            editView.bottom - scrollView.height + SizeUtils.dp2px(50f)
                        )
                    }
                }
                nextButton.isVisible = !hasFocus
            }
        }
    }


    private fun updateNextButton() {
        with(binding) {
            val isRadioButtonChecked = radioGroup.checkedRadioButtonId != -1
            val isEditTextFilled = editTextList.all { editText ->
                editText.text.toString().length >= 4
            }
            nextButton.isEnabled = isRadioButtonChecked && isEditTextFilled
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

    override fun show() {
        with(binding) {
            securityKeyboard.isVisible = true
            nextButton.isVisible = false
        }
    }

    override fun hide() {
        with(binding) {
            securityKeyboard.isVisible = false
            nextButton.isVisible = true
            KeyboardUtils.hideSoftInput(requireActivity())
            editTextList.forEach { it.clearFocus() }
        }
    }
}