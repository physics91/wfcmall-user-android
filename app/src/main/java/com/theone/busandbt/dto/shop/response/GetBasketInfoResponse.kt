package com.theone.busandbt.dto.shop.response

import com.theone.busandbt.dto.menu.MenuBasketInfo
import com.theone.busandbt.dto.menu.OptionBasketInfo
import com.theone.busandbt.dto.shop.BasketInfo

data class GetBasketInfoResponse(
    val shopBasketInfoList: List<BasketInfo>,
    val menuBasketInfoList: List<MenuBasketInfo>,
    val optionBasketInfoList: List<OptionBasketInfo>
)