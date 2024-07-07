package com.theone.busandbt.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theone.busandbt.R
import com.theone.busandbt.databinding.DialogCategorySelectionBinding
import com.theone.busandbt.adapter.CategorySelectionListAdapter
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.model.category.CategoryViewModel
import com.theone.busandbt.view.recyclerview.decoration.VerticalSpaceItemDecoration
import org.koin.androidx.viewmodel.ext.android.activityViewModel


/**
 * 방문포장 메뉴선택 다이어로그
 * */
class CategorySelectionDialog(override val requestKey: String) :
    SingleDataSelectBottomDialog<DialogCategorySelectionBinding, CategorySelectionListAdapter, CategoryListItem, CategoryListItem>() {
    override val layoutId: Int = R.layout.dialog_category_selection
    override val recyclerView: RecyclerView get() = binding.menuList
    private val categoryViewModel: CategoryViewModel by activityViewModel()

    override fun makeAdapter(): CategorySelectionListAdapter = CategorySelectionListAdapter().apply {
        setItems(categoryViewModel.foodDeliveryCategoryList)
    }

    override fun convertToResult(selectItem: CategoryListItem): CategoryListItem = selectItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(SizeUtils.dp2px(9F)))
        with(binding) {
            exitBtn.setOnClickListener {
                dismiss()
            }
        }
    }

    //다이얼로그 크기만큼 나오기
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
        return dialog
    }
}