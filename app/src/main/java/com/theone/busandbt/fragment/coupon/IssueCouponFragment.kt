package com.theone.busandbt.fragment.coupon

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.api.orderchannel.CouponAPI
import com.theone.busandbt.databinding.FragmentIssueCouponBinding
import com.theone.busandbt.dto.coupon.request.IssueCouponByCodeRequest
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.fragment.DataBindingFragment
import org.koin.android.ext.android.inject

/**
 * 쿠폰등록화면
 */
class IssueCouponFragment : DataBindingFragment<FragmentIssueCouponBinding>() {
    override val layoutId: Int = R.layout.fragment_issue_coupon
    override val actionBarTitle: String = "쿠폰 등록"
    private val couponAPI: CouponAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            couponCodeIssueButton.setOnClickListener {
                val innerLoginInfo = loginInfo ?: return@setOnClickListener
                val code = couponCodeEditText.text.toString()
                if (code.isEmpty()) return@setOnClickListener
                safeApiRequest(
                    couponAPI.issueCouponByCode(
                        innerLoginInfo.getFormedToken(),
                        IssueCouponByCodeRequest(innerLoginInfo.id, code)
                    ),
                    showFailMessage = true
                ) {
                    showMessageDialog("쿠폰 등록이 완료되었습니다.") {
                        onDoneButtonClick {
                            findNavController().popBackStack()
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}