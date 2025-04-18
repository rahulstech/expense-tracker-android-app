package dreammaker.android.expensetracker.ui.util

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

interface SelectionKeyProvider<T> {

    fun getSelectionKey(position: Int): T

    fun count(): Int

    fun updateSelectionState(key: T, selected: Boolean)
}

enum class SelectionMode {
    SINGLE, MULTIPLE,
}

class SelectionStore<T>(val selectionMode: SelectionMode = SelectionMode.SINGLE) {

    private val TAG = SelectionStore::class.simpleName

    var selectionProvider: SelectionKeyProvider<T>? = null
    var selectedKey: T? = null
    var selectedKeys: HashSet<T>? = null
    private var keyPositions = HashMap<T, Int>()

    var itemSelectionListener: ((SelectionStore<T>, T, Int, Boolean)->Unit)? = null

    init {
        if (selectionMode == SelectionMode.MULTIPLE) {
            selectedKeys = HashSet()
        }
    }

    fun updateKeyPosition() {
        if (null == selectionProvider) {
            throw NullPointerException("SelectionKeyProvider not set")
        }
        val count = selectionProvider!!.count()
        val keyPositions = HashMap<T, Int>()
        (0..<count).forEach { position ->
            val key = selectionProvider!!.getSelectionKey(position)
            keyPositions[key] = position
        }
        this.selectedKeys?.clear()
        this.keyPositions = keyPositions
    }

    fun getPosition(key: T?): Int = keyPositions[key] ?: -1

    fun changeSelection(key: T, selected: Boolean): Boolean {
        val selectionMode = this.selectionMode
        if (selectionMode == SelectionMode.SINGLE) {
            val oldKey = selectedKey
            if (selected) {
                selectedKey = key
                selectionProvider?.updateSelectionState(key, true)
            }
            else {
                selectedKey = null
                selectionProvider?.updateSelectionState(key, false)
            }
            if (oldKey != key && null != oldKey) {
                selectionProvider?.updateSelectionState(oldKey, false)
            }
        }
        else {
            if (selected == isSelected(key)) return false
            val selectedKeys = this.selectedKeys
            if (selected) {
                selectedKeys?.add(key)
                selectionProvider?.updateSelectionState(key, true)
            }
            else {
                selectedKeys?.remove(key)
                selectionProvider?.updateSelectionState(key, false)
            }
        }
        itemSelectionListener?.invoke(this, key, getPosition(key), selected)
        return true
    }

    fun toggleSelection(key: T) {
        val selected = isSelected(key)
        if (selectionMode == SelectionMode.SINGLE) {
            if (selectedKey != key) {
                changeSelection(key, !selected)
            }
        }
        else {
            changeSelection(key, !selected)
        }
    }

    fun isSelected(key: T): Boolean {
        return key == selectedKey
    }

    fun hasSelection(): Boolean {
        return if (selectionMode == SelectionMode.SINGLE) {
            null != selectedKey
        }
        else {
            selectedKeys?.isNotEmpty() ?: false
        }
    }
}

open class ClickableViewHolder<VH : ViewHolder>(itemView: View, val onClick: ((VH, View)->Unit)?): ViewHolder(itemView) {

    @Suppress("UNCHECKED_CAST")
    fun attachItemClickListener() {
        itemView.setOnClickListener{ onClick?.invoke(this as VH, it)}
    }

    fun detachItemClickListener() {
        itemView.setOnClickListener(null)
    }
}

interface IClickableItemAdapter<T, VH : ClickableViewHolder<VH>> {
    var itemClickListener: ((RecyclerView.Adapter<VH>, View, Int) -> Unit)?

    fun handleItemClick(holder: VH, view: View)
}

interface ISelectableItemAdapter<KeyType> : SelectionKeyProvider<KeyType> {

    var selectionStore: SelectionStore<KeyType>?

    fun toggleSelection(position: Int) {
        val key = getSelectionKey(position)
        selectionStore?.toggleSelection(key)
    }

    fun updateKeyPositions() {
        selectionStore?.updateKeyPosition()
    }

    fun isSelected(position: Int): Boolean {
        val key = getSelectionKey(position)
        return selectionStore?.isSelected(key) ?: false
    }

    override fun updateSelectionState(key: KeyType, selected: Boolean) {
        val position = selectionStore?.getPosition(key) ?: -1
        updateSelectionState(position, selected)
    }

    fun updateSelectionState(position: Int, selected: Boolean)
}

abstract class BaseClickableItemListAdapter<T, VH : ClickableViewHolder<VH>>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T,VH>(callback), IClickableItemAdapter<T,VH> {

    override var itemClickListener: ((RecyclerView.Adapter<VH>, View, Int)->Unit)? = null

    override fun handleItemClick(holder: VH, view: View) {
        val position = holder.bindingAdapterPosition
        itemClickListener?.invoke(this, view, position)
    }
}

abstract class BaseSelectableItemListAdapter<ItemType, KeyType, VH : ClickableViewHolder<VH>>(callback: DiffUtil.ItemCallback<ItemType>)
    : BaseClickableItemListAdapter<ItemType,VH>(callback), ISelectableItemAdapter<KeyType> {

    override var selectionStore: SelectionStore<KeyType>? = null

    override fun submitList(list: List<ItemType>?) = this.submitList(list, null)

    override fun submitList(list: List<ItemType>?, commitCallback: Runnable?) {
        super.submitList(list) {
            updateKeyPositions()
            commitCallback?.run()
        }
    }

    override fun count(): Int = itemCount

    override fun updateSelectionState(position: Int, selected: Boolean) {
        notifyItemChanged(position)
    }

    override fun handleItemClick(holder: VH, view: View) {
        val position = holder.bindingAdapterPosition
        toggleSelection(position)
        super.handleItemClick(holder, view)
    }
}