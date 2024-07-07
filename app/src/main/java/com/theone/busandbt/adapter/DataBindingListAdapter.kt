package com.theone.busandbt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.fragment.SingleListViewHolder
import com.theone.busandbt.utils.OnItemClick
import kotlin.math.min

abstract class DataBindingListAdapter<ViewHolderBinding : ViewDataBinding, Item> :
    RecyclerView.Adapter<SingleListViewHolder<ViewHolderBinding, Item>>() {

    /**
     * 데이터 리스트를 이 인스턴스로 관리한다.
     */
    protected val _items = mutableListOf<Item>()
    val items: List<Item> get() = _items

    /**
     * 데이터 리스트 각각의 항목들을 적용할 레이아웃 id를 정의한다.
     * R.layout 형식으로 입력한다.
     */
    protected abstract val viewHolderLayoutId: Int

    /**
     * 항목들을 클릭 했을때 일어날 로직을 정의한다.
     * 기본적으로는 클릭 했을때 아무런 일도 일어나지 않는다.
     * TODO 나중에 고정 변수가 아닌 유동적인 로직으로 변경이 필요
     */
    private var onItemClick: OnItemClick<Item>? = null
    open var beforeItemClick: OnItemClick<Item>? = null

    override fun getItemCount(): Int = _items.size
    override fun getItemId(position: Int): Long = position.toLong()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SingleListViewHolder<ViewHolderBinding, Item> {
        val binding =
            DataBindingUtil.inflate<ViewHolderBinding>(
                LayoutInflater.from(parent.context),
                viewHolderLayoutId,
                parent,
                false
            )
        return object : SingleListViewHolder<ViewHolderBinding, Item>(binding) {
            override fun bind(item: Item, position: Int, payloads: MutableList<Any>) {
                doBind(binding, item, position, payloads)
            }
        }
    }

    override fun onBindViewHolder(
        holder: SingleListViewHolder<ViewHolderBinding, Item>,
        position: Int
    ) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        holder: SingleListViewHolder<ViewHolderBinding, Item>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = _items[position]
        with(holder) {
            bind(item, position, payloads)
            itemView.setOnClickListener {
                beforeItemClick?.invoke(it, position, item)
                onItemClick?.invoke(it, position, item)
            }
        }
    }

    /**
     * ViewHolder와 연결될 때 실행될 로직을 정의한다.
     */
    protected abstract fun doBind(
        binding: ViewHolderBinding,
        item: Item,
        position: Int,
        payloads: MutableList<Any>
    )

    fun setOnItemClick(onItemClick: OnItemClick<Item>?) {
        this.onItemClick = onItemClick
    }

    open fun clear() {
        val size = _items.size
        _items.clear()
        notifyItemRangeRemoved(0, size)
    }

    open fun addItems(items: Collection<Item>) {
        val start = _items.size
        this._items.addAll(items)
        notifyItemRangeInserted(start, items.size)
    }

    open fun addItem(item: Item) {
        this._items.add(item)
        notifyItemInserted(_items.size - 1)
    }

    open fun setItems(items: Collection<Item>) {
        val oldSize = _items.size
        val newSize = items.size
        this._items.clear()
        this._items.addAll(items)
        notifyItemRangeChanged(0, min(newSize, oldSize))
        if (oldSize > newSize) notifyItemRangeRemoved(newSize, oldSize - newSize)
        if (newSize > oldSize) notifyItemRangeInserted(oldSize, newSize - oldSize)
    }

    open fun remove(item: Item) {
        val index = _items.indexOf(item)
        if (index == -1) return
        _items.removeAt(index)
        notifyItemRemoved(index)
    }

    open fun removeList(items: Collection<Item>) {
        items.forEach { item ->
            val verifiedIndex = _items.indexOfFirst { old -> item == old }
            if (verifiedIndex == -1) return@forEach
            _items.removeAt(verifiedIndex)
            notifyItemRemoved(verifiedIndex)
        }
    }

    open fun refreshViews() {
        notifyItemRangeChanged(0, _items.size)
    }
}