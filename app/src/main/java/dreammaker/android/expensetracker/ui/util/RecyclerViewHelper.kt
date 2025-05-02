package dreammaker.android.expensetracker.ui.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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

class SelectionStore<T>(val selectionMode: SelectionMode = SelectionMode.SINGLE) {

    private val TAG = SelectionStore::class.simpleName

    var selectionProvider: SelectionKeyProvider<T>? = null
    var selectedKey: T? = null
    var selectedKeys: HashSet<T>? = null
    private var keyPositions = HashMap<T, Int>()

    private var itemSelectionListenerRef = WeakReference<((SelectionStore<T>, T, Int, Boolean)->Unit)?>(null)
    var itemSelectionListener: ((SelectionStore<T>, T, Int, Boolean)->Unit)?
        get() = itemSelectionListenerRef.get()
        set(value) { itemSelectionListenerRef = WeakReference(value) }

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

    fun setInitialKey(key: T?) {
        if (selectionMode == SelectionMode.SINGLE) {
            selectedKey = key
        }
    }

    fun setInitialKeys(keys: Collection<T>) {
        if (selectionMode == SelectionMode.MULTIPLE) {
            selectedKeys = HashSet(keys)
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
        val position = selectionStore?.getPosition(key) ?: -1
        updateSelectionState(position, selected)
    }

    fun updateSelectionState(position: Int, selected: Boolean)
}

abstract class BaseClickableItemListAdapter<T, VH : ClickableViewHolder<VH>>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T,VH>(callback), IClickableItemAdapter<T,VH> {

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

// Sectioned Adapter

open class SectionViewHolder(val itemView: View) {}

interface ISectionAdapter<T, VH: SectionViewHolder> {

    fun getSection(position: Int): T?

    fun createSectionViewHolder(parent: ViewGroup, position: Int): VH

    fun bindSectionViewHolder(holder: VH, position: Int)
}
class SectionItemDecoration<T, VH : SectionViewHolder>(
    private val adapter: ISectionAdapter<T, VH>,
) : RecyclerView.ItemDecoration() {

    private val sectionCache = mutableMapOf<T, VH>()

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        if (childCount == 0) return

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i) ?: continue
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) continue

            val section = adapter.getSection(position)!!
            val isFirstInSection = position == 0 || adapter.getSection(position - 1) != section

            if (isFirstInSection) {
                val viewHolder = getSectionViewHolder(parent, section, position, rebind = true)
                val sectionView = viewHolder.itemView
                val left = child.left
                val top = child.top - sectionView.measuredHeight

                canvas.save()
                canvas.translate(left.toFloat(), top.toFloat())
                sectionView.draw(canvas)
                canvas.restore()
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val section = adapter.getSection(position)!!
        val isFirstInSection = position == 0 || adapter.getSection(position - 1) != section

        if (isFirstInSection) {
            val viewHolder = getSectionViewHolder(parent, section, position)
            outRect.top = viewHolder.itemView.measuredHeight
        } else {
            outRect.top = 0
        }
    }

    private fun getSectionViewHolder(
        parent: RecyclerView,
        section: T,
        position: Int,
        rebind: Boolean = false
    ): VH {
        val viewHolder: VH

        if (!sectionCache.containsKey(section)) {
            viewHolder = adapter.createSectionViewHolder(parent, position)
            adapter.bindSectionViewHolder(viewHolder, position)
            sectionCache[section] = viewHolder

            val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.AT_MOST)

            viewHolder.itemView.measure(widthSpec, heightSpec)
            viewHolder.itemView.layout(0, 0, viewHolder.itemView.measuredWidth, viewHolder.itemView.measuredHeight)

        } else {
            viewHolder = sectionCache[section]!!

            if (rebind) {
                adapter.bindSectionViewHolder(viewHolder, position)
            }
        }

        return viewHolder
    }
}
