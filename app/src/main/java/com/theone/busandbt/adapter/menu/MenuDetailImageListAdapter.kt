package com.theone.busandbt.adapter.menu

import android.graphics.drawable.Drawable
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemShoppingProductImageBinding
import com.theone.busandbt.eventbus.menu.SmallRecyclerLoadEvent
import org.greenrobot.eventbus.EventBus

/**
 * 쇼핑몰 상품상세에 들어가는 이미지 어뎁터
 */
class MenuDetailImageListAdapter(imageUrlList: List<String>) :
    DataBindingListAdapter<ItemShoppingProductImageBinding, String>() {
    override val viewHolderLayoutId: Int = R.layout.item_shopping_product_image
    private val imagesLoaded = mutableSetOf<Int>()

    init {
        _items.addAll(imageUrlList)
    }

    override fun doBind(
        binding: ItemShoppingProductImageBinding,
        item: String,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            Glide.with(shoppingImageView.context)
                .load(item)
                .thumbnail(0.1f)
                .dontTransform()
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                    ) {
                        shoppingImageView.setImageDrawable(resource)
                        shoppingImageView.isVisible = true
                        imagesLoaded.add(position)
                        if (imagesLoaded.size == _items.size) {
                            EventBus.getDefault().post(SmallRecyclerLoadEvent())
                        }
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }
}