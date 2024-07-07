package com.theone.busandbt.fragment.card

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentCardDateRegistrationBinding
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.listener.BackButtonInterceptor
import com.theone.busandbt.model.card.CardViewModel

class CardDateRegistrationFragment : DataBindingFragment<FragmentCardDateRegistrationBinding>(), BackButtonInterceptor {
    override val layoutId: Int = R.layout.fragment_card_date_registration
    private val viewModel: CardViewModel by activityViewModels()
    private lateinit var editTextList: List<EditText>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            initializeEditTextList()
            initEditSetting()
            nextButton.setOnClickListener {
                val date = editTextList.reversed().joinToString("") { it.text.toString() }
                viewModel.setExpire(date)
                viewModel.updateCurrentPage(CardViewModel.CardRegistrationPage.PASSWORD_REGISTRATION)
            }
            rootView.setOnClickListener {
                editTextList.forEach { it.clearFocus() }
                KeyboardUtils.hideSoftInput(requireActivity())
            }
            monthEditTextView.post {
                monthEditTextView.requestFocus()
            }
        }
    }

    override fun interceptOnBackButtonClicked(): Boolean {
        debugLog("설마", "여기에오나")
        viewModel.updateCurrentPage(CardViewModel.CardRegistrationPage.NUMBER_REGISTRATION)
        return false
    }

    private fun initializeEditTextList() {
        with(binding) {
            editTextList = listOf(
                monthEditTextView,
                yearEditTextView,
            )
        }
    }

    private fun initEditTextAfterChange(editView: EditText, index: Int) {
        editView.doAfterTextChanged { editable ->
            if (editable?.length == 2 && index < editTextList.lastIndex) {
                editTextList[index + 1].requestFocus()
            }
            updateNextButton()
        }
    }

    private fun initEditSetting() {
        editTextList.forEachIndexed { index, editText ->
            initEditTextFocusEvent(editText)
            initEditTextAfterChange(editText, index)
        }
    }

    private fun initEditTextFocusEvent(editView: EditText) {
        with(binding) {
            editView.setOnFocusChangeListener { _, hasFocus ->
                dateBackground.setBackgroundResource(if (hasFocus) R.drawable.bg_address_focus_edittext else R.drawable.bg_address_edittext_selector)
            }
        }
    }

    private fun updateNextButton() {
        with(binding) {
            val isEditTextFilled = editTextList.all { editText ->
                editText.text.toString().length >= 2
            }
            nextButton.isEnabled = isEditTextFilled
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }
}