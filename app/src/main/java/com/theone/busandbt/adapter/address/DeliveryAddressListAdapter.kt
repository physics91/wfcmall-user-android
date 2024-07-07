package com.theone.busandbt.adapter.address

import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemAddressSetBinding
import com.theone.busandbt.dto.address.DeliveryAddress
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.fragment.address.DeliveryAddressListFragmentDirections

/**
 * 주소 설정 등록한 아이템 리사이클러뷰 어댑터
 */
class DeliveryAddressListAdapter :
    DataBindingListAdapter<ItemAddressSetBinding, DeliveryAddress>() {
    override val viewHolderLayoutId: Int = R.layout.item_address_set

    override fun doBind(
        binding: ItemAddressSetBinding,
        item: DeliveryAddress,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            addressCheckImg.isVisible = item.enabled
            addressCheckTextView.isVisible = item.enabled

            when (item.name) {
                "집", "회사" -> {
                    if (item.jibun.isEmpty() && item.road.isEmpty()) {
                        addressContentTextView.isVisible = false
                        addAddressButton.isVisible = true
                        addAddressButton.setOnClickListener {
                            val action =
                                DeliveryAddressListFragmentDirections.actionHaveAddressSetFragmentToAddressSearchFragment(item.name)
                            it.findNavController().navigate(action)
                        }
                    }
                }
            }
            when (item.name) {
                "집" -> addressMarker.setImageResource(R.drawable.ic_home)
                "회사" -> addressMarker.setImageResource(R.drawable.ic_company)
                else -> addressMarker.setImageResource(R.drawable.ic_black_ping)
            }
            address = item
        }
    }
}