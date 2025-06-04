package dreammaker.android.expensetracker.util

import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

// Item Selection

interface SelectionKeyProvider<T> {

    fun getSelectionKey(position: Int): T

    fun count(): Int

    fun updateSelectionState(key: T, selected: Boolean)
}

enum class SelectionMode {
    SINGLE, MULTIPLE
}


abstract class SelectionStoreViewModel<T>: ViewModel() {

    var selectedKey: T? = null
    var selectedKeys: HashSet<T>? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var keyPositionUpdateJob: Job? = null

    fun updateKeyPosition(selectionProvider: SelectionKeyProvider<T>, collector: (result: Map<T,Int>)->Unit) {
        keyPositionUpdateJob?.cancel()

        scope.launch {
            val count = selectionProvider.count()
            val keyPositions = HashMap<T, Int>()
            (0..<count).forEach { position ->
                val key = selectionProvider.getSelectionKey(position)
                keyPositions[key] = position
            }

            withContext(Dispatchers.Main) {
                collector(keyPositions)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        keyPositionUpdateJob?.cancel()
        scope.cancel()
    }
}

class SelectionStore<T>(val selectionMode: SelectionMode = SelectionMode.SINGLE,
                        provider: SelectionKeyProvider<T>,
                        private val viewModel: SelectionStoreViewModel<T>
) {

    private val TAG = SelectionStore::class.simpleName

    private var selectionProviderReference = WeakReference(provider)
    private val selectionProvider: SelectionKeyProvider<T>?
        get() = selectionProviderReference.get()

    val selectedKey: T?
        get() = viewModel.selectedKey

    val selectedKeys: Set<T>?
        get() = viewModel.selectedKeys

    private var keyPositions: Map<T,Int> = emptyMap()

    private var itemSelectionListenerRef = WeakReference<((SelectionStore<T>, T, Int, Boolean)->Unit)?>(null)
    var itemSelectionListener: ((SelectionStore<T>, T, Int, Boolean)->Unit)?
        get() = itemSelectionListenerRef.get()
        set(value) { itemSelectionListenerRef = WeakReference(value) }

    init {
        if (selectionMode == SelectionMode.MULTIPLE) {
            viewModel.selectedKeys = HashSet()
        }
    }

    fun updateKeyPosition() {
        selectionProvider?.let { provider ->
            viewModel.updateKeyPosition(provider) { this.keyPositions = it }
        }
    }

    fun getPosition(key: T?): Int = keyPositions[key] ?: -1

    private fun addSelectedKey(key: T) {
        if (selectionMode == SelectionMode.SINGLE) {
            viewModel.selectedKey = key
        }
        else {
            viewModel.selectedKeys?.add(key)
        }
    }

    private fun removeSelectedKey(key: T) {
        if (selectionMode == SelectionMode.SINGLE) {
            viewModel.selectedKey = null
        }
        else {
            viewModel.selectedKeys?.remove(key)
        }
    }

    fun changeSelection(key: T, selected: Boolean): Boolean {
        val selectionMode = this.selectionMode
        if (selectionMode == SelectionMode.SINGLE) {
            val oldKey = selectedKey
            if (selected) {
                addSelectedKey(key)
                selectionProvider?.updateSelectionState(key, true)
            }
            else {
                removeSelectedKey(key)
                selectionProvider?.updateSelectionState(key, false)
            }
            if (oldKey != key && null != oldKey) {
                selectionProvider?.updateSelectionState(oldKey, false)
            }
        }
        else {
            if (selected == isSelected(key)) return false
            if (selected) {
                addSelectedKey(key)
                selectionProvider?.updateSelectionState(key, true)
            }
            else {
                removeSelectedKey(key)
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
        return if (selectionMode == SelectionMode.SINGLE) {
            key == selectedKey
        }
        else {
            selectedKeys?.contains(key) ?: false
        }
    }

    fun hasSelection(): Boolean {
        return if (selectionMode == SelectionMode.SINGLE) {
            null != selectedKey
        }
        else {
            selectedKeys?.isNotEmpty() ?: false
        }
    }

    fun setInitialKey(key: T?) {
        Log.d(TAG, "initial-key=${key}")
        if (selectionMode == SelectionMode.SINGLE) {
            viewModel.selectedKey = key
        }
    }

    fun setInitialKeys(keys: Collection<T>) {
        Log.d(TAG,"initial-keys=${keys}")
        if (!hasSelection() && selectionMode == SelectionMode.MULTIPLE) {
            viewModel.selectedKeys = HashSet(keys)
        }
    }

    fun clearSelection() {
        if (selectionMode == SelectionMode.SINGLE) {
            viewModel.selectedKey = null
        }
        else {
            viewModel.selectedKeys?.clear()
        }
    }
}

// ViewHolder and Adapter

open class BaseViewHolder(itemView: View): ViewHolder(itemView) {

    val context: Context = itemView.context

    fun getString(@StringRes id: Int, vararg args: Any) = context.getString(id,*args)
}

open class ClickableViewHolder<VH : ViewHolder>(itemView: View): BaseViewHolder(itemView) {

    private var clickListener: ((VH,View)->Unit)? = null

    private var longClickListener: ((VH,View)->Boolean)? = null

    fun setItemClickListener(listener: ((VH,View)->Unit)?) {
        clickListener = listener
    }

    fun setLongClickListener(listener: ((VH, View) -> Boolean)?) {
        longClickListener = listener
    }

    @Suppress("UNCHECKED_CAST")
    fun attachItemClickListener() {
        itemView.setOnClickListener{ clickListener?.invoke(this as VH, it)}
    }

    fun detachItemClickListener() {
        itemView.setOnClickListener(null)
    }
}

interface IClickableItemAdapter<T, VH : ClickableViewHolder<VH>> {
    var itemClickListener: ((RecyclerView.Adapter<VH>, View, Int)->Unit)?

    var itemLongClickListener: ((RecyclerView.Adapter<VH>,View,Int)->Boolean)?

    fun handleItemClick(holder: VH, view: View)

    fun handleItemLongClick(holder: VH, view: View): Boolean
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
        val position = selectionStore?.getPosition(key) ?: RecyclerView.NO_POSITION
        updateSelectionState(position, selected)
    }

    fun updateSelectionState(position: Int, selected: Boolean)
}

abstract class BaseClickableItemListAdapter<T, VH : ClickableViewHolder<VH>>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T,VH>(callback), IClickableItemAdapter<T, VH> {

    override var itemClickListener: ((RecyclerView.Adapter<VH>, View, Int)->Unit)? = null

    override var itemLongClickListener: ((RecyclerView.Adapter<VH>, View, Int) -> Boolean)? = null

    override fun handleItemClick(holder: VH, view: View) {
        val position = holder.bindingAdapterPosition
        itemClickListener?.invoke(this, view, position)
    }

    override fun handleItemLongClick(holder: VH, view: View): Boolean {
        val position = holder.bindingAdapterPosition
        return itemLongClickListener?.invoke(this, view, position) ?: false
    }
}

abstract class BaseSelectableItemListAdapter<ItemType, KeyType, VH : ClickableViewHolder<VH>>(callback: DiffUtil.ItemCallback<ItemType>)
    : BaseClickableItemListAdapter<ItemType, VH>(callback), ISelectableItemAdapter<KeyType> {

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
