package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.DayHistoryListItemBinding
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.getBackgroundColor
import dreammaker.android.expensetracker.ui.util.getColorOnBackground
import dreammaker.android.expensetracker.ui.util.getLabel
import dreammaker.android.expensetracker.ui.util.invisible
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import dreammaker.android.expensetracker.ui.util.visible

class DayHistoryViewHolder(
    private val binding: DayHistoryListItemBinding,
    onClick: (DayHistoryViewHolder, View)->Unit
) :  ClickableViewHolder<DayHistoryViewHolder>(binding.root) {

    init {
        setItemClickListener(onClick)
        attachItemClickListener()
    }

    fun bind(history: HistoryModel?, selected: Boolean) {
        if (history == null) {
            binding.amount.text = null
            binding.note.text = null
            binding.source.text = null
            binding.destination.text = null
            setType(null)
            setGroup(null)
        }
        else {
            binding.amount.text = history.amount?.toCurrencyString()
            binding.note.text = history.note ?: history.type!!.getLabel(context)
            setGroup(history.group)
            val type = history.type!!
            setType(type)
            when (type) {
                HistoryType.TRANSFER -> {
                    setSource(R.string.label_history_list_item_transfer_source, history.srcAccount?.name)
                    setDestination(R.string.label_history_item_transfer_destination, history.destAccount?.name)
                }
                HistoryType.CREDIT -> {
                    setDestination(R.string.label_history_list_item_credit_destination, history.destAccount?.name)
                }
                HistoryType.DEBIT -> {
                    setSource(R.string.label_history_list_item_debit_source, history.srcAccount?.name)
                }
            }
            binding.root.isSelected = selected
        }
    }

    private fun setSource(@StringRes resId: Int, text: CharSequence?) {
        if (!text.isNullOrBlank()) {
            binding.source.text = getString(resId, text)
            binding.source.visible()
        }
    }

    private fun setDestination(@StringRes resId: Int, text: CharSequence?) {
        if (!text.isNullOrBlank()) {
            binding.destination.text = getString(resId, text)
            binding.destination.visible()
        }
    }

    private fun setType(type: HistoryType?) {
        val view = binding.type
        type?.let {
            view.text = type.getLabel(context)
            ViewCompat.setBackgroundTintList(view, type.getBackgroundColor(context))
            view.setTextColor(type.getColorOnBackground(context))
            view.visible()
        }
    }

    private fun setGroup(group: GroupModel?) {
        if (null == group) {
            binding.group.invisible()
        }
        else {
            binding.group.text = group.name
            binding.group.visible()
        }
    }
}

private val callback = object: DiffUtil.ItemCallback<HistoryModel>() {
    override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem == newItem
}

class DayHistoryListAdapter: BaseSelectableItemListAdapter<HistoryModel, Long, DayHistoryViewHolder>(
    callback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DayHistoryListItemBinding.inflate(inflater,parent,false)
        return DayHistoryViewHolder(binding, this::handleItemClick)
    }
    override fun onBindViewHolder(holder: DayHistoryViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}