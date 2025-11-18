package dreammaker.android.expensetracker.util

import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dreammaker.android.expensetracker.ui.main.ContextualActionBarViewModel
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
    SINGLE,
    MULTIPLE
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





class SelectionViewModel: ViewModel() {

    private var keys: List<Any> = emptyList()
    var inSelectionMode: Boolean = false
    var cabMode: Boolean = false

    @Suppress("UNCHECKED_CAST")
    fun <T> getSelectionKeys(): List<T> = keys as List<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> setSelectionKeys(keys: List<T>) {
        this.keys = keys as List<Any>
    }
}

class SelectionHelper<KeyType>(
    private val adapter: ISelectableItemAdapter2<KeyType>,
    private val selectionTrackerBuilderProvider: ()->SelectionTracker.Builder<KeyType>
) {
    private var _inSelectionMode: Boolean = false
    val inSelectionMode: Boolean get() = _inSelectionMode
    private var cabMode: Boolean = false
    private val selectionTracker: SelectionTracker<KeyType>? get() = adapter.selectionTracker

    var itemClickListener: ItemClickListener? = null
    private val selectableItemClickListener: ItemClickListener = { adapter,itemView,position ->
        if (inSelectionMode) {
            toggleItemSelectionAtPosition(position)
        }
        else {
            itemClickListener?.invoke(adapter,itemView,position)
        }
    }

    // SelectionViewModel

    private var selectionVM: SelectionViewModel? = null

    fun bindViewModelStore(owner: ViewModelStoreOwner) {
        selectionVM = ViewModelProvider(owner)[SelectionViewModel::class]
    }

    // Lifecycle

    private val lifecycleObserver: LifecycleObserver = object: DefaultLifecycleObserver {

        override fun onResume(owner: LifecycleOwner) { onLifecycleResume() }

        override fun onPause(owner: LifecycleOwner) { onLifecyclePause() }
    }

    fun bindLifecycle(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(lifecycleObserver)
    }

    private fun onLifecycleResume() {
        setSelections(selectionVM?.getSelectionKeys() ?: emptyList())
        selectionVM?.let { vm ->
            _inSelectionMode = vm.inSelectionMode
            cabMode = vm.cabMode
            if (_inSelectionMode) {
                setSelections(vm.getSelectionKeys())
                showContextualActionBar()
            }
        }
    }

    private fun onLifecyclePause() {
        selectionVM?.let { vm ->
            vm.inSelectionMode = _inSelectionMode
            vm.cabMode = cabMode
            if (_inSelectionMode) {
                vm.setSelectionKeys(getSelections())
                hideContextualActionBar()
            }
        }
    }

    // ContextualActionBar

    private var cabVM: ContextualActionBarViewModel? = null
    private var cabMenuProvider: MenuProvider? = null

    fun prepareContextualActionBar(activity: FragmentActivity, menuProvider: MenuProvider? = null) {
        cabVM = ViewModelProvider(activity)[ContextualActionBarViewModel::class]
        cabMenuProvider = menuProvider
    }

    private fun startContextualActionBar() {
        if (inSelectionMode) {
            cabVM?.let { vm ->
                vm.startContextualActionBar(cabMenuProvider)
                cabMode = true
            }
        }
    }

    private fun showContextualActionBar() {
        if (inSelectionMode && cabMode) {
            cabVM?.showContextActionBar()
        }
    }

    private fun hideContextualActionBar() {
        if (cabMode) {
            cabVM?.hideContextActionBar()
        }
    }

    private fun endContextualActionBar() {
        if (cabMode) {
            cabVM?.endContextActionBar()
        }
    }

    // Selection

    fun startSelection(
        predicate: SelectionTracker.SelectionPredicate<KeyType>,
        contextualActionBar: Boolean = false,
        onStart: ((SelectionTracker<KeyType>)->Unit)? = null
    ): Boolean {
        if (inSelectionMode) return false

        val selectionTracker: SelectionTracker<KeyType> = selectionTrackerBuilderProvider().apply {
            withSelectionPredicate(predicate)
        }
            .build()
        adapter.selectionTracker = selectionTracker
        adapter.itemClickListener = selectableItemClickListener
        _inSelectionMode = true
        if (contextualActionBar) {
            startContextualActionBar()
            showContextualActionBar()
        }
        onStart?.invoke(selectionTracker)
        return true
    }

    fun getSelectionKey(position: Int): KeyType? = adapter.getSelectionKey(position)

    fun selectItem(key: KeyType?) {
        key?.let{ selectionTracker?.select(it) }
    }

    @Deprecated(message = "use unselectItem(key)", replaceWith = ReplaceWith("unselectItem(key)"))
    fun deselectItem(key: KeyType?) {
        unselectItem(key)
    }

    fun unselectItem(key: KeyType?) {
        key?.let{ selectionTracker?.deselect(it) }
    }

    fun isSelected(key: KeyType): Boolean = selectionTracker?.isSelected(key) == true

    fun toggleItemSelection(key: KeyType) {
        if (isSelected(key)) {
            unselectItem(key)
        }
        else {
            selectItem(key)
        }
    }

    fun toggleItemSelectionAtPosition(position: Int) {
        getSelectionKey(position)?.let { toggleItemSelection(it) }
    }

    fun count(): Int = selectionTracker?.selection?.size() ?: 0

    fun getSelections(): List<KeyType> {
        return selectionTracker?.selection?.toList() ?: emptyList()
    }

    fun setSelections(selectedKeys: List<KeyType>?) {
        selectedKeys?.let { keys ->
            selectionTracker?.setItemsSelected(keys,true)
            val positions = keys.map { key -> adapter.getKeyPosition(key) }
            adapter.notifySelectionChanged(positions)
        }
    }

    fun endSelection() {
        hideContextualActionBar()
        endContextualActionBar()
        _inSelectionMode = false
        selectionTracker?.clearSelection()
        adapter.itemClickListener = itemClickListener
        adapter.selectionTracker = null
    }
}

