package com.theone.busandbt.model.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.coupon.UseCoupon
import com.theone.busandbt.dto.order.AddOrderCouponCount
import com.theone.busandbt.dto.order.request.AddOrderRequest
import com.theone.busandbt.dto.shop.BasketInfo

class AddOrderViewModel(private val state: SavedStateHandle) : ViewModel() {
    var isSelectedExceptDisposable = true
    var isSelectedShopMemoNextUse: Boolean? = null
    var shopMemo: String? = null
    var customerName: String? = null
    var isSelectedCustomerNameNextUse: Boolean? = null
    var isSelectedRiderMemoNextUse: Boolean? = null
    var riderMemo: String? = null
    var riderMemoInput: String? = null
    val useCouponList = ArrayList<UseCoupon>()
    var shopCouponDiscountCost: Int? = null
    var eventCouponDiscountCost: Int? = null
    var selectedPaymentTypeId: Int? = null
    var isSelectedPaymentNextUse: Boolean? = null
    var isSelectedSecurityTelUse: Boolean? = null
    val basketInfoListLiveData: LiveData<List<BasketInfo>> = state.getLiveData("basketInfoList")
    val couponCountLiveData: LiveData<AddOrderCouponCount> = state.getLiveData("couponCount")
    val addOrderRequestLiveData: LiveData<AddOrderRequest> = state.getLiveData("addOrderRequest")

    fun setBasketInfoList(basketInfoList: List<BasketInfo>) {
        state["basketInfoList"] = basketInfoList
    }

    fun setCouponCount(shopCouponCount: Int, eventCouponCount: Int) {
        state["couponCount"] = AddOrderCouponCount(shopCouponCount, eventCouponCount)
    }

    fun setAddOrderRequest(request: AddOrderRequest) {
        state["addOrderRequest"] = request
    }
}