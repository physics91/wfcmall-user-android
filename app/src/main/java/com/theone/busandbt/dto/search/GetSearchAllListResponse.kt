package com.theone.busandbt.dto.search

import com.theone.busandbt.dto.menu.MallMenuListItem
import com.theone.busandbt.dto.shop.ShopListItem

data class GetSearchAllListResponse(
    val foodInstantList: List<ShopListItem>,
    val foodPackagingList: List<ShopListItem>,
    val mallMenuList: List<MallMenuListItem>
)