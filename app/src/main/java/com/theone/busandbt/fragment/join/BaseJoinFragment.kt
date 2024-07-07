package com.theone.busandbt.fragment.join

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.blankj.utilcode.util.KeyboardUtils
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.model.JoinInfoViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

abstract class BaseJoinFragment<VDB : ViewDataBinding> : DataBindingFragment<VDB>() {

    protected val joinInfoViewModel: JoinInfoViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            root.setOnClickListener {
                KeyboardUtils.hideSoftInput(requireActivity())
            }
        }
    }
}