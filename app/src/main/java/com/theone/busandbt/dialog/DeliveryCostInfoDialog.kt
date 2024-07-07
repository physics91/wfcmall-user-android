package com.theone.busandbt.dialog

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.theone.busandbt.R
import com.theone.busandbt.adapter.shop.DeliveryCostInfoAdapter
import com.theone.busandbt.api.orderchannel.CostAPI
import com.theone.busandbt.databinding.DialogDeliveryCostInfoBinding
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.shop.ShopInfoFragmentArgs
import org.koin.android.ext.android.inject

class DeliveryCostInfoDialog :
    DataBindingDialog<DialogDeliveryCostInfoBinding>() {
    override val layoutId: Int = R.layout.dialog_delivery_cost_info
    private val costAPI: CostAPI by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        val shopId = args.getInt("shopId")
        val serviceTypeId = args.getInt("serviceTypeId")
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: return
        with(binding) {
            safeApiRequest(
                costAPI.getDeliveryCostInfo(
                    shopId,
                    da.jibun,
                    da.lat,
                    da.lng
                )
            ) {
                deliveryCostInfo = it
                if (it.deliveryCostList.isNotEmpty()) space.isVisible = true
                deliveryCostRecyclerView.adapter = DeliveryCostInfoAdapter(it.deliveryCostList)
                confirmBtn.setOnClickListener { dismiss() }
                goDeliveryCostDetailButton.setOnClickListener {
                    parentFragment?.view?.navigate(
                        R.id.shop_info_graph,
                        ShopInfoFragmentArgs(shopId, serviceTypeId, true).toBundle()
                    )
                }
            }
        }
    }
}