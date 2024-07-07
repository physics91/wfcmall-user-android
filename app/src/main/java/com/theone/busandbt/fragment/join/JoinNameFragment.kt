package com.theone.busandbt.fragment.join

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentJoinNameBinding
import com.theone.busandbt.utils.NAME_INPUT_FILTERS

/**
 * 이름 입력하는 화면
 */
class JoinNameFragment : BaseJoinFragment<FragmentJoinNameBinding>(), EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_join_name
    override val actionBarTitle: String = EMPTY_ACTION_BAR_TITLE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            nameEditText.filters = NAME_INPUT_FILTERS

            //버튼 활성화 조건 확인
            buttonConverter(nameEditText)
            nextButton.setOnClickListener {
                joinInfoViewModel.setNickname(nameEditText.text.toString())
                findNavController().navigate(R.id.action_joinNameFragment_to_joinPasswordFragment)
            }
        }
    }

    //버튼 활성화 조건 확인 함수
    private fun buttonConverter(place: EditText) {
        with(binding) {
            place.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    val flag = nameEditText.editableText.length >= 2
                    nextButton.isEnabled = flag
                    nameStatusTextView.isVisible = !flag
                }
            })
        }
    }
}