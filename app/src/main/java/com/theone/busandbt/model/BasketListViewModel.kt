package com.theone.busandbt.model

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.dto.menu.MenuBasketInfo
import com.theone.busandbt.dto.menu.MenuOptionGroup
import com.theone.busandbt.dto.menu.OptionBasketInfo
import com.theone.busandbt.dto.shop.BasketInfo
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.getSelectedMenuList
import com.theone.busandbt.function.findTab
import com.theone.busandbt.item.Tab
import com.theone.busandbt.item.basket.BasketMenu
import com.theone.busandbt.item.basket.BasketMenuOption
import com.theone.busandbt.item.basket.BasketShop
import com.theone.busandbt.repository.BasketRepository
import com.theone.busandbt.utils.ENABLE_TAB_ARRAY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 장바구니 아이템 리스트 뷰모델
 * TODO - 배송지역 구분을 어떻게해서 장바구니 항목들을 비활성화 시킬 것인지
 * TODO - 현재 하나의 행정동으로 조회를 하는데 만약 지역이 애매하면 어떻게 처리할 것인지
 */
class BasketListViewModel(private val basketRepository: BasketRepository) : ViewModel() {
    private val basketLiveData = MutableLiveData<List<BasketShop>>()
    private val basketList = ArrayList<BasketShop>()
    private val selectedBasketShopListLiveData = HashMap<Tab, MutableLiveData<BasketShop>>()
    var basketInfoMap: Map<Int, BasketInfo>? = null

