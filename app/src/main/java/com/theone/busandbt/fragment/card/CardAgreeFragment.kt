package com.theone.busandbt.fragment.card

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentCardAgreeBinding
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.DataBindingFragment

/**
 * 사이다페이 약관 동의 화면
 */
class CardAgreeFragment : DataBindingFragment<FragmentCardAgreeBinding>(), View.OnClickListener,
    EnabledGoBackButton {
    private var userTriggered = true
    override val layoutId: Int = R.layout.fragment_card_agree
    override val actionBarTitle: String = EMPTY_ACTION_BAR_TITLE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            nextButton.setOnClickListener(this@CardAgreeFragment)
            registrationDetailButton.setOnClickListener(this@CardAgreeFragment)
            serviceDetailButton.setOnClickListener(this@CardAgreeFragment)
            privacyDetailButton.setOnClickListener(this@CardAgreeFragment)
            setupCheckboxListeners()
            checkNextButtonState()
        }
    }

    private fun FragmentCardAgreeBinding.setupCheckboxListeners() {
        val individualCheckboxes = listOf(registrationCheckbox, serviceCheckbox, privacyCheckbox)
        val associatedTextViews =
            listOf(registrationTextView, serviceTextView, privacyTitleTextView)

        allCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (userTriggered) {
                individualCheckboxes.forEach { it.isChecked = isChecked }
            }
        }

        individualCheckboxes.forEachIndexed { index, checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                userTriggered = false
                allCheckBox.isChecked = individualCheckboxes.all { it.isChecked }
                checkNextButtonState()
                associatedTextViews[index].setTextColor(
                    if (isChecked) ColorUtils.getColor(R.color.mainTextColor) else ColorUtils.getColor(
                        R.color.agreeDisableColor
                    )
                )
                userTriggered = true
            }
        }
    }

    private fun FragmentCardAgreeBinding.checkNextButtonState() {
        nextButton.isEnabled =
            listOf(registrationCheckbox, serviceCheckbox, privacyCheckbox).all { it.isChecked }
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view) {
                nextButton -> view.navigate(R.id.card_registration_graph)
                registrationDetailButton, serviceDetailButton, privacyDetailButton -> {
                    val url = when (view) {
                        registrationDetailButton -> "https://ciderpay.com/etc/remote/manualPayTerms"
                        serviceDetailButton -> "https://ciderpay.com/etc/remote/financialLaw"
                        else -> "https://ciderpay.com/etc/remote/privacy"
                    }
                    view.navigate(R.id.card_agree_web_graph, CardAgreeWebViewArgs(url).toBundle())
                }
            }
        }
    }
}
