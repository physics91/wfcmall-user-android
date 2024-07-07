package com.theone.busandbt.fragment.search

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.KeyboardUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.databinding.FragmentSearchShopDetailMainBinding
import com.theone.busandbt.eventbus.DeleteSearchKeywordEvent
import com.theone.busandbt.extension.errorLog
import com.theone.busandbt.extension.getSoftInputMode
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.item.RecentSearchKeyword
import com.theone.busandbt.model.RecentSearchKeywordViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 상점 상세에서 검색했을 때 뜨는 화면
 * TODO: 키보드가 나타났을 때 키보드 위에 이미지뷰를 위치해야함
 */
class SearchShopDetailMainFragment : DataBindingFragment<FragmentSearchShopDetailMainBinding>(),
    EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_search_shop_detail_main
    private lateinit var recentSearchKeywordViewModel: RecentSearchKeywordViewModel
    private val args by navArgs<SearchShopDetailMainFragmentArgs>()
    private var originalMode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalMode = activity?.window?.getSoftInputMode()
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        originalMode?.let { activity?.window?.setSoftInputMode(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            searchFocusStatus()
            recentSearchKeywordViewModel = ViewModelProvider(
                requireActivity(),
                RecentSearchKeywordViewModel.Factory(requireActivity().application)
            )[RecentSearchKeywordViewModel::class.java]

            goBackButton.setOnClickListener {
                it.findNavController().popBackStack()
            }

            searchImg.setOnClickListener {
                val keyword = mainSearch.text.toString()
                if (keyword.isEmpty()) return@setOnClickListener
                it.navigate(
                    R.id.search_shop_detail_result_graph,
                    SearchShopDetailResultFragmentArgs(
                        keyword,
                        args.shopId,
                        args.shopName,
                        args.minOrderCost,
                        args.serviceTypeId,
                        args.deliveryTypeId
                    ).toBundle()
                )
                recentSearchKeywordViewModel.addSearch(RecentSearchKeyword(keyword = keyword))
            }

            //backNumber 엔터입력시 키패드 내려감&포커스해제
            mainSearch.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    val keyword = mainSearch.text.toString()
                    if (keyword.isEmpty()) return@setOnKeyListener false
                    mainSearch.clearFocus()
                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(mainSearch.windowToken, 0)
                    errorLog("배달유형", "${args.serviceTypeId} ${args.deliveryTypeId}}")
                    mainSearch.navigate(
                        R.id.search_shop_detail_result_graph,
                        SearchShopDetailResultFragmentArgs(
                            keyword,
                            args.shopId,
                            args.shopName,
                            args.minOrderCost,
                            args.serviceTypeId,
                            args.deliveryTypeId
                        ).toBundle()
                    )
                    recentSearchKeywordViewModel.addSearch(RecentSearchKeyword(keyword = keyword))
                }
                false
            }
            mainSearch.requestFocus()
            KeyboardUtils.showSoftInput(mainSearch)
        }
    }

    //검색창 포커스 여부에 따라 색상을 변경하는 함수
    private fun searchFocusStatus() {
        with(binding) {
            mainSearch.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        mainSearch.setBackgroundResource(R.drawable.bg_address_focus_edittext)
                    } else {
                        mainSearch.setBackgroundResource(R.drawable.bg_address_edittext_selector)
                    }
                }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeleteSearchKeyword(event: DeleteSearchKeywordEvent) {
        recentSearchKeywordViewModel.deleteSearch(RecentSearchKeyword(keyword = event.keyword))
    }
}