package com.theone.busandbt.fragment.address

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentHaveAddressSetBinding
import com.theone.busandbt.dto.address.DeliveryAddress
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.adapter.address.DeliveryAddressListAdapter

/**
 * 주소 설정 했을 때 주소가 있다면 뜨는 주소 설정 창
 */
class DeliveryAddressListFragment : DataBindingFragment<FragmentHaveAddressSetBinding>(),
    EnabledGoBackButton, View.OnClickListener {
    override val layoutId: Int = R.layout.fragment_have_address_set
    override val actionBarTitle: String = "주소설정"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            checkHintOverlap()
            //어뎁터 초기화
            val addressAdapter = DeliveryAddressListAdapter()
            addressRecyclerView.adapter = addressAdapter
            deliveryAddressViewModel.observeAddressList(this@DeliveryAddressListFragment) {
                val result = ArrayList(it)
                if (it.find { a -> a.name == "회사" } == null) result.add(
                    0,
                    DeliveryAddress.createEmpty("회사")
                )
                if (it.find { a -> a.name == "집" } == null) result.add(
                    0,
                    DeliveryAddress.createEmpty("집")
                )
                addressAdapter.setItems(result)
            }

            addressAdapter.setOnItemClick { _, _, item ->
                if (item.jibun.isEmpty() && item.road.isEmpty()) return@setOnItemClick // 주소 데이터가 아닌 집, 회사같은 기본형 데이터일 경우
                deliveryAddressViewModel.toggle(item, true)
                findNavController().popBackStack()
            }

            addressFindButton.setOnClickListener(this@DeliveryAddressListFragment)
            editAddressTextView.setOnClickListener(this@DeliveryAddressListFragment)
            addressSearchButton.setOnClickListener(this@DeliveryAddressListFragment)
        }
    }

    /**
     * 힌트 메시지와 이미지가 곂치는지 여부 확인
     * 곂칠 시 힌트 텍스트를 줄이도록 한다.
     */
    private fun checkHintOverlap() {
        with(binding) {
            addressSearchText.viewTreeObserver.addOnGlobalLayoutListener {
                val addressSearchTextRect = Rect()
                addressSearchText.getGlobalVisibleRect(addressSearchTextRect)

                val searchButtonViewRect = Rect()
                searchImageView.getGlobalVisibleRect(searchButtonViewRect)

                val isSearchImageViewOverlappingAddressSearchText =
                    addressSearchTextRect.intersect(searchButtonViewRect)

                if (isSearchImageViewOverlappingAddressSearchText) {
                    addressSearchText.hint = "건물명,도로명 또는 지번으로 검색..."
                }
            }
        }
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view) {
                addressFindButton -> {
                    findNavController().navigate(R.id.action_haveAddressSetFragment_to_addressFineFragment)
                }

                editAddressTextView -> {
                    findNavController().navigate(R.id.action_haveAddressSetFragment_to_addressSetEditFragment)
                }

                addressSearchButton -> {
                    val action =
                        DeliveryAddressListFragmentDirections.actionHaveAddressSetFragmentToAddressSearchFragment(
                            ""
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }
}