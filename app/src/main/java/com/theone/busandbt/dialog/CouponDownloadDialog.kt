package com.theone.busandbt.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theone.busandbt.R
import com.theone.busandbt.adapter.coupon.CouponDownloadListAdapter
import com.theone.busandbt.api.orderchannel.CouponAPI
import com.theone.busandbt.databinding.DialogCouponDownBinding
import com.theone.busandbt.dto.coupon.request.DownloadCouponListRequest
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageBar
import org.koin.android.ext.android.inject

/**
 * 쿠폰 다운 다이얼로그
 */
class CouponDownloadDialog : DataBindingBottomDialog<DialogCouponDownBinding>() {
    override val layoutId: Int = R.layout.dialog_coupon_down
    private val couponAPI: CouponAPI by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        with(binding) {
            val innerLoginInfo = loginInfo
            safeApiRequest(
                couponAPI.getShopCouponList(args.getInt("shopId", 0), innerLoginInfo?.id)
            ) {
                allCouponDownloadButton.setOnClickListener { _ ->
                    if (innerLoginInfo == null) {
                        suggestLogin()
                        return@setOnClickListener
                    }
                    val couponIdList =
                        it.filter { c -> !c.downloaded && c.remainCount >= 1 }
                            .map { c -> c.id }
                    if (couponIdList.isEmpty()) return@setOnClickListener
                    safeApiRequest(
                        couponAPI.downloadCouponList(
                            innerLoginInfo.getFormedToken(),
                            DownloadCouponListRequest(innerLoginInfo.id, couponIdList)
                        ),
                        showFailMessage = true
                    ) {
                        innerLoginInfo.couponCount += couponIdList.size
                        loginInfoViewModel.update()
                        view.showMessageBar("쿠폰이 정상적으로 다운로드 되었어요.")
                        dismiss()
                    }
                }
                exitBtn.setOnClickListener {
                    dismiss()
                }
                couponList.adapter =
                    CouponDownloadListAdapter(it).apply {
                        setOnItemClick { view, _, item ->
                            if (innerLoginInfo == null) {
                                suggestLogin()
                                return@setOnItemClick
                            }
                            if (item.downloaded || item.remainCount < 1) return@setOnItemClick
                            safeApiRequest(
                                couponAPI.downloadCouponList(
                                    innerLoginInfo.getFormedToken(),
                                    DownloadCouponListRequest(
                                        innerLoginInfo.id,
                                        listOf(item.id)
                                    )
                                ),
                                showFailMessage = true
                            ) {
                                innerLoginInfo.couponCount += 1
                                loginInfoViewModel.update()
                                view.showMessageBar("쿠폰이 정상적으로 다운로드 되었어요.")
                                dismiss()
                            }
                        }
                    }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
        return dialog
    }

    /**
     * 로그인 화면으로 유도한다.
     * 로그인이 필요한 상황에서 호출한다.
     */
    private fun suggestLogin() {
        view?.showMessageBar("로그인이 필요합니다.")
        val navController = Navigation.findNavController(
            requireActivity(),
            R.id.nav_host_fragment_content_main
        )
        navController.navigate(R.id.login_graph)
    }
}