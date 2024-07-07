package com.theone.busandbt.repository

import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.dao.BasketMenuDao
import com.theone.busandbt.dao.BasketMenuOptionDao
import com.theone.busandbt.dao.BasketShopDao
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.item.basket.BasketMenu
import com.theone.busandbt.item.basket.BasketMenuOption
import com.theone.busandbt.item.basket.BasketMenuWithoutOption
import com.theone.busandbt.item.basket.BasketShop
import com.theone.busandbt.item.basket.BasketShopWithoutMenu

class BasketRepository(
    private val basketShopDao: BasketShopDao,
    private val basketMenuDao: BasketMenuDao,
    private val basketMenuOptionDao: BasketMenuOptionDao
) {

    suspend fun getShopListAll(): List<BasketShop> {
        return basketShopDao.getShopListAll().map {
            BasketShop(
                it.id,
                it.shopId,
                it.shopName,
                it.serviceTypeId,
                it.deliveryTypeId,
                it.deliveryCost,
                it.minOrderCost,
                it.isSelected,
                basketMenuDao.getList(it.id).map { menu ->
                    BasketMenu(
                        menu.id,
                        menu.basketShopId,
                        menu.menuId,
                        menu.status,
                        menu.name,
                        basketMenuOptionDao.getList(menu.id).toMutableList(),
                        menu.isAdultMenu,
                        menu.imageUrl,
                        menu.cost,
                        menu.saleCost,
                        menu.isSelected,
                        menu.count,
                        menu.menuCostId,
                        menu.menuCostName
                    )
                }.toMutableList()
            )
        }
    }

    suspend fun addMenu(item: BasketMenu) {
        with(item) {
            val basketMenuId = basketMenuDao.add(
                BasketMenuWithoutOption(
                    id,
                    basketShopId,
                    menuId,
                    status,
                    name,
                    isAdultMenu,
                    imageUrl,
                    cost,
                    saleCost,
                    isSelected,
                    count,
                    menuCostId,
                    menuCostName
                )
            )
            item.id = basketMenuId
            item.optionList.forEach {
                it.basketMenuId = basketMenuId
                basketMenuOptionDao.add(it)
            }
        }
    }

    suspend fun setMenu(item: BasketMenuWithoutOption) {
        basketMenuDao.set(item)
    }

    suspend fun setMenu(item: BasketMenu) {
        with(item) {
            basketMenuDao.set(
                BasketMenuWithoutOption(
                    id,
                    basketShopId,
                    menuId,
                    status,
                    name,
                    isAdultMenu,
                    imageUrl,
                    cost,
                    saleCost,
                    isSelected,
                    count,
                    menuCostId,
                    menuCostName
                )
            )
        }
    }

    suspend fun removeMenu(item: BasketMenu) {
        basketMenuDao.remove(item.id)
        basketMenuOptionDao.removeAll(item.id)
    }

    suspend fun removeMenuList(list: List<BasketMenu>) {
        list.forEach { removeMenu(it) }
    }

    suspend fun removeOptionList(list: List<BasketMenuOption>) {
        debugLog("장바구니 옵션 리스트 제거", list)
        list.forEach { basketMenuOptionDao.remove(it.id) }
//        basketMenuOptionDao.removeAll(list.map { it.id })
    }

    suspend fun add(item: BasketShop) {
        debugLog("장바구니 데이터", "전체 추가 : $item")
        with(item) {
            val basketShopId = basketShopDao.add(
                BasketShopWithoutMenu(
                    id,
                    shopId,
                    shopName,
                    serviceTypeId,
                    deliveryTypeId,
                    deliveryCost,
                    minOrderCost,
                    isSelected
                )
            )
            item.id = basketShopId
            menuList.forEach {
                it.basketShopId = basketShopId
                addMenu(it)
            }
            debugLog("추가된 장바구니 상점", item)
        }
    }

    suspend fun set(item: BasketShop) {
        with(item) {
            basketShopDao.set(
                BasketShopWithoutMenu(
                    id,
                    shopId,
                    shopName,
                    serviceTypeId,
                    deliveryTypeId,
                    deliveryCost,
                    minOrderCost,
                    isSelected
                )
            )
            menuList.forEach {
                basketMenuDao.set(
                    BasketMenuWithoutOption(
                        it.id,
                        it.basketShopId,
                        it.menuId,
                        it.status,
                        it.name,
                        it.isAdultMenu,
                        it.imageUrl,
                        it.cost,
                        it.saleCost,
                        it.isSelected,
                        it.count,
                        it.menuCostId,
                        it.menuCostName
                    )
                )
                it.optionList.forEach { option ->
                    basketMenuOptionDao.set(option)
                }
            }
        }
    }

    suspend fun setAllOptionList(itemList: List<BasketMenuOption>) {
        if (itemList.isEmpty()) return
        basketMenuOptionDao.setAll(itemList)
    }

    suspend fun remove(item: BasketShop) {
        with(item) {
            basketShopDao.remove(id)
            basketMenuDao.removeAll(id)
            menuList.forEach {
                basketMenuOptionDao.removeAll(it.id)
            }
        }
    }

    suspend fun removeAll(serviceType: ServiceType, deliveryType: DeliveryType) {
        val targetList = basketShopDao.getShopListAll()
            .filter { it.serviceTypeId == serviceType.id && it.deliveryTypeId == deliveryType.id }
        targetList.forEach {
            basketShopDao.remove(it.id)
            val menuList = basketMenuDao.getList(it.id)
            basketMenuDao.removeAll(it.id)
            menuList.forEach { bm ->
                basketMenuOptionDao.removeAll(bm.id)
            }
        }
    }
}