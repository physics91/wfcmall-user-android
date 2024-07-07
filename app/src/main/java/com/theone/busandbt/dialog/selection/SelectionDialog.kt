package com.theone.busandbt.dialog.selection

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.R
import com.theone.busandbt.databinding.DialogSelectionBinding
import com.theone.busandbt.dialog.SingleDataSelectBottomDialog
import com.theone.busandbt.adapter.SelectionListAdapter
import com.theone.busandbt.dto.Selection

/**
 * 항목 중 하나만을 선택하는 팝업을 정의한다.
 * 선택됐을때 호출한 Fragment에서 선택한 데이터를 받을 수 있다.
 * TODO - 다양한 형식의 팝업으로도 가능하게 수정한다. 현재는 UI부터 고정적이라 다양한 형태로 구현 불가능
 */
abstract class SelectionDialog<Input, Output> :
    SingleDataSelectBottomDialog<DialogSelectionBinding, SelectionListAdapter<Input>, Selection<Input>, Output>() {
    protected abstract val inputList: List<Selection<Input>>
    protected abstract val titleText: String
    override val layoutId: Int = R.layout.dialog_selection
    override val recyclerView: RecyclerView get() = binding.selectionRecyclerView

    override fun makeAdapter(): SelectionListAdapter<Input> = SelectionListAdapter(inputList)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            title = titleText
            closeButton.setOnClickListener { dismiss() }
        }
    }

    /**
     * 기본 선택값을 변경한다.
     * 데이터만 변경된다.
     */
    fun setDefault(default: Input?) {
        inputList.forEach {
            if (it.data == default) {
                it.isSelected = true
                return@forEach
            }
            if (it.isSelected) it.isSelected = false
        }
    }
}