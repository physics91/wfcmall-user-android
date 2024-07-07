package com.theone.busandbt.dialog.selection

import com.busandbt.code.MenuSortType
import com.theone.busandbt.dto.Selection

class MenuSortTypeSelectionDialog(
    override val inputList: List<Selection<MenuSortType>>
) : SelectionDialog<MenuSortType, MenuSortType>() {
    override val titleText: String = "정렬"
    override fun convertToResult(selectItem: Selection<MenuSortType>): MenuSortType =
        selectItem.data
}