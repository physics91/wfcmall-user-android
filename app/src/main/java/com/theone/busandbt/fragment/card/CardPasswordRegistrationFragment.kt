package com.theone.busandbt.fragment.card

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.KeyboardUtils
import com.theone.busandbt.R
import com.theone.busandbt.api.orderchannel.CardAPI
import com.theone.busandbt.databinding.FragmentCardPasswordRegistrationBinding
import com.theone.busandbt.dto.card.request.AddCardInfoRequest
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.model.card.CardViewModel
import com.theone.busandbt.view.CustomKeyboard
import com.theone.busandbt.view.edittext.AsteriskPasswordTransformationMethod
import org.koin.android.ext.android.inject


class CardPasswordRegistrationFragment :
    SecurityKeyboardFragment<FragmentCardPasswordRegistrationBinding>() {
    override val layoutId: Int = R.layout.fragment_card_password_registration
    override val securityKeyboard: CustomKeyboard get() = binding.keyboard
    private lateinit var editTextList: List<EditText>
    private val viewModel: CardViewModel by activityViewModels()
    private val cardAPI: CardAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            initEditTextList()
            initEditSetting()
            initCustomKeyboard()
            rootView.setOnClickListener {
                hide()
            }
            nextButton.setOnClickListener {
                val innerLoginInfo = loginInfo ?: return@setOnClickListener
                if (viewModel.isCorpCard.value == false) viewModel.setPassword(
                    cardFrontNumberEditTextView.text.toString()
                )
                viewModel.setSimplePassword(cardSimpleNumberEditTextView.text.toString())
                viewModel.setMemberId(innerLoginInfo.id)
                safeApiRequest(
                    cardAPI.cardRegistration(
                        innerLoginInfo.getFormedToken(), AddCardInfoRequest(
                            viewModel.memberId.value!!,
                            viewModel.cardNo.value!!,
                            viewModel.expireYearMonth.value!!,
                            viewModel.password.value,
                            viewModel.birth.value,
                            viewModel.simplePassword.value!!,
                            viewModel.isCorpCard.value!!,
                        )
                    ),
                    onFail = { _, _ ->
                        showMessageDialog(
                            "카드 등록에 실패하였어요.",
                            "다시 한 번 시도해 주세요.",
                            showWarningImageView = true,
                            showCancelButton = false
                        ) { onDoneButtonClick { dismiss() } }
                    }
                ) {
                    view.showMessageBar("카드가 정상적으로 등록되었어요.")
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun interceptOnBackButtonClicked(): Boolean {
        val result = super.interceptOnBackButtonClicked()
        if (result && !securityKeyboard.isVisible) {
            debugLog("패스워드에서 뒤로", "ㅇㅇ")
            viewModel.updateCurrentPage(CardViewModel.CardRegistrationPage.DATE_REGISTRATION)
            return false
        }
        return result
    }

    private fun initEditSetting() {
        editTextList.forEachIndexed { index, editText ->
            initEditTextFocusEvent(editText, index)
            initEditTextAfterChange(editText, index)
            editText.showSoftInputOnFocus = false
        }
    }

    private fun initEditTextList() {
        with(binding) {
            editTextList = listOf(
                cardFrontNumberEditTextView,
                cardSimpleNumberEditTextView,
                cardReEnterEditTextView
            )
            editTextList.forEach {
                it.transformationMethod = AsteriskPasswordTransformationMethod()
            }
        }
    }

    private fun initCustomKeyboard() {
        with(binding) {
            securityKeyboard.associateEditTexts(
                cardFrontNumberEditTextView,
                cardSimpleNumberEditTextView,
                cardReEnterEditTextView
            )
        }
    }

    private fun initEditTextFocusEvent(editView: EditText, index: Int) {
        with(binding) {
            editView.setOnFocusChangeListener { _, hasFocus ->
                editView.setBackgroundResource(if (hasFocus) R.drawable.bg_address_focus_edittext else R.drawable.bg_address_edittext_selector)
                keyboard.isVisible = hasFocus
                nextButton.isVisible = !hasFocus
                if (hasFocus) {
                    keyboard.rearrangeNumbers()
                    when (index) {
                        1 -> keyboardPasswordSpace.isVisible = true
                        2 -> keyboardSpace.isVisible = true
                        else -> {
                            keyboardPasswordSpace.isVisible = false
                            keyboardSpace.isVisible = false
                        }
                    }
                    if (editView == cardReEnterEditTextView) {
                        val parentFragmentView = parentFragment?.view
                        val scrollView = parentFragmentView?.findViewById<NestedScrollView>(R.id.scrollView)

                        scrollView?.post {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                        }
                    }
                }
            }
        }
    }

    private fun initEditTextAfterChange(editView: EditText, index: Int) {
        editView.doAfterTextChanged { editable ->
            val targetLength = if (index == 1 || index == 2) 6 else 2
            if (editable?.length == targetLength) {
                if (index < editTextList.lastIndex) editTextList[index + 1].requestFocus()
                else hide()
            }
            updateNextButton()
        }
    }

    private fun updateNextButton() {
        with(binding) {
            val isEditTextFilled = cardFrontNumberEditTextView.text.toString().length >= 2

            val isSimplePasswordEditTextFilled = listOf(
                cardSimpleNumberEditTextView,
                cardReEnterEditTextView
            ).all { editText ->
                editText.text.toString().length >= 6
            }

            val isTextMatching =
                cardSimpleNumberEditTextView.text.toString() == cardReEnterEditTextView.text.toString()

            if (cardReEnterEditTextView.text.toString()
                    .isNotEmpty()
            ) matchStatusTextView.isVisible = !isTextMatching
            nextButton.isEnabled =
                isEditTextFilled && isSimplePasswordEditTextFilled && isTextMatching
        }
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

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }
}