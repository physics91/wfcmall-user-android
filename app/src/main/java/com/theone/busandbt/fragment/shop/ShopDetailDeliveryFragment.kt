package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentShopDetailsDeliveryBinding
import com.theone.busandbt.dialog.DeliveryCostInfoDialog
import com.theone.busandbt.dto.shop.ShopDetail
import com.theone.busandbt.extension.getParcelableCompat
import com.theone.busandbt.fragment.DataBindingFragment

/**
 * 바로배달 화면
 */
class ShopDetailDeliveryFragment : DataBindingFragment<FragmentShopDetailsDeliveryBinding>() {
    override val layoutId: Int = R.layout.fragment_shop_details_delivery

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        val sd = args.getParcelableCompat<ShopDetail>("shopDetail") ?: return
        val serviceTypeId = args.getInt("serviceTypeId")
        with(binding) {
            shopDetail = sd
            paymentText.text = sd.paymentTypeNameList.joinToString(",")
            price.setOnClickListener {
                val dialog = DeliveryCostInfoDialog()
                dialog.arguments =
                    bundleOf("shopId" to args.getInt("shopId"), "serviceTypeId" to serviceTypeId)
                dialog.show(childFragmentManager, "")
            }
        }
    }
}