// ViewHolder and Adapter

open class BaseViewHolder(itemView: View): ViewHolder(itemView) {

    val context: Context = itemView.context

    fun getString(@StringRes id: Int, vararg args: Any) = context.getString(id,*args)
}

open class ClickableViewHolder<VH : ViewHolder>(itemView: View): BaseViewHolder(itemView) {

    @Suppress("UNCHECKED_CAST")
    fun attachItemClickListener(clickListener: ((VH,View)->Unit)?) {
        itemView.setOnClickListener{ clickListener?.invoke(this as VH, it)}
    }

    @Suppress("UNCHECKED_CAST")
    fun attachItemLongClickListener(longClickListener: ((VH,View)-> Boolean)?) {
        itemView.setOnLongClickListener { longClickListener?.invoke(this as VH,it) == true }
    }
}

typealias ItemClickListener = (RecyclerView.Adapter<*>,View,Int)->Unit

typealias ItemLongClickListener = (RecyclerView.Adapter<*>,View,Int)->Boolean

interface IClickableItemAdapter<T, VH : ClickableViewHolder<VH>> {
    var itemClickListener: ItemClickListener?

    var itemLongClickListener: ItemLongClickListener?

    fun handleItemClick(holder: VH, view: View)

    fun handleItemLongClick(holder: VH, view: View): Boolean
}

interface ISelectableItemAdapter2<KeyType> {
    var selectionTracker: SelectionTracker<KeyType>?

    var itemClickListener: ItemClickListener?

    fun getSelectionKey(position: Int): KeyType?

    fun getKeyPosition(key: KeyType): Int

    fun notifySelectionChanged(positions: List<Int>)
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

    override var itemClickListener: ItemClickListener? = null

    override var itemLongClickListener: ItemLongClickListener? = null

    override fun handleItemClick(holder: VH, view: View) {
        val position = holder.bindingAdapterPosition
        itemClickListener?.invoke(this, view, position)
    }

    override fun handleItemLongClick(holder: VH, view: View): Boolean {
        val position = holder.bindingAdapterPosition
        return itemLongClickListener?.invoke(this, view, position) == true
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

abstract class BaseSelectableItemListAdapter2<ItemType, KeyType, VH: ClickableViewHolder<VH>>(callback: DiffUtil.ItemCallback<ItemType>)
    : BaseClickableItemListAdapter<ItemType,VH>(callback), ISelectableItemAdapter2<KeyType> {

    override var selectionTracker: SelectionTracker<KeyType>? = null

    private var _mapKeyPosition: Map<KeyType,Int> = emptyMap()

    override fun submitList(list: List<ItemType>?) = this.submitList(list, null)

    override fun submitList(list: List<ItemType>?, commitCallback: Runnable?) {
        super.submitList(list) {
            updateKeyPositions()
            commitCallback?.run()
        }
    }

    protected fun updateKeyPositions() {
        val map = mutableMapOf<KeyType,Int>()
        currentList.forEachIndexed { pos,_ ->
            getSelectionKey(pos)?.let { key ->
                map[key] = pos
            }
        }
        _mapKeyPosition = map
    }

    override fun getKeyPosition(key: KeyType): Int = _mapKeyPosition[key] ?: RecyclerView.NO_POSITION

    fun isSelected(position: Int): Boolean {
        return getSelectionKey(position)?.let { selectionTracker?.isSelected(it) } == true
    }

    override fun notifySelectionChanged(positions: List<Int>) {
        positions.forEach { notifyItemChanged(it) }
    }
}