package com.theone.busandbt.adapter.review

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemMyReviewDeliveryBinding
import com.theone.busandbt.dto.review.MemberReviewListItem
import com.theone.busandbt.eventbus.DoReviewRemoveEvent
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.adapter.ImageDetailPagerAdapter
import com.theone.busandbt.fragment.review.MyReviewFragmentDirections
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.google.android.material.chip.Chip
import org.greenrobot.eventbus.EventBus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 내가 쓴 리뷰 배달주문 리사이클러뷰 어뎁터
 * TODO: 리뷰 이미지가 안 뜸 (동백통 수정사항에 있던 현상) 리뷰 이미지 뜨도록 수정해야함 확인해보니 작은폰에서 안뜸.
 */
class MyReviewListAdapter(
    private val fragment: Fragment,
    private val serviceType: ServiceType,
    private val deliveryType: DeliveryType
) :
    DataBindingListAdapter<ItemMyReviewDeliveryBinding, MemberReviewListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_my_review_delivery

    override fun doBind(
        binding: ItemMyReviewDeliveryBinding,
        item: MemberReviewListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            review = item
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("yy-MM-dd")
            val dateTime = LocalDateTime.parse(item.createDateTime, inputFormatter)
            reviewDateTextView.text = dateTime.format(outputFormatter)

            if (item.imageFileList.isEmpty()) {
                reviewImageGroup.isVisible = false
            } else {
                reviewImageViewPager.adapter =
                    ImageDetailPagerAdapter(fragment, item.imageFileList.map { it.path })
                reviewImageViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        allCount.text = (position + 1).toString() + "/${item.imageFileList.size}"
                    }
                })
                reviewImageGroup.isVisible = true
            }
            baseRatingBar.rating = item.star.toFloat()
            reviewMenuChipGroup.removeAllViews()
            item.menuList.forEach {
                val chip = LayoutInflater.from(root.context)
                    .inflate(R.layout.layout_review_menu_chip, reviewMenuChipGroup, false) as Chip
                chip.isChecked = it.recommended
                chip.text = it.menuName
                reviewMenuChipGroup.addView(chip)
            }

            storeTitle.setOnClickListener { view ->
                val action = MyReviewFragmentDirections.actionMyReviewFragmentToShopDetailsFragment(
                    item.shopId,
                    serviceType.id,
                    deliveryType.id
                )
                view.findNavController()
                    .navigate(action)
            }

            editBtn.setOnClickListener { view ->
                val action =
                    MyReviewFragmentDirections.actionMyReviewFragmentToSetReviewFragment(item)
                view.findNavController()
                    .navigate(action)
            }

            deleteBtn.setOnClickListener { view ->
                val popupLayout =
                    LayoutInflater.from(view.context)
                        .inflate(R.layout.popup_review_delete, null)
                val builder = AlertDialog.Builder(view.context)
                    .setView(popupLayout)
                val callPopupWindow = builder.show()
                callPopupWindow.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val deleteBtn = popupLayout?.findViewById<Button>(R.id.deleteButton)
                val cancelBtn = popupLayout?.findViewById<Button>(R.id.cancelButton)
                //삭제하기 클릭시 커스텀한 토스트 출력.ㅌ
                deleteBtn?.setOnClickListener { _ ->
                    callPopupWindow.dismiss()
                    EventBus.getDefault().post(DoReviewRemoveEvent(item))
                }
                cancelBtn?.setOnClickListener {
                    callPopupWindow.dismiss()
                }
            }
        }
    }
}