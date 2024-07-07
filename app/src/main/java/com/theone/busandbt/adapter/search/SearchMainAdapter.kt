package com.theone.busandbt.adapter.search

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemSearchMainBinding
import com.theone.busandbt.eventbus.DeleteSearchKeywordEvent
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.item.RecentSearchKeyword
import org.greenrobot.eventbus.EventBus

/**
 * 최근 검색어 어뎁터
 */
class SearchMainAdapter : DataBindingListAdapter<ItemSearchMainBinding, RecentSearchKeyword>() {
    override val viewHolderLayoutId: Int = R.layout.item_search_main

    override fun doBind(
        binding: ItemSearchMainBinding,
        item: RecentSearchKeyword,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            keyword = item
            deleteView.setOnClickListener {
                EventBus.getDefault().post(DeleteSearchKeywordEvent(item.keyword))
                deleteView.showMessageBar("최근 검색어가 정상적으로 삭제되었어요.")
            }
        }
    }
}