package dreammaker.android.expensetracker.ui.history.viewhistories.daily

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.DayHistoryListItemBinding
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.toCurrencyString

class DayHistoryViewHolder(
    private val binding: DayHistoryListItemBinding,
    onClick: (DayHistoryViewHolder, View)->Unit
) :  ClickableViewHolder<DayHistoryViewHolder>(binding.root, onClick) {

    init {
        attachItemClickListener()
    }

    fun bind(history: HistoryModel?, selected: Boolean) {
        if (history == null) {
            binding.amount.text = null
            binding.note.text = null
            binding.source.text = null
            binding.destination.text = null
            binding.type.text = null
            ViewCompat.setBackgroundTintList(binding.type, null)
        }
        else {
            binding.amount.text = history.amount?.toCurrencyString()
            binding.note.text = history.note
            val type = history.type
            when (type) {
                HistoryType.TRANSFER -> {
                    setType(R.string.label_history_type_transfer, R.color.colorTransfer)
                    setSourceText(R.string.label_history_list_item_transfer_source, history.srcAccount?.name)
                    setDestinationText(R.string.label_history_item_transfer_destination, history.destAccount?.name)
                }
                HistoryType.CREDIT -> {
                    setType(R.string.label_history_type_credit, R.color.colorCredit)
                    setSourceText(R.string.label_history_list_item_credit_source, history.group?.name)
                    setDestinationText(R.string.label_history_list_item_credit_destination, history.destAccount?.name)
                }
                HistoryType.DEBIT -> {
                    setType(R.string.label_history_type_debit, R.color.colorDebit)
                    setSourceText(R.string.label_history_list_item_debit_source, history.srcAccount?.name)
                    setDestinationText(R.string.label_history_list_item_debit_destination, history.group?.name)
                }
                else -> {}
            }
        }
    }

    private fun setSourceText(@StringRes resId: Int, text: CharSequence?) {
        binding.source.text = getString(resId, text ?: "")
    }

    private fun setDestinationText(@StringRes resId: Int, text: CharSequence?) {
        binding.destination.text = getString(resId, text ?: "")
    }

    private fun setType(@StringRes text: Int, @ColorRes backgroundTint: Int) {
        binding.type.text = getString(text)
        binding.type.setTextColor(context.resources.getColorStateList(R.color.colorWhite, null))
        ViewCompat.setBackgroundTintList(binding.type, context.resources.getColorStateList(backgroundTint, null))
    }
}

private val callback = object: DiffUtil.ItemCallback<HistoryModel>() {
    override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem.equals(newItem)
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