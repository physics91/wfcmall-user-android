package com.theone.busandbt.fragment.address

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.theone.busandbt.R
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentAddressEditBinding
import com.theone.busandbt.adapter.address.DeliveryAddressEditListAdapter

/**
 * 주소 편집(수정,삭제) 하는 화면
 */
class AddressEditFragment : DataBindingFragment<FragmentAddressEditBinding>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_address_edit
    override val actionBarTitle: String = "주소 편집"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addressAdapter = DeliveryAddressEditListAdapter(deliveryAddressViewModel, loginInfoViewModel)
        binding.addressListRecyclerView.adapter = addressAdapter
        deliveryAddressViewModel.observeAddressList(this) {
            Log.d("결과",it.toString())
            if(it.isNotEmpty()){
                addressAdapter.setItems(it)
            } else {
                binding.noResultInclude.root.isVisible = true
            }
        }
    }
}