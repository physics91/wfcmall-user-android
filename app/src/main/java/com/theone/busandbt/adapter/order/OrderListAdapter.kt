package com.theone.busandbt.adapter.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.busandbt.code.IdBaseType
import com.busandbt.code.OrderStatus
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemOrderListBinding
import com.theone.busandbt.databinding.ItemOrderProgressBinding
import com.theone.busandbt.dto.order.OrderListItem
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.toLocalDateTime
import com.theone.busandbt.fragment.order.OrderDetailFragmentArgs
import com.theone.busandbt.fragment.order.OrderListMainFragmentDirections
import com.theone.busandbt.fragment.order.OrderStatusFragmentArgs
import com.theone.busandbt.utils.ORDER_COMPLETE_STATUS_ARRAY
import com.theone.busandbt.utils.ORDER_PROCESS_STATUS_ID_ARRAY
import com.theone.busandbt.utils.OnItemClick
import java.time.LocalDateTime
import kotlin.math.min

/**
 * 주문내역 음식점 어뎁터
 */
class OrderListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items = ArrayList<OrderListItem>()

    /**
     * 항목들을 클릭 했을때 일어날 로직을 정의한다.
     * 기본적으로는 클릭 했을때 아무런 일도 일어나지 않는다.
     * TODO 나중에 고정 변수가 아닌 유동적인 로직으로 변경이 필요
     */
    private var onItemClick: OnItemClick<OrderListItem>? = null

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is OrderListEtcViewHolder -> holder.bind(item)
            is OrderListProcessViewHolder -> holder.bind(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.PROCESS.id -> OrderListProcessViewHolder(
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_order_progress,
                    parent,
                    false
                )
            )

            ViewType.ETC.id -> OrderListEtcViewHolder(
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_order_list,
                    parent,
                    false
                )
            )

            else -> OrderListEtcViewHolder(
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_order_list,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].status) {
            in ORDER_PROCESS_STATUS_ID_ARRAY -> ViewType.PROCESS.id
            else -> ViewType.ETC.id
        }
    }

    private fun OrderStatus.label() = when (this) {
        OrderStatus.NEW_OR_RECEIVING -> "접수중"
        OrderStatus.START_DELIVERY -> "배달중"
        OrderStatus.COMPLETE_PACKAGING -> "포장완료"
        OrderStatus.CANCEL -> "주문취소"
        OrderStatus.START_COOKING -> "조리중"
        OrderStatus.COMPLETE_DELIVERY -> "배달완료"
        OrderStatus.COMPLETE_PICK_UP -> "고객픽업완료"
        else -> ""
    }

    fun getItem(orderId: String) = items.find { it.id == orderId }

    fun setOnItemClick(onItemClick: OnItemClick<OrderListItem>) {
        this.onItemClick = onItemClick
    }

    fun clear() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun addItemWithSort(item: OrderListItem) {
        this.items.add(item)
        val sorted = items.sortedByDescending { it.createDateTime }
        setItems(sorted)
    }

    fun addItems(items: List<OrderListItem>) {
        val position = this.items.size
        this.items.addAll(items)
        notifyItemRangeInserted(position, items.size)
    }

    fun setItems(items: List<OrderListItem>) {
        val oldSize = this.items.size
        val newSize = items.size
        this.items.clear()
        this.items.addAll(items)
        notifyItemRangeChanged(0, min(newSize, oldSize))
        if (oldSize > newSize) notifyItemRangeRemoved(newSize, oldSize - newSize)
        if (newSize > oldSize) notifyItemRangeInserted(oldSize, newSize - oldSize)
    }

    fun setOrderStatus(orderId: String, orderStatus: OrderStatus) {
        val index = items.indexOfFirst { it.id == orderId }
        if (index == -1) return
        val item = items[index]
        item.status = orderStatus.id
        notifyItemChanged(index)
    }

    fun writtenReviewOrder(orderId: String) {
        val index = items.indexOfFirst { it.id == orderId }
        if (index == -1) return
        val item = items[index]
        item.doWrittenReview = true
        notifyItemChanged(index)
    }

    fun remove(item: OrderListItem) {
        val index = items.indexOf(item)
        if (index == -1) return
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    inner class OrderListEtcViewHolder(private val binding: ItemOrderListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderListItem) {
            with(binding) {
                order = item
                val orderStatus = OrderStatus.find(item.status)
                logoImageView.clipToOutline = true
                orderStatusTextView.text = orderStatus.label()
                goOrderDetailButton.setOnClickListener {
                    it.navigate(
                        R.id.order_detail_graph,
                        OrderDetailFragmentArgs(item.id).toBundle()
                    )
                }
                if (orderStatus in ORDER_COMPLETE_STATUS_ARRAY) {
                    if (item.orderDoneDateTime != null) {
                        val now = LocalDateTime.now()
                        reviewWriteInclude.root.isVisible =
                            item.orderDoneDateTime.toLocalDateTime().plusDays(3)
                                .isAfter(now) && !item.doWrittenReview
                        if (reviewWriteInclude.root.isVisible) {
                            reviewWriteInclude.reviewWriteButton.setOnClickListener {
                                val action =
                                    OrderListMainFragmentDirections.actionOrderHistoryFragmentToReviewWriteFragment(
                                        item.id
                                    )
                                it.findNavController().navigate(action)
                            }
                        } else {
                            reviewWriteInclude.reviewWriteButton.setOnClickListener(null)
                        }
                    } else {
                        reviewWriteInclude.root.isVisible = false
                    }
                } else {
                    reviewWriteInclude.root.isVisible = false
                }
            }
        }
    }

    inner class OrderListProcessViewHolder(private val binding: ItemOrderProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderListItem) {
            with(binding) {
                order = item
                val orderStatus = OrderStatus.find(item.status)
                logoImageView.clipToOutline = true
                orderStatusTextView.text = orderStatus.label()
                goOrderDetailButton.setOnClickListener {
                    it.navigate(
                        R.id.order_detail_graph,
                        OrderDetailFragmentArgs(item.id).toBundle()
                    )
                }
                goOrderStatusButton.setOnClickListener {
                    it.navigate(
                        R.id.order_status_graph,
                        OrderStatusFragmentArgs(item.id).toBundle()
                    )
                }
            }
        }
    }

    enum class ViewType(override val id: Int) : IdBaseType {
        PROCESS(1),
        ETC(2),
        ;
    }
}