package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.databinding.DayHistoryListItemBinding
import dreammaker.android.expensetracker.ui.history.historieslist.BaseHistoryViewHolder
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter2
import rahulstech.android.expensetracker.domain.model.History

private val callback = object: DiffUtil.ItemCallback<History>() {
    override fun areItemsTheSame(oldItem: History, newItem: History): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: History, newItem: History): Boolean =
        oldItem == newItem
}

class DayViewHolder(
    val binding: DayHistoryListItemBinding
): BaseHistoryViewHolder<DayViewHolder>(binding.root) {

    override fun bind(history: History?, selected: Boolean) {
        setGroup(binding.group,history)
        setType(binding.type,history)
        setAmount(binding.amount,history)
        setNote(binding.note,history)
        setSource(binding.source,history)
        setDestination(binding.destination,history)
        if (null == history) {
            binding.root.isActivated = false
        }
        else {
            binding.root.isActivated = selected
        }
    }

    fun getSelectedItemDetails(): ItemDetailsLookup.ItemDetails<Long?>? = object: ItemDetailsLookup.ItemDetails<Long?>() {
        override fun getPosition(): Int = absoluteAdapterPosition

        override fun getSelectionKey(): Long? = itemId
    }
}

class DayHistoryListAdapter: BaseSelectableItemListAdapter2<History, Long, DayViewHolder>(callback) {

    init {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DayHistoryListItemBinding.inflate(inflater,parent,false)
        return DayViewHolder(binding).apply {
            attachItemClickListener { vh,v -> handleItemClick(vh,v) }
            attachItemLongClickListener { vh,v -> handleItemLongClick(vh,v) }
        }
    }
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long? {
        val id = getItemId(position)
        return if (id == RecyclerView.NO_ID) null else id
    }
}