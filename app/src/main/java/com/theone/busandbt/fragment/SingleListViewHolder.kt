package com.theone.busandbt.fragment

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class SingleListViewHolder<ViewHolderBinding : ViewDataBinding, Item>(val binding: ViewHolderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(item: Item, position: Int, payloads: MutableList<Any>)
}