package com.theone.busandbt.model.menu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MallMenuMiddleCategoryViewModel(private val state: SavedStateHandle): ViewModel() {
    var savedTabPosition: Int? = null
}