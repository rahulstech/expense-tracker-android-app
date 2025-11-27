package dreammaker.android.expensetracker.ui.history.historieslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import dreammaker.android.expensetracker.FULL_DATE_FORMAT
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.HistoryHeaderLayoutBinding
import dreammaker.android.expensetracker.databinding.HistoryItemLayoutBinding
import dreammaker.android.expensetracker.ui.HistoryListItem
import dreammaker.android.expensetracker.util.IClickableItemAdapter
import dreammaker.android.expensetracker.util.ISelectableItemAdapter
import dreammaker.android.expensetracker.util.ItemClickListener
import dreammaker.android.expensetracker.util.ItemLongClickListener
import dreammaker.android.expensetracker.util.SelectableViewHolder
import dreammaker.android.expensetracker.util.SelectionHelper
import dreammaker.android.expensetracker.util.UNKNOWN_ACCOUNT
import dreammaker.android.expensetracker.util.getTypeBackground
import dreammaker.android.expensetracker.util.getTypeColorOnBackground
import dreammaker.android.expensetracker.util.getTypeLabel
import dreammaker.android.expensetracker.util.toCurrencyString
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.Dispatchers


sealed class HistoryViewHolder(itemView: View): SelectableViewHolder<Long>(itemView) {

    fun bind(data: HistoryListItem) {
        when {
            this is HeaderViewHolder && data is HistoryListItem.Header -> bind(data)
            this is ItemViewHolder && data is HistoryListItem.Item -> bind(data)
            else -> {}
        }
    }

    class HeaderViewHolder(
        private val binding: HistoryHeaderLayoutBinding
    ): HistoryViewHolder(binding.root) {

        fun bind(item: HistoryListItem.Header) {
            val date = item.data
            binding.headerText.text = date.format(FULL_DATE_FORMAT)
        }
    }

    class ItemViewHolder(
        private val binding: HistoryItemLayoutBinding
    ): HistoryViewHolder(binding.root) {

        private var stableItemId: Long? = null

        override fun getSelectionKey(): Long? = stableItemId

        fun bind(item: HistoryListItem.Item) {
            val history = item.data
            stableItemId = history.id
            binding.apply {
                note.text = history.note
                amount.text = history.amount.toCurrencyString()

                val srcAccount = history.primaryAccount ?: UNKNOWN_ACCOUNT
                val destAccount = history.secondaryAccount
                val historyGroup = history.group

                source.text = boldFieldText(R.string.label_history_list_item_source_account,srcAccount.name)
                source.visible()

                if (null != destAccount) {
                    destination.text = boldFieldText(R.string.label_history_list_item_destination_account,destAccount.name)
                    destination.visible()
                }
                else {
                    destination.visibilityGone()
                    destination.text = null
                }

                type.apply {
                    text = history.getTypeLabel(context)
                    background = history.getTypeBackground(context)
                    setTextColor(history.getTypeColorOnBackground(context))
                }

                 if (null != historyGroup) {
                    group.text = historyGroup.name
                    group.visible()
                 }
                else {
                    group.visibilityGone()
                    group.text = null
                }
            }

            itemView.isActivated = item.selected
        }
    }

    class PlaceholderViewHolder(inflater: LayoutInflater, parent: ViewGroup)
        : HistoryViewHolder(
        inflater.inflate(R.layout.history_placeholder_layout,parent,false)
        )

    fun boldFieldText(@StringRes labelResId: Int, text: String?): CharSequence = boldFieldText(getString(labelResId),text)

    fun boldFieldText(label: String?, text: String?): CharSequence {
        return buildSpannedString {
            append(label ?: "")
            append(" ")
            if (!text.isNullOrBlank()) {
                bold { append(text) }
            }
        }
    }
}

internal val DIFF_CALLBACK = object: DiffUtil.ItemCallback<HistoryListItem>() {
    override fun areItemsTheSame(
        oldItem: HistoryListItem,
        newItem: HistoryListItem
    ): Boolean {
        return when {
            oldItem is HistoryListItem.Header &&
                    newItem is HistoryListItem.Header ->
                oldItem.data == newItem.data

            oldItem is HistoryListItem.Item &&
                    newItem is HistoryListItem.Item ->
                oldItem.data.id == newItem.data.id

            else ->
                false
        }
    }

    override fun areContentsTheSame(
        oldItem: HistoryListItem,
        newItem: HistoryListItem
    ): Boolean = oldItem == newItem
}

class HistoryListAdapter:
    PagingDataAdapter<HistoryListItem, HistoryViewHolder>(
        diffCallback = DIFF_CALLBACK,
        workerDispatcher = Dispatchers.IO
    ),
    IClickableItemAdapter<HistoryViewHolder>,
    ISelectableItemAdapter<Long>
{
    companion object {
        private val TAG = HistoryListAdapter::class.simpleName
        const val TYPE_PLACEHOLDER = 0
        const val TYPE_HEADER = 1
        const val TYPE_ITEM = 2
    }

    override var selectionHelper: SelectionHelper<Long>? = null

    override var itemClickListener: ItemClickListener? = null

    override var itemLongClickListener: ItemLongClickListener? = null

    override fun handleItemClick(holder: HistoryViewHolder, view: View) {
        itemClickListener?.invoke(this,view,holder.absoluteAdapterPosition)
    }

    override fun handleItemLongClick(holder: HistoryViewHolder, view: View): Boolean =
        itemLongClickListener?.invoke(this,view,holder.absoluteAdapterPosition) ?: false


    override fun getItemViewType(position: Int): Int =
        when(getItem(position)) {
            is HistoryListItem.Header -> TYPE_HEADER
            is HistoryListItem.Item -> TYPE_ITEM
            else -> TYPE_PLACEHOLDER
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_HEADER -> {
                val binding = HistoryHeaderLayoutBinding.inflate(inflater,parent,false)
                HistoryViewHolder.HeaderViewHolder(binding)
            }
            TYPE_ITEM -> {
                val binding = HistoryItemLayoutBinding.inflate(inflater,parent,false)
                HistoryViewHolder.ItemViewHolder(binding).apply {
                    attachItemClickListener { vh,v -> handleItemClick(vh as HistoryViewHolder,v) }
                    attachItemLongClickListener { vh,v ->  handleItemLongClick(vh as HistoryViewHolder,v) }
                }
            }
            else -> {
                HistoryViewHolder.PlaceholderViewHolder(inflater,parent)
            }
        }
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = getItem(position) ?: HistoryListItem.Placeholder()
        if (item is HistoryListItem.Item) {
            item.selected = isSelected(item.data.id)
        }
        holder.bind(item)
    }

    fun getHistoryId(position: Int): Long? {
        val item = getItem(position)
        return when(item) {
            is HistoryListItem.Item -> item.data.id
            else -> null
        }
    }

    override fun canSelectKey(key: Long?): Boolean = null != key

    override fun getSelectionKeyAtPosition(position: Int): Long? = getHistoryId(position)
}

