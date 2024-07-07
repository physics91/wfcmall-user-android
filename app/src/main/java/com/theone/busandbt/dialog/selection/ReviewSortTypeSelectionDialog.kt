package com.theone.busandbt.dialog.selection

import com.busandbt.code.ReviewSortType
import com.theone.busandbt.dto.Selection
import com.theone.busandbt.extension.desc

class ReviewSortTypeSelectionDialog: SelectionDialog<ReviewSortType, ReviewSortType>() {
    override val inputList: List<Selection<ReviewSortType>> = ReviewSortType.values().map { Selection(it.desc(), it) }
    override val titleText: String = "정렬"

    override fun convertToResult(selectItem: Selection<ReviewSortType>): ReviewSortType = selectItem.data
}