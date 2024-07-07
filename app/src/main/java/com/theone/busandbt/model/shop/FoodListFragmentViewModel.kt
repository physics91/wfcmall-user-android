package com.theone.busandbt.model.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class FoodListFragmentViewModel(private val state: SavedStateHandle) : ViewModel() {
    companion object {
        private const val CURRENT_CATEGORY_ID_KEY = "cci"
    }

    val currentCategoryIdLiveData: LiveData<Int> = state.getLiveData(CURRENT_CATEGORY_ID_KEY)

    fun setCurrentCategoryId(categoryId: Int) {
        state[CURRENT_CATEGORY_ID_KEY] = categoryId
    }
}