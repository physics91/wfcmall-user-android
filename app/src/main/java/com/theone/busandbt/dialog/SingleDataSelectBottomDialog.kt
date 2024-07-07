package com.theone.busandbt.dialog

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.adapter.DataBindingListAdapter

abstract class SingleDataSelectBottomDialog<VDB : ViewDataBinding, Adapter : DataBindingListAdapter<*, Item>, Item, Output> :
    DataBindingBottomDialog<VDB>() {

    protected open val requestKey: String by lazy { "${hashCode()}" }

    /**
     * 데이터 리스트가 표시될 [RecyclerView]를 정의한다.
     */
    protected abstract val recyclerView: RecyclerView

    /**
     * 데이터 리스트가 관리될 [DataBindingListAdapter]를 정의한다.
     */
    protected lateinit var recyclerViewAdapter: Adapter

    /**
     * 선택된 항목을 반환할 데이터로 변경하는 함수
     */
    abstract fun convertToResult(selectItem: Item): Output

    abstract fun makeAdapter(): Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewAdapter = makeAdapter()
        recyclerView.adapter = recyclerViewAdapter
        recyclerViewAdapter.setOnItemClick { _, _, item ->
            parentFragmentManager.setFragmentResult(
                requestKey,
                bundleOf("item" to convertToResult(item))
            )
            dismiss()
        }
    }

    /**
     * 해당 Fragment에 팝업의 데이터를 받는 부분을 등록한다.
     * 해당 기능은 이걸 사용하지 말고 확장함수인 setOnReceiveData를 사용하도록 한다.
     */
    fun onReceiveData(fragment: Fragment, returnClass: Class<Output>, op: (Output?) -> Unit) {
        fragment.childFragmentManager.setFragmentResultListener(
            requestKey,
            fragment
        ) { requestKey, result ->
            if (this.requestKey != requestKey) return@setFragmentResultListener
            op(returnClass.cast(result.get("item")))
        }
    }

    fun recycle(fragment: Fragment) {
        fragment.childFragmentManager.clearFragmentResult(requestKey)
    }
}