    fun init() {
        basketList.clear()
        viewModelScope.launch(Dispatchers.IO) {
            val basketList = basketRepository.getShopListAll()
            launch(Dispatchers.Main) {
                val selectedList = basketList.filter { it.isSelected }
                ENABLE_TAB_ARRAY.forEach { tab ->
                    selectedBasketShopListLiveData[tab] = MutableLiveData<BasketShop>().apply {
                        value =
                            selectedList.find { it.serviceTypeId == tab.serviceType.id && it.deliveryTypeId == tab.deliveryType.id }
                    }
                }
                this@BasketListViewModel.basketList.addAll(basketList)
                basketLiveData.value = this@BasketListViewModel.basketList
            }
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, op: (List<BasketShop>) -> Unit) {
        basketLiveData.observe(lifecycleOwner) {
            op(it)
        }
    }

    fun observeAllSelectedBasketShop(
        lifecycleOwner: LifecycleOwner,
        op: (Tab, BasketShop?) -> Unit
    ) {
        selectedBasketShopListLiveData.forEach { (t, u) ->
            u.observe(lifecycleOwner) {
                op(t, it)
            }
        }
    }

    fun save() {
        viewModelScope.launch(Dispatchers.IO) {
            basketList.forEach {
                basketRepository.set(it)
            }
            launch(Dispatchers.Main) {
                basketLiveData.value = basketList
            }
        }
    }

    fun setSelectedBasketShop(old: BasketShop?, basketShop: BasketShop) {
        if (old?.id == basketShop.id) return
        viewModelScope.launch(Dispatchers.IO) {
            if (old != null) basketRepository.set(old)
            basketRepository.set(basketShop)
            launch(Dispatchers.Main) {
                selectedBasketShopListLiveData[findTab(
                    basketShop.serviceTypeId,
                    basketShop.deliveryTypeId
                )]?.value = basketShop
            }
        }
    }

    fun getBasketShopIdList(): List<Int> = basketList.map { it.shopId }
    fun getBasketMenuIdList(): List<Int> = basketList.flatMap { it.menuList }.map { it.menuId }
    fun getBasketOptionIdList(): List<Int> =
        basketList.flatMap { it.menuList }.flatMap { it.optionList }.map { it.optionId }

    fun getBasketMenuCount(): Int = basketList.flatMap { it.menuList }.size

    fun getBasketList(serviceType: ServiceType, deliveryType: DeliveryType): List<BasketShop> =
        getBasketList(serviceType.id, deliveryType.id)

    fun getBasketList(serviceTypeId: Int, deliveryTypeId: Int): List<BasketShop> =
        basketList.filter { it.deliveryTypeId == deliveryTypeId && it.serviceTypeId == serviceTypeId }

    fun getSelectedBasketShop(serviceType: ServiceType, deliveryType: DeliveryType): BasketShop? =
        selectedBasketShopListLiveData[findTab(serviceType, deliveryType)]?.value

    fun getSelectedBasketShop(serviceTypeId: Int, deliveryTypeId: Int): BasketShop? =
        getSelectedBasketShop(ServiceType.find(serviceTypeId), DeliveryType.find(deliveryTypeId))


    fun getSelectedBasketShopList(
        serviceTypeId: Int,
        deliveryTypeId: Int
    ): List<BasketShop> = getBasketList(
        serviceTypeId, deliveryTypeId
    ).filter { it.isSelected }

    fun add(
        shopId: Int,
        shopName: String,
        serviceTypeId: Int,
        deliveryTypeId: Int,
        deliveryCost: Int,
        minOrderCost: Int,
        menuId: Int,
        menuStatus: Int,
        menuName: String,
        isAdultMenu: Boolean,
        menuCost: Int,
        menuSaleCost: Int,
        menuCount: Int,
        menuImageUrl: String,
        optionGroupList: List<MenuOptionGroup>,
        menuCostId: Int,
        menuCostName: String
    ) {
        val oldList = getBasketList(serviceTypeId, deliveryTypeId)
        val new = BasketShop.createForAdd(
            shopId,
            shopName,
            serviceTypeId,
            deliveryTypeId,
            deliveryCost,
            minOrderCost,
            menuId,
            menuStatus,
            menuName,
            isAdultMenu,
            menuCost,
            menuSaleCost,
            menuCount,
            menuImageUrl,
            optionGroupList,
            menuCostId,
            menuCostName
        )
        viewModelScope.launch(Dispatchers.IO) {
            if (oldList.isEmpty()) { // 장바구니가 하나도 없을 경우에는 묻지도 따지지도 않고 넣기
                basketRepository.add(new)
                update(this, new)
                return@launch
            }
            val existItem = oldList.find { it.shopId == new.shopId }
            if (existItem == null) { // 이미 들어간 장바구니 상점 데이터가 없다면 묻지도 따지지도 않고 넣기
                basketRepository.add(new)
                debugLog("장바구니", "새롭게 추가 : $new")
                update(this, new)
                return@launch
            }
            val newMenu = new.menuList.first()
            val existMenu = existItem.menuList.find { it.isContentSame(newMenu) }
            if (existMenu != null) { // 완전히 일치하는지 체크하고 일치하면 기존 데이터에 개수만 늘려주기
                debugLog("장바구니", "기존 메뉴 데이터에 개수만 추가")
                existMenu.count += newMenu.count
                basketRepository.setMenu(existMenu)
                if (!existItem.isSelected) {
                    existItem.isSelected = true
                    existItem.menuList.forEach { it.isSelected = true }
                }
                update(this, existItem)
                return@launch
            }

            debugLog("장바구니", "상점은 있고 새로운 메뉴만 추가")
            // 메뉴 추가
            newMenu.basketShopId = existItem.id
            basketRepository.addMenu(newMenu)
            existItem.menuList.add(newMenu)
            if (!existItem.isSelected) {
                existItem.isSelected = true
                existItem.menuList.forEach { it.isSelected = true }
            }
            update(this, existItem)
        }
    }

    /**
     * 추가된 항목의 basketShop을 선택하고 기존에 선택된걸 해제하는 로직
     */
    private suspend fun selectNewAndReleaseOld(new: BasketShop) {
        // 기존에 선택된 항목이 없다면 (장바구니에 새롭게 메뉴가 추가된 케이스)
        val old = getSelectedBasketShop(new.serviceTypeId, new.deliveryTypeId) ?: return
        debugLog("장바구니 기존", "있다 : $old")
        // 새로운것과 기존것의 상점이 동일한 경우
        if (new.shopId == old.shopId) return
        debugLog("장바구니 기존", "해제 : $old")
        old.isSelected = false
        old.menuList.forEach {
            it.isSelected = false
        }
        basketRepository.set(old)
    }

    private suspend fun update(coroutineScope: CoroutineScope, new: BasketShop) {
        selectNewAndReleaseOld(new)
        debugLog("장바구니 업데이트", new)
        val position = basketList.indexOfFirst { it.id == new.id }
        if (position != -1) {
            basketList[position] = new
        } else {
            basketList.add(new)
        }
        coroutineScope.launch(Dispatchers.Main) {
            debugLog("장바구니 업데이트 최종", new)
            basketLiveData.value = basketList
            selectedBasketShopListLiveData[findTab(new.serviceTypeId, new.deliveryTypeId)]?.value =
                new
        }
    }

    /**
     * @param oldDeliveryType 이전 배달유형으로 "포장주문으로 이동" 같은 기능을 수행할 경우 입력한다.
     */
    fun set(item: BasketShop, oldDeliveryType: DeliveryType? = null) {
        val index = basketList.indexOfFirst { it.id == item.id }
        if (index == -1) return
        viewModelScope.launch(Dispatchers.IO) {
            basketRepository.set(item)
            launch(Dispatchers.Main) {
                if (oldDeliveryType != null) {
                    val another = getBasketList(
                        item.serviceTypeId,
                        oldDeliveryType.id
                    ).firstOrNull()
                    another?.isSelected = true
                    if (another != null) set(another)
                    selectedBasketShopListLiveData[findTab(
                        item.serviceTypeId,
                        oldDeliveryType.id
                    )]?.value = another
                    selectedBasketShopListLiveData[findTab(
                        item.serviceTypeId,
                        oldDeliveryType.id
                    )]?.value = null
                }
                val old = selectedBasketShopListLiveData[findTab(
                    item.serviceTypeId,
                    item.deliveryTypeId
                )]?.value
                if (old != null && old != item && item.isSelected) {
                    launch(Dispatchers.IO) {
                        old.isSelected = false
                        old.menuList.forEach {
                            it.isSelected = false
                        }
                        basketRepository.set(old)
                    }
                }
                selectedBasketShopListLiveData[findTab(
                    item.serviceTypeId,
                    item.deliveryTypeId
                )]?.value = item
                basketList[index] = item
                basketLiveData.value = basketList
            }
        }
    }

    fun setMenu(item: BasketMenu) {
        viewModelScope.launch(Dispatchers.IO) {
            basketRepository.setMenu(item)
        }
    }

    fun remove(item: BasketShop) {
        viewModelScope.launch(Dispatchers.IO) {
            basketRepository.remove(item)
            launch(Dispatchers.Main) {
                basketList.remove(item)
                if (item.isSelected) { // 선택돼있는 경우 다른걸 선택하거나 없애야함
                    val another =
                        getBasketList(item.serviceTypeId, item.deliveryTypeId).firstOrNull()
                    another?.isSelected = true
                    another?.menuList?.forEach { it.isSelected = true }
                    if (another != null) set(another)
                    selectedBasketShopListLiveData[findTab(
                        item.serviceTypeId,
                        item.deliveryTypeId
                    )]?.value = another
                }
                basketLiveData.value = basketList
            }
        }
    }

    fun removeAll(serviceType: ServiceType, deliveryType: DeliveryType) {
        viewModelScope.launch(Dispatchers.IO) {
            basketRepository.removeAll(serviceType, deliveryType)
            launch(Dispatchers.Main) {
                basketList.removeAll { it.serviceTypeId == serviceType.id && it.deliveryTypeId == deliveryType.id }
                selectedBasketShopListLiveData[findTab(serviceType, deliveryType)]?.value = null
                basketLiveData.value = basketList
            }
        }
    }

    fun removeMenu(menu: BasketMenu) {
        viewModelScope.launch(Dispatchers.IO) {
            basketRepository.removeMenu(menu)
            launch(Dispatchers.Main) main@{
                val basketShop = basketList.find { it.id == menu.basketShopId } ?: return@main
                basketShop.menuList.remove(menu)
                if (basketShop.menuList.isEmpty()) { // 상점의 모든 장바구니 메뉴가 사라질 경우 상점 데이터도 사라져야한다.
                    basketRepository.remove(basketShop)
                    basketList.remove(basketShop)
                    if (basketShop.isSelected) { // 선택돼있는 경우 다른걸 선택하거나 없애야함
                        val another = getBasketList(
                            basketShop.serviceTypeId,
                            basketShop.deliveryTypeId
                        ).firstOrNull()
                        another?.isSelected = true
                        another?.menuList?.forEach { it.isSelected = true }
                        if (another != null) set(another)
                        selectedBasketShopListLiveData[findTab(
                            basketShop.serviceTypeId,
                            basketShop.deliveryTypeId
                        )]?.value = another
                    }
                    basketLiveData.value = basketList
                    return@main
                }
                if (basketShop.isSelected) { // 갱신
                    selectedBasketShopListLiveData[findTab(
                        basketShop.serviceTypeId,
                        basketShop.deliveryTypeId
                    )]?.value = basketShop
                }
                basketLiveData.value = basketList
            }
        }
    }

    fun removeSelectedMenu(basketShop: BasketShop) {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedMenuList = basketShop.getSelectedMenuList()
            basketRepository.removeMenuList(selectedMenuList)
            basketShop.menuList.removeAll(selectedMenuList)
            launch(Dispatchers.Main) main@{
                if (basketShop.menuList.isEmpty()) {
                    basketRepository.remove(basketShop)
                    basketList.remove(basketShop)
                    if (basketShop.isSelected) { // 선택돼있는 경우 다른걸 선택하거나 없애야함
                        val another = getBasketList(
                            basketShop.serviceTypeId,
                            basketShop.deliveryTypeId
                        ).firstOrNull()
                        another?.isSelected = true
                        another?.menuList?.forEach { it.isSelected = true }
                        if (another != null) set(another)
                        selectedBasketShopListLiveData[findTab(
                            basketShop.serviceTypeId,
                            basketShop.deliveryTypeId
                        )]?.value = another
                    }
                    basketLiveData.value = basketList
                    return@main
                }
                if (basketShop.isSelected) { // 갱신
                    selectedBasketShopListLiveData[findTab(
                        basketShop.serviceTypeId,
                        basketShop.deliveryTypeId
                    )]?.value = basketShop
                }
                basketLiveData.value = basketList
            }
        }
    }

