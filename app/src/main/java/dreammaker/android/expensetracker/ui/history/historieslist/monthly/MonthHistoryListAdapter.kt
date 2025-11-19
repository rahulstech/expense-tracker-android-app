package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.FULL_DATE_FORMAT
import dreammaker.android.expensetracker.databinding.MonthHistoryListItemBinding
import dreammaker.android.expensetracker.ui.history.historieslist.BaseHistoryViewHolder
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter2
import dreammaker.android.expensetracker.util.invisible
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

private val callback = object: DiffUtil.ItemCallback<History>() {
    override fun areItemsTheSame(oldItem: History, newItem: History): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: History, newItem: History): Boolean =
        oldItem == newItem
}

class MonthHistoryViewHolder(
    private val binding: MonthHistoryListItemBinding
):  BaseHistoryViewHolder<MonthHistoryViewHolder>(binding.root) {

    fun bind(history: History?, selected: Boolean, isFirstInSection: Boolean, isLastInSection: Boolean) {
        bind(history, selected)
        bindHeader(history?.date, isFirstInSection)
        if (isLastInSection) {
            binding.divider.invisible()
        }
        else {
            binding.divider.visible()
        }
    }

    override fun bind(history: History?, selected: Boolean) {
        setGroup(binding.group,history)
        setType(binding.type,history)
        setAmount(binding.amount,history)
        setNote(binding.note,history)
        setSource(binding.source,history)
        setDestination(binding.destination,history)
        binding.main.isSelected = null != history && selected
    }

    private fun bindHeader(data: LocalDate?, isFirstInSection: Boolean) {
        if (null == data || !isFirstInSection) {
            binding.date.visibilityGone()
            binding.date.text = null
        }
        else {
            binding.date.text = data.format(FULL_DATE_FORMAT)
            binding.date.visible()
        }
    }
}

class MonthHistoryListAdapter : BaseSelectableItemListAdapter2<History, Long, MonthHistoryViewHolder>(callback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MonthHistoryListItemBinding.inflate(inflater,parent,false)
        return MonthHistoryViewHolder(binding).apply {
            attachItemClickListener { vh,v -> handleItemClick(vh,v) }
            attachItemLongClickListener { vh,v -> handleItemLongClick(vh,v) }
        }
    }

    override fun onBindViewHolder(holder: MonthHistoryViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position), isFirstInSection(position), isLastInSection(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)

    fun getSection(position: Int): LocalDate? = getItem(position)?.date

    fun isFirstInSection(position: Int): Boolean {
        val v = position == 0 || getSection(position-1) != getSection(position)
        return v;
    }

    fun isLastInSection(position: Int): Boolean = position == itemCount-1 || getSection(position+1) != getSection(position)
}