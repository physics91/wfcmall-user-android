package com.theone.busandbt.adapter.card

import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemCardCategoryBinding
import com.theone.busandbt.dto.card.CardInfoListItem
import com.theone.busandbt.eventbus.RemoveCardEvent
import org.greenrobot.eventbus.EventBus

/**
 * 임시로 카드 아이템을 넣어두었음 수정해야함
 */
class CardListAdapter :
    DataBindingListAdapter<ItemCardCategoryBinding, CardInfoListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_card_category

    override fun doBind(
        binding: ItemCardCategoryBinding,
        item: CardInfoListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            cardItem = item
            cardImageView.clipToOutline = true
            deleteButton.setOnClickListener {
                EventBus.getDefault().post(RemoveCardEvent(item))
            }
        }
    }
}