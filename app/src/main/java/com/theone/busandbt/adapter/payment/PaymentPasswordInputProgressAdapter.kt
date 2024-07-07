package com.theone.busandbt.adapter.payment

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemPasswordInputProgressBinding
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.utils.CARD_SIMPLE_PASSWORD_LENGTH

class PaymentPasswordInputProgressAdapter :
    DataBindingListAdapter<ItemPasswordInputProgressBinding, Int?>() {

    init {
        repeat(CARD_SIMPLE_PASSWORD_LENGTH) {
            _items.add(null)
        }
    }

    override val viewHolderLayoutId: Int = R.layout.item_password_input_progress

    override fun doBind(
        binding: ItemPasswordInputProgressBinding,
        item: Int?,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            root.isSelected = item != null
        }
    }

    /**
     * @return true일 경우 삭제가 됐다는 의미, false인 경우에는 삭제할 숫자가 더이상 존재하지 않다는 의미
     */
    fun removeLastNumber(): Boolean {
        val i = items.indexOfLast { it != null }
        if (i == -1) return false
        _items[i] = null
        notifyItemChanged(i)
        return true
    }

    /**
     * @return 입력한 숫자가 반영된 위치를 반환한다. -1일 경우 반영이 안됐다는 의미
     */
    fun addNumber(number: Int): Int {
        val i = items.indexOfLast { it != null }
        if (i == CARD_SIMPLE_PASSWORD_LENGTH - 1) return -1
        val changedIndex = if (i == -1) 0 else i + 1
        _items[changedIndex] = number
        notifyItemChanged(changedIndex)
        return changedIndex
    }

    fun getAllNumberText(): String = items.filterNotNull().joinToString("")
}