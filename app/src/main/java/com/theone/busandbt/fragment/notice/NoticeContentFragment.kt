package com.theone.busandbt.fragment.notice

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.orderchannel.NoticeAPI
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.FragmentNoticeContentBinding
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.DataBindingFragment
import org.koin.android.ext.android.inject

/**
 * 공지사항 상세화면
 * 공지사항의 상세정보를 입력합니다.
 */
class NoticeContentFragment : DataBindingFragment<FragmentNoticeContentBinding>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_notice_content
    override val actionBarTitle: String = "공지사항"

    private val args by navArgs<NoticeContentFragmentArgs>()
    private val noticeApi: NoticeAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNoticeContent()
    }

    /**
     * 공지 상세 내용을 불러온다.
     */
    private fun initNoticeContent() {
        safeApiRequest(
            noticeApi.getNoticeDetail(args.noticeId)
        ) {
            with(binding) {
                noticeTitle.text = it.title
                noticeContent.text = it.content
                CommonBindingAdapter.setDate(noticeDate,it.createDateTime)
            }
        }
    }
}

