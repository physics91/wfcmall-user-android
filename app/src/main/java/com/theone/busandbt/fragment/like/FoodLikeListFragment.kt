package com.theone.busandbt.fragment.like

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.busandbt.code.DeliveryType
import com.busandbt.code.LikeSortType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.shop.FoodLikeListAdapter
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentFoodLikeListBinding
import com.theone.busandbt.dialog.selection.LikeSortTypeSelectionDialog
import com.theone.busandbt.dto.Selection
import com.theone.busandbt.dto.shop.LikedFoodListItem
import com.theone.busandbt.extension.desc
import com.theone.busandbt.extension.getDeliveryType
import com.theone.busandbt.extension.getServiceType
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.setOnReceiveData
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.fragment.SingleListFragment
import org.koin.android.ext.android.inject
import retrofit2.Call
import kotlin.properties.Delegates

/**
 * 음식점 바로배달 찜
 */
class FoodLikeListFragment :
    SingleListFragment<FragmentFoodLikeListBinding, FoodLikeListAdapter, LikedFoodListItem>() {
    override val layoutId: Int = R.layout.fragment_food_like_list
    override val recyclerView: RecyclerView get() = binding.likeRecyclerView
    private val serviceType: ServiceType by lazy { arguments.getServiceType() }
    private val deliveryType: DeliveryType by lazy { arguments.getDeliveryType() }
    private val shopAPI: ShopAPI by inject()
    private val likeSortTypeInputList: List<Selection<LikeSortType>> by lazy {
        LikeSortType.values().map { Selection(it.desc(), it) }
    }
    private val likeSortTypeSelectionDialog: LikeSortTypeSelectionDialog by lazy {
        LikeSortTypeSelectionDialog(
            likeSortTypeInputList
        )
    }
    private var selectedLikeSortType: LikeSortType = LikeSortType.RECENT_ADDED
    private var likeCount: Int by Delegates.observable(0) { _, _, newValue ->
        with(binding) {
            couponAmount.text = "총 ${newValue}개"
            if (newValue <= 0) notExistLikeInclude.root.isVisible = true
        }
    }

    override fun getCall(): Call<List<LikedFoodListItem>> {
        return shopAPI.getLikedFoodList(
            page,
            15,
            loginInfo?.id ?: throw IllegalStateException("비정상적인 접근입니다."),
            deliveryType.id,
            when (binding.normalMenuForm.likeSortTypeTextView.text.toString()) {
                "최근 추가한 순" -> LikeSortType.RECENT_ADDED.id
                "최근 주문한 순" -> LikeSortType.RECENT_ORDER.id
                else -> null
            }
        )
    }

    override fun makeAdapter(): FoodLikeListAdapter = FoodLikeListAdapter(deliveryType.id)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            safeApiRequest(
                shopAPI.getMemberLikeCount(
                    innerLoginInfo.getFormedToken(),
                    innerLoginInfo.id,
                    serviceType.id,
                    deliveryType.id
                )
            ) {
                likeCount = it.likeCount
            }
            likeSortTypeSelectionDialog.setOnReceiveData(this@FoodLikeListFragment) {
                if (it == null) return@setOnReceiveData
                selectedLikeSortType = it
                normalMenuForm.likeSortTypeTextView.text = it.desc()
                refresh()
            }
            normalMenuForm.likeSortTypeTextView.setOnClickListener {
                likeSortTypeSelectionDialog.setDefault(selectedLikeSortType)
                likeSortTypeSelectionDialog.show(childFragmentManager, "")
            }
            normalMenuForm.editButton.setOnClickListener {
                normalMenuForm.root.isVisible = false
                editorMenuForm.root.isVisible = true
                recyclerViewAdapter.toggleEditMode(true)
            }
            editorMenuForm.cancelButton.setOnClickListener {
                normalMenuForm.root.isVisible = true
                editorMenuForm.root.isVisible = false
                recyclerViewAdapter.toggleEditMode(false)
            }
            editorMenuForm.removeButton.setOnClickListener {
                showMessageDialog("정말 삭제하시겠어요?", showCancelButton = true) {
                    onDoneButtonClick(buttonText = "삭제하기") {
                        val selectedItems = recyclerViewAdapter.getSelectedItems()
                        safeApiRequest(
                            shopAPI.removeLikedShopList(
                                innerLoginInfo.getFormedToken(),
                                innerLoginInfo.id,
                                selectedItems.map { it.id }
                            )
                        )
                        likeCount -= selectedItems.size
                        recyclerViewAdapter.removeSelectedItems()
                        view.showMessageBar("찜목록에서 정상적으로 삭제되었어요.")
                        dismiss()
                        normalMenuForm.root.isVisible = true
                        editorMenuForm.root.isVisible = false
                        recyclerViewAdapter.toggleEditMode(false)
                    }
                }
            }
            editorMenuForm.allSelectCheckBox.setOnCheckedChangeListener { _, isChecked ->
                recyclerViewAdapter.toggleAllEditCheckBox(isChecked)
            }
            recyclerViewAdapter.setOnItemClick { _, _, item ->
                val action =
                    LikeListFragmentDirections.actionMyWishListFragmentToShopDetailsFragment(
                        item.id,
                        serviceType.id,
                        deliveryType.id
                    )
                findNavController().navigate(action)
            }
//            viewLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
//                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                    when (event) {
//                        Lifecycle.Event.ON_RESUME -> refresh()
//                        else -> {}
//                    }
//                }
//            })
        }
    }
}