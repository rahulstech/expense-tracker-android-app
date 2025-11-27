package dreammaker.android.expensetracker.util

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Checkable
import androidx.annotation.StringRes
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dreammaker.android.expensetracker.ui.main.ContextualActionBarViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Item Selection

class SelectionViewModel: ViewModel() {

    private var keys: Set<Any> = emptySet()
    var inSelectionMode: Boolean = false
    var inCabMode: Boolean = false
    var canSelectMultiple: Boolean = true

    @Suppress("UNCHECKED_CAST")
    fun <T> getSelectionKeys(): Set<T> = keys as Set<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> setSelectionKeys(keys: Set<T>) {
        this.keys = keys as Set<Any>
    }
}

typealias ItemSelectionChangeCallback<KeyType> = (helper: SelectionHelper<KeyType>, key: KeyType, selected: Boolean)->Unit

class SelectionHelper<KeyType>(
    private val recyclerView: RecyclerView,
    private val adapter: ISelectableItemAdapter<KeyType>,
    private val vmsOwner: ViewModelStoreOwner,
    private val lcOwner: LifecycleOwner,
) {

    companion object {
        private val TAG = SelectionHelper::class.simpleName
        private const val DEFAULT_SELECT_MULTIPLE = true
    }

    private var inSelectionMode: Boolean = false
    private var inCabMode: Boolean = false
    private var canSelectMultiple: Boolean = DEFAULT_SELECT_MULTIPLE
    private val isSingleSelection: Boolean get() = !canSelectMultiple

    var itemClickListener: ItemClickListener? = null
    private val selectableItemClickListener: ItemClickListener = { adapter,itemView,position ->
        if (inSelectionMode) {
            this.adapter.getSelectionKeyAtPosition(position)?.let { toggleItemSelection(it) }
        }
        else {
            itemClickListener?.invoke(adapter,itemView,position)
        }
    }

    var itemSelectionChangeCallback: ItemSelectionChangeCallback<KeyType>? = null

    private var selectionVM: SelectionViewModel = ViewModelProvider(vmsOwner)[SelectionViewModel::class]

    private val selections = mutableSetOf<KeyType>()

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
            val canSelectMultiple = vm.canSelectMultiple
            if (inSelectionMode) {
                startSelection(canSelectMultiple,inCabMode)
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
        val canSelectMultiple = this.canSelectMultiple
        val selections = HashSet<KeyType>()
        selections.addAll(this.selections)

        hideContextualActionBar()
        selectionVM.let { vm ->
            vm.inSelectionMode = inSelectionMode
            vm.inCabMode = inCabMode
            vm.canSelectMultiple = canSelectMultiple
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
        selectMultiple: Boolean = true,
        contextualActionBar: Boolean = false,
        initialSelection: KeyType? = null,
        onStart: ((SelectionHelper<KeyType>)->Unit)? = null
    ): Boolean {
        if (inSelectionMode) {
            Log.d(TAG, "startSelection: already in selection mode")
            return false
        }

        Log.d(TAG, "startSelection: selectionMultiple=$selectMultiple" +
                " contextualActionBar=$contextualActionBar initialSelection=$initialSelection")

        canSelectMultiple = selectMultiple
        inSelectionMode = true
        if (contextualActionBar) {
            showContextualActionbar()
        }
        initialSelection?.let { selectItem(it) }
        onStart?.invoke(this)
        return true
    }

    private fun canSelectKey(key: KeyType) = adapter.canSelectKey(key)

    private fun setItemSelection(key: KeyType, newSelection: Boolean, notify: Boolean = true) {
        val updated = if (!newSelection) {
            selections.remove(key)
        }
        else if (canSelectKey(key)) {
            selections.add(key)
        }
        else false

        if (updated && notify) {
            notifySelectionChange(key,newSelection)
            itemSelectionChangeCallback?.invoke(this,key,newSelection)
        }
    }

    fun selectItem(key: KeyType) {
        // if single selection allowed then first restore the old selection
        if (isSingleSelection && hasSelection()) {
            val oldKey = selections.first()
            setItemSelection(key = oldKey, newSelection = false)
        }

        // new selection is independent of selection type: single or multiple
        // check the new key is selectable
        setItemSelection(key,true)
    }

    fun deselectItem(key: KeyType) {
        setItemSelection(key,false)
    }

    private fun notifySelectionChange(key: KeyType, selected: Boolean) {
        val vh = recyclerView.children
            .map{ child -> recyclerView.getChildViewHolder(child) }
            .find { vh ->
                if (vh is SelectableViewHolder<*>) {
                    Log.d(TAG,"notifySelectionChange find: vh=$vh key=$key vh.selectionKey=${vh.getSelectionKey()}")
                    return@find vh.getSelectionKey() == key
                }
                false
            } as? SelectableViewHolder<*>
        Log.d(TAG,"notifySelectionChange: vh=$vh key=$key selected=$selected")
        vh?.changeSelection(selected)
    }

    fun isSelected(key: KeyType): Boolean = selections.contains(key)

    fun toggleItemSelection(key: KeyType) {
        if (isSelected(key)) {
            deselectItem(key)
        }
        else {
            selectItem(key)
        }
    }

    fun count(): Int = selections.size

    fun hasSelection(): Boolean = count() > 0

    fun getSelections(): List<KeyType> = if (inSelectionMode) selections.toList() else emptyList()

    fun getSelection(): KeyType? = if (hasSelection()) selections.first() else null

    fun setSelections(keys: Set<KeyType>) {
        keys.forEach { setItemSelection(it, newSelection = true, notify = false) }
        notifySelections()
    }

    @Suppress("UNCHECKED_CAST")
    private fun notifySelections(selected: Boolean = true) {
        recyclerView.children.forEach { child ->
            val vh = recyclerView.getChildViewHolder(child)
            if (vh is SelectableViewHolder<*>) {
                val key = vh.getSelectionKey() as? KeyType
                if (key != null)
                    vh.changeSelection(selected)
            }

        }
    }

    fun endSelection() {
        if (!inSelectionMode) return

        hideContextualActionBar()
        inSelectionMode = false
        selections.clear()
        notifySelections(false)
    }
}

// ViewHolder and Adapter

open class BaseViewHolder(itemView: View): ViewHolder(itemView) {

    val context: Context = itemView.context

    fun getString(@StringRes id: Int, vararg args: Any) = context.getString(id,*args)
}

abstract class ClickableViewHolder(itemView: View): BaseViewHolder(itemView) {

    fun attachItemClickListener(clickListener: ((ViewHolder, View)->Unit)?) {
        itemView.setOnClickListener{ clickListener?.invoke(this, it)}
    }

    fun attachItemLongClickListener(longClickListener: ((ViewHolder, View)-> Boolean)?) {
        itemView.setOnLongClickListener { longClickListener?.invoke(this,it) == true }
    }
}

abstract class SelectableViewHolder<KeyType>(itemView: View): ClickableViewHolder(itemView) {

    open fun getSelectionKey(): KeyType? = null

    open fun changeSelection(selected: Boolean) {
        if (itemView is Checkable) {
            itemView.isSelected = selected
        }
        else {
            itemView.isActivated = selected
        }
    }
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

    fun getSelectionKeyAtPosition(position: Int): KeyType?

    fun canSelectKey(key: KeyType?): Boolean = key != null

    fun isSelected(key: KeyType): Boolean = selectionHelper?.isSelected(key) ?: false
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
}