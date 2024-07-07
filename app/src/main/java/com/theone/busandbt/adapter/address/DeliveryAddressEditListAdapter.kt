package com.theone.busandbt.adapter.address

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Button
import androidx.navigation.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.api.orderchannel.DeliveryAddressAPI
import com.theone.busandbt.databinding.ItemAddressEditBinding
import com.theone.busandbt.dto.address.DeliveryAddress
import com.theone.busandbt.extension.callOnSuccess
import com.theone.busandbt.extension.showCompleteToast
import com.theone.busandbt.fragment.address.AddressEditFragmentDirections
import com.theone.busandbt.model.DeliveryAddressViewModel
import com.theone.busandbt.model.LoginInfoViewModel
import org.koin.java.KoinJavaComponent.inject
import java.lang.ref.WeakReference

/**
 * 주소 편집하기 리사이클러뷰 어댑터
 */
class DeliveryAddressEditListAdapter(
    private val deliveryAddressViewModel: DeliveryAddressViewModel,
    private val loginInfoViewModel: LoginInfoViewModel
) : DataBindingListAdapter<ItemAddressEditBinding, DeliveryAddress>() {
    override val viewHolderLayoutId: Int = R.layout.item_address_edit
    private val deliveryAddressAPI: DeliveryAddressAPI by inject(DeliveryAddressAPI::class.java)

    override fun doBind(
        binding: ItemAddressEditBinding,
        item: DeliveryAddress,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            address = item
            when (item.name) {
                "집" -> addressMarker.setImageResource(R.drawable.ic_home)
                "회사" -> addressMarker.setImageResource(R.drawable.ic_company)
                else -> addressMarker.setImageResource(R.drawable.ic_black_ping)
            }
            deleteTextView.setOnClickListener {
                val context = it.context
                val addressLayout =
                    LayoutInflater.from(context).inflate(R.layout.popup_address_delete, null)
                val builder = AlertDialog.Builder(context)
                    .setView(addressLayout)
                val addressPopupWindow = builder.show()
                addressPopupWindow.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val cancelButton = addressLayout?.findViewById<Button>(R.id.ceancellBtn)!!
                val deleteButton = addressLayout.findViewById<Button>(R.id.deleteBtn)!!
                //취소버튼
                cancelButton.setOnClickListener { addressPopupWindow.dismiss() }
                //삭제버튼
                deleteButton.setOnClickListener { v ->
                    addressPopupWindow.dismiss()
                    val weak = WeakReference(v.context)
                    deliveryAddressAPI.removeDeliveryAddress(
                        loginInfoViewModel.getLoginInfo()?.getFormedToken() ?: "",
                        item.deliveryAddressId
                    ).callOnSuccess {
                        val ctx = weak.get() ?: return@callOnSuccess
                        deliveryAddressViewModel.remove(item)
                        ctx.showCompleteToast(R.layout.toast_message_address_delete)
                    }
                }
            }
            modifyAddressTextView.setOnClickListener {
                val action =
                    AddressEditFragmentDirections.actionAddressSetEditFragmentToAddressDetailFragment(
                        item = item,
                        addressName = item.name
                    )
                it.findNavController().navigate(action)
            }
        }
    }
}