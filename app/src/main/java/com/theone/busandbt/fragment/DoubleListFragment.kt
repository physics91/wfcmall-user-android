package com.theone.busandbt.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import androidx.core.widget.NestedScrollView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.adapter.order.OrderListAdapter
import com.theone.busandbt.dto.order.OrderListItem
import com.theone.busandbt.extension.safeApiRequest
import retrofit2.Call
import kotlin.properties.Delegates

/**
 * 화면에 하나의 데이터 리스트만 관리할 경우 사용할 수 있는 객체이다.
 * 데이터 리스트 조회는 Retrofit으로 한다.
 */
abstract class DoubleListFragment<Binding : ViewDataBinding> :
    DataBindingFragment<Binding>() {

    /**
     * 데이터 리스트가 표시될 [RecyclerView]를 정의한다.
     */
    protected abstract val recyclerView: RecyclerView

    protected abstract val nestedScrollView: NestedScrollView

    /**
     * 데이터 리스트가 관리될 [DataBindingListAdapter]를 정의한다.
     */
    protected abstract val recyclerViewAdapter: OrderListAdapter

    /**
     * 페이징 시스템의 현재 페이지를 의미한다.
     * 데이터 리스트가 조회될 때마다 다음 페이지로 넘어간다.
     * 변경은 반드시 UI 스레드에서 이루어져야 한다.
     * TODO 레이스 컨디션 고려
     */
    protected var page = 1

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

    /**
     * Retrofit 데이터 리스트 조회 객체를 정의한다.
     * [Call]의 파라미터는 달라질 수 있으므로 함수로 구현한다.
     */
    protected abstract fun getCall(): Call<List<OrderListItem>>

    @MainThread
    open fun onReceiveEmptyList(page: Int) {
    }

    @MainThread
    open fun onReceiveNotEmptyList(page: Int, data: List<OrderListItem>) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        if (isAutoLoad) dataLoad()
    }

    /**
     * 데이터 리스트를 초기화하고 새롭게 첫페이지부터 불러온다.
     */
    @MainThread
    fun refresh() {
        if (isDataLoading) return
        page = 1
        dataLoad(true)
    }

    @MainThread
    private fun initRecyclerView() {
        with(recyclerView) {
            adapter = recyclerViewAdapter
            nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
                val scrollViewHeight =
                    nestedScrollView.getChildAt(0).bottom - nestedScrollView.height
                val getScrollY = nestedScrollView.scrollY
                val scrollPosition = getScrollY.toDouble() / scrollViewHeight.toDouble() * 100.0
                if (scrollPosition >= 100.0) dataLoad()
            })
        }
    }

    /**
     * 리스트 데이터 로딩까지 처리
     * TODO 에러 처리
     */
    @MainThread
    fun dataLoad(isRefreshing: Boolean = false) {
        if (isDataLoading) return // 중복 데이터 로딩이 일어나지 않도록한다.
        isDataLoading = true
        try {
            safeApiRequest(getCall(), onFail = { _, _ -> isDataLoading = false }) {
                if (it.isEmpty()) {
                    if (isRefreshing) recyclerViewAdapter.clear()
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