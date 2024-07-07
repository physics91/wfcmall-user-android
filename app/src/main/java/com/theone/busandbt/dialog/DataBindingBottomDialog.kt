package com.theone.busandbt.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.dto.address.DeliveryAddress
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.addon.RequiredDeliveryAddress
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.item.LoginInfo
import com.theone.busandbt.model.AppViewModel
import com.theone.busandbt.model.DeliveryAddressViewModel
import com.theone.busandbt.model.LoginInfoViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

abstract class DataBindingBottomDialog<VDB : ViewDataBinding> : BottomSheetDialogFragment() {
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

    /**
     * TODO - 현재는 개발 속도 문제로 여기에 정의
     */
    protected val deliveryAddressViewModel: DeliveryAddressViewModel by activityViewModels()
    protected val loginInfo: LoginInfo? get() = loginInfoViewModel.getLoginInfo()
    protected val deliveryAddress: DeliveryAddress? get() = deliveryAddressViewModel.selectedDeliveryAddress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
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

        if (this is RequiredDeliveryAddress && deliveryAddress == null) {
            view.showMessageBar("주소 등록이 필요합니다.")
            dismiss()
            return
        }

        // 로그인이 필요한 화면의 경우
        if (this is RequiredLogin && !loginInfoViewModel.isLoginState()) {
            goLogin()
            return
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun goLogin() {
        val dest = findNavController().currentDestination ?: return
        view?.showMessageBar("로그인이 필요합니다.")
        findNavController().navigate(
            R.id.login_graph,
            null,
            NavOptions.Builder().setPopUpTo(dest.id, true).build()
        )
    }
}