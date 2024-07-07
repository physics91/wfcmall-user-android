package com.theone.busandbt.adapter.notice

import androidx.navigation.findNavController
import com.blankj.utilcode.util.ColorUtils
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemNoticeListBinding
import com.theone.busandbt.dto.notice.NoticeListItem
import com.theone.busandbt.fragment.notice.NoticeListFragmentDirections
import com.theone.busandbt.utils.UnitConverter

/**
 * 공지사항 어뎁터입니다
 * 공지사항 RecyclerView 아이템 연결에 사용 중
 */
class NoticeListAdapter : DataBindingListAdapter<ItemNoticeListBinding, NoticeListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_notice_list

    override fun doBind(
        binding: ItemNoticeListBinding,
        item: NoticeListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            if (item.fixed) {
                noticeRoot.setBackgroundColor(ColorUtils.getColor(R.color.noticeForm))
                noticeTitle.setTextColor(ColorUtils.getColor(R.color.mainColor))
                noticeTitle.setPadding(0, UnitConverter.dpToPx(6), 0, 0)
                noticeTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_blue_speaker,
                    0,
                    0,
                    0
                )
            }
            notice = item
            noticeDate.text = item.createDateTime
            setOnItemClick { view, _, item ->
                val action =
                    NoticeListFragmentDirections.actionNoticeListFragmentToNoticeContentFragment(
                        item.id
                    )
                view.findNavController().navigate(action)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = position
}
