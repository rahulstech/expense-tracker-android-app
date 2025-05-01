package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.databinding.HistoryListItemBinding
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryViewHolder
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter

private val callback = object: DiffUtil.ItemCallback<HistoryModel>() {
    override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem == newItem
}

class DayHistoryListAdapter: BaseSelectableItemListAdapter<HistoryModel, Long, HistoryViewHolder>(
    callback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HistoryListItemBinding.inflate(inflater,parent,false)
        return HistoryViewHolder(binding, this::handleItemClick)
    }
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}