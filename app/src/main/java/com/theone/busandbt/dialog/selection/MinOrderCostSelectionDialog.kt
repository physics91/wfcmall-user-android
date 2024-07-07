package com.theone.busandbt.dialog.selection

import com.theone.busandbt.dto.Selection
import com.theone.busandbt.type.MinOrderCostType

class MinOrderCostSelectionDialog : SelectionDialog<MinOrderCostType, MinOrderCostType>() {
    override val inputList: List<Selection<MinOrderCostType>> =
        MinOrderCostType.values().map { Selection(it.title, it) }
    override val titleText: String = "최소 주문 금액"
    override fun convertToResult(selectItem: Selection<MinOrderCostType>): MinOrderCostType =
        selectItem.data
}