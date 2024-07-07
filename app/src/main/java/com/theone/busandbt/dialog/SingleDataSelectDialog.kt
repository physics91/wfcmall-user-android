package com.theone.busandbt.dialog

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.adapter.DataBindingListAdapter

abstract class SingleDataSelectDialog<VDB : ViewDataBinding, Adapter : DataBindingListAdapter<*, Item>, Item> : DataBindingDialog<VDB>() {

    /**
     * 데이터 리스트가 표시될 [RecyclerView]를 정의한다.
     */
    protected abstract val recyclerView: RecyclerView

    /**
     * 데이터 리스트가 관리될 [DataBindingListAdapter]를 정의한다.
     */
    protected abstract val recyclerViewAdapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerViewAdapter.setOnItemClick { _, _, item ->
            setFragmentResult("", bundleOf("item" to item))
        }
    }
}