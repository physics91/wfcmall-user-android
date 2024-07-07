package com.theone.busandbt.dialog.selection

import com.busandbt.code.LikeSortType
import com.theone.busandbt.dto.Selection

class LikeSortTypeSelectionDialog(override val inputList: List<Selection<LikeSortType>>) :
    SelectionDialog<LikeSortType, LikeSortType>() {
    override val titleText: String = "정렬"
    override fun convertToResult(selectItem: Selection<LikeSortType>): LikeSortType =
        selectItem.data
}