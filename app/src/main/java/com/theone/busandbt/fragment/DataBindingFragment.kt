package com.theone.busandbt.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.EnabledGoBasketButton
import com.theone.busandbt.addon.RequiredDeliveryAddress
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.dto.address.DeliveryAddress
import com.theone.busandbt.eventbus.ShowMessageDialogEvent
import com.theone.busandbt.extension.safe
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.item.LoginInfo
import com.theone.busandbt.listener.BackButtonInterceptor
import com.theone.busandbt.model.AppViewModel
import com.theone.busandbt.model.BasketListViewModel
import com.theone.busandbt.model.DeliveryAddressViewModel
import com.theone.busandbt.model.LoginInfoViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * DataBinding의 공통 작업을 정의한다.
 */
abstract class DataBindingFragment<VDB : ViewDataBinding> : Fragment() {

    companion object {
        const val EMPTY_ACTION_BAR_TITLE = " " // 액션바는 보이고 싶고 제목은 안보이고 싶을때 사용 [actionBarTitle]
    }

    private var _binding: VDB? = null
    protected val binding: VDB get() = _binding!!

    /**
     * R.layout의 값을 입력한다.
     */
    protected abstract val layoutId: Int

    /**
     * 로그인 상태를 체크하는 용도로 사용한다.
     */
    protected val loginInfoViewModel: LoginInfoViewModel by activityViewModel()
    protected val appViewModel: AppViewModel by activityViewModel()
    protected val basketListViewModel: BasketListViewModel by activityViewModel()

    /**
     * TODO - 현재는 개발 속도 문제로 여기에 정의
     */
    protected val deliveryAddressViewModel: DeliveryAddressViewModel by activityViewModels()
    protected val loginInfo: LoginInfo? get() = loginInfoViewModel.getLoginInfo()
    protected val deliveryAddress: DeliveryAddress? get() = deliveryAddressViewModel.selectedDeliveryAddress

    open val actionBarTitle: String = ""

    @ColorRes
    open val actionBarBackgroundColor: Int? = null

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backStack = findNavController().currentBackStackEntry
        if (backStack != null) {
            val stateHandle = backStack.savedStateHandle
            stateHandle.getLiveData<Bundle>("resultData").observe(backStack) {
                if (it == null) return@observe
                safe {
                    onResultDataReceived(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        if (parentFragment is NavHostFragment) { // 뷰페이저의 화면이 아닌 최상단의 화면일 경우에만
            if (this is RequiredDeliveryAddress && deliveryAddress == null) {
                view.showMessageBar("주소 등록이 필요합니다.")
                navController.popBackStack()
                return
            }

            // 로그인이 필요한 화면의 경우
            if (this is RequiredLogin && !loginInfoViewModel.isLoginState()) {
                suggestLogin()
                navController.popBackStack()
                return
            }

            appViewModel.setActionBarTitle(actionBarTitle)
            appViewModel.setActionBarBackgroundColor(actionBarBackgroundColor)
            appViewModel.visibleGoBackButton(this is EnabledGoBackButton)
            appViewModel.visibleBasket(this is EnabledGoBasketButton)

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (this@DataBindingFragment is BackButtonInterceptor && !interceptOnBackButtonClicked()) return
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setResultData(resultData: Bundle) {
        val navController = findNavController()
        val backStackEntry = navController.previousBackStackEntry ?: return
        backStackEntry.savedStateHandle["resultData"] = resultData
    }

    open fun onResultDataReceived(resultData: Bundle) {}

    protected fun suggestLogin() {
        val controller = findNavController()
        val dest = controller.currentDestination ?: return
        showMessageDialog(
            "로그인이 필요한 서비스 입니다.",
            "로그인 화면으로 이동합니다.",
            showWarningImageView = true,
            showCancelButton = true
        ) {
            onDoneButtonClick {
                dismiss()
                controller.navigate(
                    R.id.login_graph,
                    null,
                    NavOptions.Builder().setPopUpTo(dest.id, false).build()
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowMessageDialog(event: ShowMessageDialogEvent) {
        with(event) {
            showMessageDialog(
                title,
                subTitle,
                showCancelButton,
                showWarningImageView,
                cancelButtonText,
                cancelable = true,
                op
            )
        }
    }
}