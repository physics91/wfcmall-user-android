package com.theone.busandbt.fragment.search

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.databinding.FragmentSearchMainBinding
import com.theone.busandbt.eventbus.DeleteSearchKeywordEvent
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.adapter.search.SearchMainAdapter
import com.theone.busandbt.item.RecentSearchKeyword
import com.theone.busandbt.model.RecentSearchKeywordViewModel
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 메인 검색 화면
 * 메인 화면과 연결되어있으며 검색창 구현 및 최근 검색어를 보여준다.
 */
class SearchMainFragment : DataBindingFragment<FragmentSearchMainBinding>(), EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_search_main

    private lateinit var searchMainAdapter: SearchMainAdapter
    private lateinit var recentSearchKeywordViewModel: RecentSearchKeywordViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            searchFocusStatus()
            recentSearchKeywordViewModel = ViewModelProvider(
                requireActivity(),
                RecentSearchKeywordViewModel.Factory(requireActivity().application)
            )[RecentSearchKeywordViewModel::class.java]
            searchMainAdapter = SearchMainAdapter()
            keywordRecyclerView.adapter = searchMainAdapter
            keywordRecyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(26F)))
            recentSearchKeywordViewModel.readAllData.observe(requireActivity()) {
                searchMainAdapter.setItems(it)
                notExistSearchInclude.root.isVisible = it.isEmpty()
            }
            searchMainAdapter.setOnItemClick { _, _, item ->
                mainSearch.setText(item.keyword)
                goSearch(item.keyword)
            }

            goBackButton.setOnClickListener {
                it.findNavController().popBackStack()
            }
            allKeywordDeleteButton.setOnClickListener {
                recentSearchKeywordViewModel.deleteAll()
                view.showMessageBar("최근 검색어가 정상적으로 삭제되었어요.")
            }
            searchImg.setOnClickListener {
                val keyword = mainSearch.text.toString()
                if (keyword.isEmpty()) return@setOnClickListener
                val action =
                    SearchMainFragmentDirections.actionSearchMainFragmentToSearchResultFragment(
                        keyword
                    )
                findNavController().navigate(action)
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

                    goSearch(keyword)
                }
                false
            }

            mainSearch.requestFocus()
            KeyboardUtils.showSoftInput(mainSearch)
        }
    }

    private fun goSearch(keyword: String) {
        val action =
            SearchMainFragmentDirections.actionSearchMainFragmentToSearchResultFragment(
                keyword
            )
        findNavController().navigate(action)
        recentSearchKeywordViewModel.addSearch(RecentSearchKeyword(keyword = keyword))
    }

    //검색창 포커스 여부에 따라 색상을 변경하는 함수
    private fun searchFocusStatus() {
        with(binding) {
            mainSearch.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        mainSearch.setBackgroundResource(R.drawable.bg_address_focus_edittext)
                        val textLength = mainSearch.text?.length ?: 0
                        mainSearch.setSelection(textLength)
                        notExistSearchInclude.root.isVisible = false
                    } else {
                        mainSearch.setBackgroundResource(R.drawable.bg_address_edittext_selector)
                        notExistSearchInclude.root.isVisible = true
                    }
                }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeleteSearchKeyword(event: DeleteSearchKeywordEvent) {
        recentSearchKeywordViewModel.deleteSearch(RecentSearchKeyword(keyword = event.keyword))
    }
}