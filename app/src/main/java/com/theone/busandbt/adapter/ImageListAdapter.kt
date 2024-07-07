package com.theone.busandbt.adapter

import com.bumptech.glide.Glide
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemMenuDetailsImgBinding

/**
 * 이미지 상세보기 리스트 어댑터
 */
class ImageListAdapter(imageUrlList: Array<String>) :
    DataBindingListAdapter<ItemMenuDetailsImgBinding, String>() {

    init {
        _items.addAll(imageUrlList)
    }

    override val viewHolderLayoutId: Int = R.layout.item_menu_details_img

    override fun doBind(
        binding: ItemMenuDetailsImgBinding,
        item: String,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            Glide.with(root)
                .load(item)
                .into(mainImageView)
        }
    }
}