package com.theone.busandbt.fragment.notice

import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.orderchannel.NoticeAPI
import com.theone.busandbt.databinding.FragmentNoticeListBinding
import com.theone.busandbt.dto.notice.NoticeListItem
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.adapter.notice.NoticeListAdapter
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 공지사항 목록 화면
 */
class NoticeListFragment :
    SingleListFragment<FragmentNoticeListBinding, NoticeListAdapter, NoticeListItem>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_notice_list
    override val actionBarTitle: String = "공지사항"
    override val recyclerView: RecyclerView get() = binding.noticeList
    private val noticeAPI: NoticeAPI by inject()

    override fun getCall(): Call<List<NoticeListItem>> = noticeAPI.getNoticeList(page, 15)

    override fun makeAdapter(): NoticeListAdapter = NoticeListAdapter()
}