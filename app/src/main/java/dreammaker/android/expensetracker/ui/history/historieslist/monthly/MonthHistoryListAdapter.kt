package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.databinding.MonthHistoryListItemBinding
import dreammaker.android.expensetracker.ui.history.historieslist.BaseHistoryViewHolder
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible

private val DATE_FORMAT = "MMMM dd, yyyy"

private val callback = object: DiffUtil.ItemCallback<HistoryModel>() {
    override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem == newItem
}

class MonthHistoryViewHolder(
    private val binding: MonthHistoryListItemBinding,
    onClick: (MonthHistoryViewHolder, View)->Unit
) :  BaseHistoryViewHolder<MonthHistoryViewHolder>(binding.root,onClick) {

    fun bind(history: HistoryModel?, selected: Boolean, showHeader: Boolean = false) {
        bind(history,selected)
        if (null == history) {
            binding.date.visibilityGone()
            binding.date.text = null
        }
        else {
            binding.date.text = history.date?.format(DATE_FORMAT)
            binding.date.visible()
        }
    }

    override fun bind(history: HistoryModel?, selected: Boolean) {
        setGroup(binding.group,history)
        setType(binding.type,history)
        setAmount(binding.amount,history)
        setNote(binding.note,history)
        setSource(binding.source,history)
        setDestination(binding.destination,history)
        if (null == history) {
            binding.main.isSelected = false
        }
        else {
            binding.main.isSelected = selected
        }
    }
}


class MonthHistoryListAdapter
    : BaseSelectableItemListAdapter<HistoryModel, Long, MonthHistoryViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MonthHistoryListItemBinding.inflate(inflater,parent,false)
        return MonthHistoryViewHolder(binding, this::handleItemClick)
    }
    override fun onBindViewHolder(holder: MonthHistoryViewHolder, position: Int) {
        val data = getItem(position)
        val isFirstInGroup = position == 0 || getItem(position-1)?.date == data.date
        holder.bind(data, isSelected(position), isFirstInGroup)
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}