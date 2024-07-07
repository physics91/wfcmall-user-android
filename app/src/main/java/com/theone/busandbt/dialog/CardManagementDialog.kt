package com.theone.busandbt.dialog

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theone.busandbt.R
import com.theone.busandbt.adapter.card.CardListAdapter
import com.theone.busandbt.api.orderchannel.CardAPI
import com.theone.busandbt.databinding.DialogCardManagementBinding
import com.theone.busandbt.dto.card.CardInfoListItem
import com.theone.busandbt.eventbus.RemoveCardEvent
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.showMessageDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject

class CardManagementDialog : DataBindingBottomDialog<DialogCardManagementBinding>() {
    override val layoutId: Int = R.layout.dialog_card_management
    private val cardAPI: CardAPI by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val activeDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_indicator_active)
            val inactiveDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_indicator_inactive)
            val innerLoginInfo = loginInfo ?: error("부적절한 접근")
            safeApiRequest(
                cardAPI.getCardList(
                    innerLoginInfo.getFormedToken(),
                    innerLoginInfo.id
                )
            ) {
                val modifiedList = mutableListOf<CardInfoListItem>()
                modifiedList.add(CardInfoListItem(0, 0, "0"))
                modifiedList.addAll(it)
                repeat(modifiedList.size) {
                    indicatorContainer.addView(createDotView(inactiveDrawable))
                }
                cardList.adapter = CardListAdapter().apply {
                    addItems(modifiedList)
                    setOnItemClick { _, _, item ->
                        if (item.type == 0) findNavController().navigate(R.id.card_agree_graph)
                    }
                    val snapHelper = PagerSnapHelper()
                    snapHelper.attachToRecyclerView(cardList)
                }
            }

            cardList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val activePosition =
                        (cardList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    for (i in 0 until indicatorContainer.childCount) {
                        val dot = indicatorContainer.getChildAt(i)
                        dot.background =
                            if (i == activePosition) activeDrawable else inactiveDrawable
                    }
                }
            })
            exitBtn.setOnClickListener {
                dismiss()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun removeCard(event: RemoveCardEvent) {
        with(binding) {
            showMessageDialog(
                "선택하신 카드를 삭제하시겠어요?",
                event.card.cardNo,
                showWarningImageView = false,
                showCancelButton = true
            ) {
                onDoneButtonClick {
                    val innerLoginInfo = loginInfo ?: return@onDoneButtonClick
                    safeApiRequest(
                        cardAPI.removeCard(innerLoginInfo.getFormedToken(), event.card.id),
                        onFail = { _, _ ->
                            showMessageDialog(
                                "카드 삭제에 실패하였어요.",
                                "다시 한 번 시도해 주세요.",
                                showWarningImageView = true,
                                showCancelButton = true
                            ) { onDoneButtonClick { dismiss() } }
                        }
                    ) {
                        dismiss()
                        view?.showMessageBar("카드가 정상적으로 삭제되었어요.")
                        val inactiveDrawable = ContextCompat.getDrawable(
                            root.context,
                            R.drawable.bg_indicator_inactive
                        )
                        val adapter =
                            cardList.adapter as? CardListAdapter ?: return@safeApiRequest
                        with(adapter) {
                            remove(event.card)
                            indicatorContainer.removeAllViews()
                            for (i in 0 until adapter.itemCount) {
                                indicatorContainer.addView(createDotView(inactiveDrawable))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createDotView(backgroundDrawable: Drawable?): View {
        val dot = View(context)
        dot.background = backgroundDrawable
        dot.layoutParams =
            LinearLayout.LayoutParams(SizeUtils.dp2px(8F), SizeUtils.dp2px(8F)).apply {
                setMargins(SizeUtils.dp2px(8F), 0, 0, 0)
            }
        return dot
    }
}