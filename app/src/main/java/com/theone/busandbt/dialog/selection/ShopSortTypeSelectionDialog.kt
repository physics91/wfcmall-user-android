package com.theone.busandbt.dialog.selection

import com.busandbt.code.ShopSortType
import com.theone.busandbt.dto.Selection

class ShopSortTypeSelectionDialog(
    override val inputList: List<Selection<ShopSortType>>
) : SelectionDialog<ShopSortType, ShopSortType>() {
    override val titleText: String = "정렬"
    override fun convertToResult(selectItem: Selection<ShopSortType>): ShopSortType =
        selectItem.data
}