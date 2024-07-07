package com.theone.busandbt.fragment

import android.os.Bundle
import android.view.View
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentBusinessInfoBinding
import com.theone.busandbt.adapter.BusinessInfoListAdapter

/**
 * 사업자정보확인 화면
 */
class BusinessInfoFragment : DataBindingFragment<FragmentBusinessInfoBinding>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_business_info
    override val actionBarTitle: String = "사업자정보확인"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            businessInfoRecyclerView.adapter = BusinessInfoListAdapter()
        }
    }
}