package com.theone.busandbt.adapter

import com.bumptech.glide.Glide
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemFoodCategoryGridviewBinding
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.shop.FoodListFragmentArgs
import com.theone.busandbt.fragment.shop.ShoppingListFragmentArgs

/**
 * 카테고리 어뎁터입니다
 */
class CategoryRecyclerViewListAdapter(
    categoryList: List<CategoryListItem>,
    private val serviceType: ServiceType,
    private val deliveryType: DeliveryType
) : DataBindingListAdapter<ItemFoodCategoryGridviewBinding, CategoryListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_food_category_gridview

    init {
        _items.addAll(categoryList)
    }

    override fun doBind(
        binding: ItemFoodCategoryGridviewBinding,
        item: CategoryListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            Glide.with(foodCategoryTextView.context).load(item.imageUrl).into(foodCategoryImageView)
            foodCategoryTextView.text = item.name
            foodCategoryImageView.setOnClickListener {
                when (serviceType) {
                    ServiceType.FOOD_DELIVERY -> it.navigate(
                        R.id.food_list_graph,
                        FoodListFragmentArgs(item.id, deliveryType.id).toBundle()
                    )

                    ServiceType.SHOPPING_MALL -> {
                        when (deliveryType) {
                            DeliveryType.PARCEL -> it.navigate(
                                R.id.mall_menu_list_graph,
                                ShoppingListFragmentArgs(item.id).toBundle()
                            )

                            else -> {}
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}