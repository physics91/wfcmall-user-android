package com.theone.busandbt.extension

import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.scrollPercent(): Double = (computeVerticalScrollOffset() * 1.0 / (computeVerticalScrollRange() - computeVerticalScrollExtent())) * 100.0