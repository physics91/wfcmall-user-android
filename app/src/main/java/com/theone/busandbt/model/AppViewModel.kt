package com.theone.busandbt.model

import androidx.annotation.ColorRes
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R

/**
 *
 */
class AppViewModel : ViewModel() {
    private val isProgress = MutableLiveData<Boolean>()
    private val goBasketButtonVisibleLiveData = MutableLiveData(false)
    private val goBackButtonVisibleLiveData = MutableLiveData(false)
    private val actionBarTitleLiveData = MutableLiveData<String>()
    private val actionBarBackgroundColorLiveData = MutableLiveData<Int>()
    var basketServiceType: ServiceType = ServiceType.FOOD_DELIVERY
    var basketDeliveryType: DeliveryType = DeliveryType.INSTANT
    var appState: AppState = AppState.NORMAL
    var showJoinPopup: Boolean = false

    /**
     * 현재 작업 중인 로직의 개수로 볼 수 있다.
     * 프로그래스 바의 보임/안보임 횟수를 각각 체크해서
     * 최종적으로 프로그래스 바가 보여야하는지 안보여야하는지 판단하는 용도로 사용한다.
     */
    private var workingJobCount: Int = 0

    fun observeProgressBar(owner: LifecycleOwner, op: (Boolean) -> Unit) {
        isProgress.observe(owner) {
            op(it)
        }
    }

    fun observeActionBarTitle(owner: LifecycleOwner, op: (String) -> Unit) {
        actionBarTitleLiveData.observe(owner) {
            op(it)
        }
    }

    fun observeGoBasketVisible(owner: LifecycleOwner, op: (Boolean) -> Unit) {
        goBasketButtonVisibleLiveData.observe(owner) {
            if (it == null) return@observe
            op(it)
        }
    }

    fun observeGoBackButtonVisible(owner: LifecycleOwner, op: (Boolean) -> Unit) {
        goBackButtonVisibleLiveData.observe(owner) {
            if (it == null) return@observe
            op(it)
        }
    }

    /**
     * 프로그래스 바가 기본적으로 숨김 상태인걸 전제로 두고 작성한다.
     * @param flag true면 프로그래스바가 보이게 하고 false면 숨긴다.
     */
    @MainThread
    fun showProgress(flag: Boolean) {
        val old = isProgress.value ?: false
        if (flag == old) return // 프로그래바의 현재 상태와 바꿀려는 상태가 같으면 동작할 필요가 없다.
        if (flag) workingJobCount++ else workingJobCount--
        if (old && workingJobCount > 0) return // 작업이 아직 남은 경우 프로그래스 바를 조작하지 않고 종료한다.
        isProgress.value = flag
    }

    @MainThread
    fun setActionBarTitle(headerTitle: String) {
        actionBarTitleLiveData.value = headerTitle
    }

    @MainThread
    fun setActionBarBackgroundColor(@ColorRes colorRes: Int?) {
        actionBarBackgroundColorLiveData.value = colorRes ?: R.color.white
    }

    @MainThread
    fun visibleBasket(isVisible: Boolean) {
        goBasketButtonVisibleLiveData.value = isVisible
    }

    @MainThread
    fun visibleGoBackButton(isVisible: Boolean) {
        goBackButtonVisibleLiveData.value = isVisible
    }

    enum class AppState {
        FIRST_JOIN, // 최초 접속
        NORMAL,
        ;
    }
}