    fun updateBasketMenuList(
        menuBasketInfoList: List<MenuBasketInfo>,
        optionBasketInfoList: List<OptionBasketInfo>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val menuBasketInfoMap = menuBasketInfoList.associateBy { it.menuId }
            val optionBasketInfoMap = optionBasketInfoList.associateBy { it.optionId }
            val requireRemoveMenuList = ArrayList<BasketMenu>()
            val requireUpdateMenuList = ArrayList<BasketMenu>()
            val requireRemoveOptionList = ArrayList<BasketMenuOption>()
            val requireUpdateOptionList = ArrayList<BasketMenuOption>()
            basketList.flatMap { it.menuList }.forEach { bm ->
                val menuBasketInfo = menuBasketInfoMap[bm.menuId]
                if (menuBasketInfo == null) {
                    requireRemoveMenuList.add(bm)
                    return@forEach
                }
                val menuCost = menuBasketInfo.menuCostList.find { mc -> mc.id == bm.menuCostId }
                if (menuCost == null) {
                    requireRemoveMenuList.add(bm)
                    return@forEach
                }
                var changed = false
                if (bm.cost != menuCost.cost) {
                    bm.cost = menuCost.cost
                    changed = true
                }
                if (bm.saleCost != menuCost.saleCost) {
                    bm.saleCost = menuCost.saleCost
                    changed = true
                }
                if (changed) requireUpdateMenuList.add(bm)
                if (bm.optionList.isEmpty()) return@forEach
                bm.optionList.forEach option@{ bo ->
                    val optionBasketInfo = optionBasketInfoMap[bo.optionId]
                    if (optionBasketInfo == null) {
                        requireRemoveOptionList.add(bo)
                        return@option
                    }
                    if (bo.cost != optionBasketInfo.cost) {
                        bo.cost = optionBasketInfo.cost
                        requireUpdateOptionList.add(bo)
                    }
                }
            }
            var changed = false
            if (requireRemoveMenuList.isNotEmpty()) {
                basketRepository.removeMenuList(requireRemoveMenuList)
                val requireRemoveShopList = ArrayList<BasketShop>()
                basketList.forEach { bs ->
                    bs.menuList.removeAll(requireRemoveMenuList)
                    if (bs.menuList.isEmpty()) { // 상점의 모든 장바구니 메뉴가 사라질 경우 상점 데이터도 사라져야한다.
                        basketRepository.remove(bs)
                        requireRemoveShopList.add(bs)
                        if (bs.isSelected) { // 선택돼있는 경우 다른걸 선택하거나 없애야함
                            val another = getBasketList(
                                bs.serviceTypeId,
                                bs.deliveryTypeId
                            ).firstOrNull()
                            another?.isSelected = true
                            another?.menuList?.forEach { it.isSelected = true }
                            launch(Dispatchers.Main) {
                                if (another != null) set(another)
                                selectedBasketShopListLiveData[findTab(
                                    bs.serviceTypeId,
                                    bs.deliveryTypeId
                                )]?.value = another
                            }
                        }
                        return@forEach
                    }
                    if (bs.isSelected) { // 갱신
                        launch(Dispatchers.Main) {
                            selectedBasketShopListLiveData[findTab(
                                bs.serviceTypeId,
                                bs.deliveryTypeId
                            )]?.value = bs
                        }
                    }
                }
                if (requireRemoveShopList.isNotEmpty()) basketList.removeAll(requireRemoveShopList)
                changed = true
            }

            if (requireUpdateMenuList.isNotEmpty()) {
                requireUpdateMenuList.forEach { bm -> basketRepository.setMenu(bm) }
                changed = true
            }

            if (requireRemoveOptionList.isNotEmpty()) {
                basketRepository.removeOptionList(requireRemoveOptionList)
                changed = true
            }

            if (requireUpdateOptionList.isNotEmpty()) {
                basketRepository.setAllOptionList(requireUpdateOptionList)
                changed = true
            }

            if (changed) launch(Dispatchers.Main) {
                basketLiveData.value = basketList
            }
        }
    }
}