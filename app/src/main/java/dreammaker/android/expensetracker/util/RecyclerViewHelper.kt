package dreammaker.android.expensetracker.util

import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dreammaker.android.expensetracker.ui.main.ContextualActionBarViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Item Selection

class SelectionViewModel: ViewModel() {

    private var keys: List<Any> = emptyList()
    var inSelectionMode: Boolean = false
    var inCabMode: Boolean = false

    @Suppress("UNCHECKED_CAST")
    fun <T> getSelectionKeys(): List<T> = keys as List<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> setSelectionKeys(keys: List<T>) {
        this.keys = keys as List<Any>
    }
}

typealias ItemSelectionChangeCallback<KeyType> = (helper: SelectionHelper<KeyType>, key: KeyType, position: Int, selected: Boolean)->Unit

class SelectionHelper<KeyType>(
    private val adapter: ISelectableItemAdapter<KeyType>,
    vmsOwner: ViewModelStoreOwner,
    private val lcOwner: LifecycleOwner,
    private val selectionTrackerBuilderFactory: ()->SelectionTracker.Builder<KeyType>
) {
    private val TAG = SelectionHelper::class.simpleName

    private var inSelectionMode: Boolean = false
    private var inCabMode: Boolean = false
    private var selectionTracker: SelectionTracker<KeyType>? = null

    var itemClickListener: ItemClickListener? = null
    private val selectableItemClickListener: ItemClickListener = { adapter,itemView,position ->
        if (inSelectionMode) {
            toggleItemSelectionAtPosition(position)
        }
        else {
            itemClickListener?.invoke(adapter,itemView,position)
        }
    }

    var itemSelectionChangeCallback: ItemSelectionChangeCallback<KeyType>? = null
    private val selectionTrackerObserver = object: SelectionTracker.SelectionObserver<KeyType>() {
        override fun onItemStateChanged(key: KeyType & Any, selected: Boolean) {
            itemSelectionChangeCallback?.invoke(this@SelectionHelper,key,adapter.getKeyPosition(key),selected)
        }
    }

    private var selectionVM: SelectionViewModel = ViewModelProvider(vmsOwner)[SelectionViewModel::class]

    init {
        adapter.selectionHelper = this
        adapter.itemClickListener = selectableItemClickListener
        lcOwner.lifecycle.addObserver(object: DefaultLifecycleObserver {

            override fun onResume(owner: LifecycleOwner) { onLifecycleResume() }

            override fun onPause(owner: LifecycleOwner) { onLifecyclePause() }
        })
    }

    private fun onLifecycleResume() {
        Log.d(TAG,"bound lifecycle resumed")
        selectionVM.let { vm ->
            val inSelectionMode = vm.inSelectionMode
            val inCabMode = vm.inCabMode
            if (inSelectionMode) {
                startSelection(inCabMode)
                setSelections(vm.getSelectionKeys())
            }
        }
    }

    private fun onLifecyclePause() {
        Log.d(TAG,"bound lifecycle paused")

        // hideContextualActionBar() will reset following value,
        // therefore caching these values before calling the method
        val inSelectionMode = this.inSelectionMode
        val inCabMode = this.inCabMode
        val selections = getSelections()

        hideContextualActionBar()
        selectionVM.let { vm ->
            vm.inSelectionMode = inSelectionMode
            vm.inCabMode = inCabMode
            vm.setSelectionKeys(selections)
        }
    }

    // ContextualActionBar

    private var _cabViewModel: ContextualActionBarViewModel? = null
    val cabViewModel: ContextualActionBarViewModel? get() = _cabViewModel
    private var cabMenuProvider: MenuProvider? = null

    fun prepareContextualActionBar(activity: FragmentActivity, menuProvider: MenuProvider? = null) {
        cabMenuProvider = menuProvider
        _cabViewModel = ViewModelProvider(activity)[ContextualActionBarViewModel::class]
        lcOwner.lifecycleScope.launch {
            _cabViewModel!!.cabStartState
                .collectLatest { start ->
                    if (!start)
                        endSelection()
                }
        }
    }

    private fun showContextualActionbar() {
        Log.d(TAG,"showContextualActionBar inSelectionMode=$inSelectionMode")
        if (inSelectionMode) {
            _cabViewModel?.let { vm ->
                vm.startContextualActionBar(cabMenuProvider)
                inCabMode = true
            }
        }
    }

    private fun hideContextualActionBar() {
        Log.d(TAG,"hideContextualActionBar inCabMode=$inCabMode")
        if (inCabMode) {
            _cabViewModel?.let { vm ->
                if (vm.cabStartState.value) {
                    vm.endContextActionBar()
                }
                inCabMode = false
            }
        }
    }

    // Selection

    fun startSelection(
        contextualActionBar: Boolean = false,
        initialSelection: KeyType? = null,
        onStart: ((SelectionTracker<KeyType>)->Unit)? = null
    ): Boolean {
        if (inSelectionMode) return false

        val tracker = selectionTrackerBuilderFactory().build()
        tracker.addObserver(selectionTrackerObserver)
        selectionTracker = tracker
        inSelectionMode = true
        if (contextualActionBar) {
            showContextualActionbar()
        }
        selectItem(initialSelection)
        onStart?.invoke(selectionTracker!!)
        return true
    }

    fun getSelectionKey(position: Int): KeyType? = adapter.getSelectionKey(position)

    fun selectItem(key: KeyType?) {
        key?.let{ selectionTracker?.select(it) }
    }

    fun deselectItem(key: KeyType?) {
        key?.let{ selectionTracker?.deselect(it) }
    }

    fun isSelected(key: KeyType): Boolean = selectionTracker?.isSelected(key) == true

    fun toggleItemSelection(key: KeyType) {
        if (isSelected(key)) {
            deselectItem(key)
        }
        else {
            selectItem(key)
        }
    }

    fun toggleItemSelectionAtPosition(position: Int) {
        getSelectionKey(position)?.let { toggleItemSelection(it) }
    }

    fun count(): Int = selectionTracker?.selection?.size() ?: 0

    fun hasSelection(): Boolean = selectionTracker?.hasSelection() ?: false

    fun getSelections(): List<KeyType> =
        when(inSelectionMode) {
            true -> selectionTracker?.selection?.toList() ?: emptyList()
            else -> emptyList()
        }

    fun setSelections(keys: List<KeyType>) {
        selectionTracker?.setItemsSelected(keys,true)
        val positions = keys.map { key -> adapter.getKeyPosition(key) }
        adapter.notifySelectionChanged(positions)
    }

    fun endSelection() {
        if (!inSelectionMode) return

        hideContextualActionBar()
        inSelectionMode = false
        selectionTracker?.clearSelection()
        selectionTracker = null
    }
}

