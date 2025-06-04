package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.databinding.MonthHistoryListItemBinding
import dreammaker.android.expensetracker.ui.history.historieslist.BaseHistoryViewHolder
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.util.invisible
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible

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
) :  BaseHistoryViewHolder<MonthHistoryViewHolder>(binding.root) {

    init {
        binding.main.setOnClickListener { onClick(this, it) }
    }

    fun bind(history: HistoryModel?, selected: Boolean, isFirstInSection: Boolean, isLastInSection: Boolean) {
        bind(history, selected)
        bindHeader(history?.date, isFirstInSection)
        if (isLastInSection) {
            binding.divider.invisible()
        }
        else {
            binding.divider.visible()
        }
    }

    override fun bind(history: HistoryModel?, selected: Boolean) {
        setGroup(binding.group,history)
        setType(binding.type,history)
        setAmount(binding.amount,history)
        setNote(binding.note,history)
        setSource(binding.source,history)
        setDestination(binding.destination,history)
        binding.main.isSelected = null != history && selected
    }

    private fun bindHeader(data: Date?, isFirstInSection: Boolean) {
        if (null == data || !isFirstInSection) {
            binding.date.visibilityGone()
            binding.date.text = null
        }
        else {
            binding.date.text = data.format(DATE_FORMAT)
            binding.date.visible()
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
        holder.bind(data, isSelected(position), isFirstInSection(position), isLastInSection(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)

    fun getSection(position: Int): Date? = getItem(position)?.date

    fun isFirstInSection(position: Int): Boolean {
        val v = position == 0 || getSection(position-1) != getSection(position)
        return v;
    }

    fun isLastInSection(position: Int): Boolean = position == itemCount-1 || getSection(position+1) != getSection(position)
}