package com.theone.busandbt.fragment.my_info

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.StringUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentContactUsBinding
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.extension.showMessageBar
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.talk.TalkApiClient

/**
 * 문의하기 프레그먼트
 */
class ContactUsFragment : DataBindingFragment<FragmentContactUsBinding>(), EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_contact_us
    override val actionBarTitle: String = "문의하기"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            kakaoButton.setOnClickListener {
                try {
                    val url = TalkApiClient.instance.channelChatUrl(getString(R.string.kakaoOpenId))
                    KakaoCustomTabsClient.openWithDefault(requireContext(), url)
                } catch (t: Throwable) {
                    view.showMessageBar("카카오톡 앱 설치 후 다시 이용해주세요.")
                    t.printStackTrace()
                }
            }
            callButton.setOnClickListener {
                showMessageDialog(
                    "고객센터로 전화를 연결합니다.",
                    getString(R.string.contactUsDesc),
                    showCancelButton = true
                ) {
                    onDoneButtonClick("전화연결") {
                        PhoneUtils.dial(StringUtils.getString(R.string.callCenterNo))
                        dismiss()
                    }
                }
            }
        }
    }
}