// ViewHolder and Adapter

open class BaseViewHolder(itemView: View): ViewHolder(itemView) {

    val context: Context = itemView.context

    fun getString(@StringRes id: Int, vararg args: Any) = context.getString(id,*args)
}

open class ClickableViewHolder(itemView: View): BaseViewHolder(itemView) {

    fun attachItemClickListener(clickListener: ((ViewHolder, View)->Unit)?) {
        itemView.setOnClickListener{ clickListener?.invoke(this, it)}
    }

    fun attachItemLongClickListener(longClickListener: ((ViewHolder, View)-> Boolean)?) {
        itemView.setOnLongClickListener { longClickListener?.invoke(this,it) == true }
    }
}

open class SelectableViewHolder<KeyType>(itemView: View): ClickableViewHolder(itemView) {

    open fun getSelectedItemDetails(): ItemDetailsLookup.ItemDetails<KeyType?>? = null
}

typealias ItemClickListener = (RecyclerView.Adapter<*>,View,Int)->Unit

typealias ItemLongClickListener = (RecyclerView.Adapter<*>,View,Int)->Boolean

interface IClickableItemAdapter<VH : ClickableViewHolder> {
    var itemClickListener: ItemClickListener?

    var itemLongClickListener: ItemLongClickListener?

    fun handleItemClick(holder: VH, view: View)

    fun handleItemLongClick(holder: VH, view: View): Boolean
}

interface ISelectableItemAdapter<KeyType> {

    var selectionHelper: SelectionHelper<KeyType>?

    var itemClickListener: ItemClickListener?

    fun getSelectionKey(position: Int): KeyType?

    fun getKeyPosition(key: KeyType): Int

    fun notifySelectionChanged(positions: List<Int>)
}

abstract class BaseClickableItemListAdapter<T, VH : ClickableViewHolder>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T,VH>(callback), IClickableItemAdapter<VH> {

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

abstract class BaseSelectableItemListAdapter<ItemType, KeyType, VH>(callback: DiffUtil.ItemCallback<ItemType>)
    : BaseClickableItemListAdapter<ItemType,VH>(callback), ISelectableItemAdapter<KeyType>
        where VH: SelectableViewHolder<KeyType>
{
    override var selectionHelper: SelectionHelper<KeyType>? = null

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
        return getSelectionKey(position)?.let { selectionHelper?.isSelected(it) } == true
    }

    override fun notifySelectionChanged(positions: List<Int>) {
        positions.forEach { notifyItemChanged(it) }
    }
}