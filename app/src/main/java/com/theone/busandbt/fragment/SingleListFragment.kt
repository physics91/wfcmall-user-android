package com.theone.busandbt.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import androidx.core.widget.NestedScrollView
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.scrollPercent
import com.theone.busandbt.model.SingleListFragmentViewModel
import retrofit2.Call
import kotlin.properties.Delegates

/**
 * 화면에 하나의 데이터 리스트만 관리할 경우 사용할 수 있는 객체이다.
 * 데이터 리스트 조회는 Retrofit으로 한다.
 */
abstract class SingleListFragment<Binding : ViewDataBinding, Adapter : DataBindingListAdapter<*, Item>, Item> :
    DataBindingFragment<Binding>() {

    /**
     * 데이터 리스트가 표시될 [RecyclerView]를 정의한다.
     */
    protected abstract val recyclerView: RecyclerView

    /**
     * 데이터 리스트가 관리될 [DataBindingListAdapter]를 정의한다.
     */
    protected lateinit var recyclerViewAdapter: Adapter

    protected open val customScrollView: NestedScrollView? = null

    protected val viewModel: SingleListFragmentViewModel<Item> by viewModels()

    /**
     * 페이징 시스템의 현재 페이지를 의미한다.
     * 데이터 리스트가 조회될 때마다 다음 페이지로 넘어간다.
     * 변경은 반드시 UI 스레드에서 이루어져야 한다.
     * TODO 레이스 컨디션 고려
     */
    protected var page = 1
        @MainThread set

    /**
     * 화면 접속 시 바로 데이터 로딩이 되게 할지 결정한다.
     */
    protected open val isAutoLoad: Boolean = true

    /**
     * 데이터 리스트를 조회 중이면 true를 반환한다.
     * true일 때는 추가적인 데이터 조회가 일어나선 안된다.
     * TODO 레이스 컨디션 고려
     */
    private var isDataLoading: Boolean by Delegates.observable(false) { _, _, newValue ->
        appViewModel.showProgress(newValue)
    }

    private var allDataLoaded = false

    /**
     * Retrofit 데이터 리스트 조회 객체를 정의한다.
     * [Call]의 파라미터는 달라질 수 있으므로 함수로 구현한다.
     */
    protected abstract fun getCall(): Call<List<Item>>

    protected abstract fun makeAdapter(): Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initViewModelListener()
        if (isAutoLoad) initData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setCurrentPage(page)
        viewModel.setAllDataLoaded(allDataLoaded)
        viewModel.setItemList(recyclerViewAdapter.items)
        viewModel.saved = true
    }

    protected fun initData() {
        if (viewModel.saved) return
        refresh()
    }

    /**
     * 데이터 리스트를 초기화하고 새롭게 첫페이지부터 불러온다.
     */
    @MainThread
    open fun refresh() {
        if (isDataLoading) return
        page = 1
        allDataLoaded = false
        dataLoad(true)
    }

    @MainThread
    open fun onReceiveEmptyList(page: Int) {
    }

    @MainThread
    open fun onReceiveNotEmptyList(page: Int, data: List<Item>) {
    }

    @MainThread
    private fun initViewModelListener() {
        viewModel.currentPageLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            page = it
            viewModel.itemListLiveData.observe(viewLifecycleOwner) oe@{ itemList ->
                if (itemList == null) return@oe
                recyclerViewAdapter.setItems(itemList)
                if (itemList.isEmpty()) onReceiveEmptyList(it) else onReceiveNotEmptyList(it, itemList)
            }
        }
        viewModel.allDataLoadedLiveData.observe(viewLifecycleOwner) {
            if (it == null || allDataLoaded == it) return@observe
            allDataLoaded = it
        }
    }

    @MainThread
    private fun initRecyclerView() {
        with(recyclerView) {
            recyclerViewAdapter = makeAdapter()
            recyclerView.adapter = recyclerViewAdapter
            val cs = customScrollView
            if (cs != null) {
                cs.viewTreeObserver.addOnScrollChangedListener {
                    val maxScrollY = cs.getChildAt(0).height - cs.height
                    val currentScrollY = cs.scrollY

                    val scrollPercentage = if (maxScrollY != 0) {
                        100.0 * currentScrollY / maxScrollY
                    } else {
                        0.0
                    }

                    if (scrollPercentage >= 100.0) dataLoad()
                }
            } else {
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (recyclerView.scrollPercent() >= 100.0) dataLoad()
                    }
                })
            }
        }
    }

    /**
     * 리스트 데이터 로딩까지 처리
     * @param isRefreshing 새로고침일때만 true를 입력한다. true일 경우 기존 데이터와 비교해서 데이터교체가 깔끔하도록 한다.
     * TODO 에러 처리
     */
    @MainThread
    private fun dataLoad(isRefreshing: Boolean = false) {
        if (isDataLoading || allDataLoaded) return // 중복 데이터 로딩이 일어나지 않도록한다.
        isDataLoading = true
        try {
            safeApiRequest(getCall(), onFail = { _, _ -> isDataLoading = false }) {
                if (it.isEmpty()) {
                    if (isRefreshing) recyclerViewAdapter.clear()
                    allDataLoaded = true
                    onReceiveEmptyList(page)
                } else {
                    if (isRefreshing) recyclerViewAdapter.setItems(it) else recyclerViewAdapter.addItems(it)
                    onReceiveNotEmptyList(page, it)
                    page++
                }
                isDataLoading = false
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            isDataLoading = false
        }
    }
}