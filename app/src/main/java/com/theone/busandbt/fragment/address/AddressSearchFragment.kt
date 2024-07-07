package com.theone.busandbt.fragment.address

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.outside.KakaoAPI
import com.theone.busandbt.databinding.FragmentAddressSearchBinding
import com.theone.busandbt.extension.getJibunDetail
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.adapter.search.SearchedAddressListAdapter
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.type.RegionType
import org.koin.android.ext.android.inject

/**
 * 주소검색 화면
 */
class AddressSearchFragment : DataBindingFragment<FragmentAddressSearchBinding>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_address_search
    override val actionBarTitle: String = "주소검색"
    private lateinit var searchedAddressListAdapter: SearchedAddressListAdapter
    private val kakaoAPI: KakaoAPI by inject()
    private val args by navArgs<AddressSearchFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            searchedAddressListAdapter = SearchedAddressListAdapter()
            addressRecyclerView.adapter = searchedAddressListAdapter
            changeClick(addressSearchEditText)
            addressSearchEditText.setOnFocusChangeListener { _, _ ->
                addressSearchEditText.setBackgroundResource(R.drawable.bg_address_edittext_selector)
                addressSearchEditText.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
                searchBtn.setImageResource(R.drawable.ic_search_main)
                addressSearchText.isVisible = false
            }
            searchedAddressListAdapter.setOnItemClick { _, _, item ->
                safeApiRequest(
                    kakaoAPI.getSearchRegion(item.lng, item.lat)
                ) { response ->
                    val admin =
                        response.documents.find { document -> document.regionType == RegionType.ADMIN.code }
                    val action =
                        AddressSearchFragmentDirections.actionAddressSearchFragmentToAddressDetailFragment(
                            address = if (admin != null) item.copy(jibun = "${admin.addressName} ${item.jibun.getJibunDetail()}".trimEnd()) else item,
                            addressName = args.addressName
                        )
                    findNavController().navigate(action)
                }
            }

            searchBtn.setOnClickListener {
                if (addressSearchEditText.length() == 0) return@setOnClickListener
                searchKeyword(addressSearchEditText.text.toString())
            }
            addressSearchEditText.requestFocus()
            KeyboardUtils.showSoftInput(addressSearchEditText)
        }
    }

    //검색결과가 없을 때
    private fun ifEmptySearchResult() {
        with(binding) {
            addressRecyclerView.isVisible = false
            normalView.root.isVisible = false
            noAddress.root.isVisible = true
        }
    }

    //검색결과가 있을 때
    private fun ifNotEmptySearchResult() {
        with(binding) {
            normalView.root.isVisible = false
            addressRecyclerView.isVisible = true
            noAddress.root.isVisible = false
        }
    }

    // 키워드 검색 함수
    private fun searchKeyword(keyword: String) {
        safeApiRequest(
            kakaoAPI.searchAddress(keyword, 1, 20)
        ) { address ->
            safeApiRequest(
                kakaoAPI.searchPlace(keyword, 1, 15)
            ) { place ->
                val total = address.toAddressSearchResultList() + place.toAddressSearchResultList()
                searchedAddressListAdapter.setItems(total)
                if (total.isEmpty()) {
                    ifEmptySearchResult()
                } else {
                    ifNotEmptySearchResult()
                }
                KeyboardUtils.hideSoftInput(binding.addressSearchEditText)
            }
        }
    }

    //text크기가 0이 아닐때 폰트와 자간 커서위치 재설정
    private fun changeClick(place: EditText) {
        place.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(frontNum: Editable?) {
                binding.searchBtn.isClickable = place.text.isNotEmpty()
            }
        })
    }
}