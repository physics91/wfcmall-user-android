package com.theone.busandbt.utils

import android.view.View

typealias OnItemClick<Item> = (view: View, position: Int, item: Item) -> Unit
typealias OnClickNumberButtonListener = (view: View, number: Int?) -> Unit
