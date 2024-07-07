package com.theone.busandbt.model.category

import androidx.lifecycle.ViewModel
import com.busandbt.code.ServiceType
import com.theone.busandbt.api.orderchannel.CategoryAPI
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.extension.callOnSuccess
import org.koin.java.KoinJavaComponent.inject

/**
 * 카테고리 정보들을 공유한다.
 * TODO 초기에 조회한 값을 Room으로 저장시켜야함.
 * TODO 카테고리가 변경됐을 경우 어떻게 처리할지도 고민해야함.
 */
class CategoryViewModel : ViewModel() {

    private val categoryAPI: CategoryAPI by inject(CategoryAPI::class.java)
    private val _mallCategoryList = ArrayList<CategoryListItem>()
    private val _foodDeliveryCategoryList = ArrayList<CategoryListItem>()
    val foodDeliveryCategoryList: List<CategoryListItem> get() = _foodDeliveryCategoryList
    val mallCategoryList: List<CategoryListItem> get() = _mallCategoryList

    fun init() {
        categoryAPI.getCategoryList(listOf(ServiceType.FOOD_DELIVERY.id, ServiceType.TRADITION.id)).callOnSuccess {
            _foodDeliveryCategoryList.addAll(it)
        }
        categoryAPI.getCategoryList(listOf(ServiceType.SHOPPING_MALL.id)).callOnSuccess {
            _mallCategoryList.addAll(it)
        }
    }
}