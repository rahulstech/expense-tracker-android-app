package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.databinding.DayHistoryListItemBinding
import dreammaker.android.expensetracker.ui.history.historieslist.BaseHistoryViewHolder
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter

private val callback = object: DiffUtil.ItemCallback<HistoryModel>() {
    override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem == newItem
}

class DayViewHolder(
    val binding: DayHistoryListItemBinding,
    onClick: (DayViewHolder, View)->Unit
): BaseHistoryViewHolder<DayViewHolder>(binding.root) {

    init {
        binding.root.setOnClickListener{ onClick(this,it) }
    }
    override fun bind(history: HistoryModel?, selected: Boolean) {
        setGroup(binding.group,history)
        setType(binding.type,history)
        setAmount(binding.amount,history)
        setNote(binding.note,history)
        setSource(binding.source,history)
        setDestination(binding.destination,history)
        if (null == history) {
            binding.root.isSelected = false
        }
        else {
            binding.root.isSelected = selected
        }
    }
}

class DayHistoryListAdapter: BaseSelectableItemListAdapter<HistoryModel, Long, DayViewHolder>(
    callback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DayHistoryListItemBinding.inflate(inflater,parent,false)
        return DayViewHolder(binding, this::handleItemClick)
    }
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}