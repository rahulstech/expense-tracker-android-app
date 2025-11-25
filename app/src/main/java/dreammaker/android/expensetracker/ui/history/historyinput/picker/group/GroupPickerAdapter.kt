package dreammaker.android.expensetracker.ui.history.historyinput.picker.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.GroupChooserListItemBinding
import dreammaker.android.expensetracker.ui.GroupListItem
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.util.SelectableViewHolder
import dreammaker.android.expensetracker.util.getDueText


sealed class GroupPickerViewHolder(itemView: View): SelectableViewHolder<Long>(itemView) {

    fun bind(value: GroupListItem) {
        when {
            this is HeaderViewHolder
                    && value is GroupListItem.Header -> {
                bind(value)
            }
            this is ItemViewHolder
                    && value is GroupListItem.Item -> {
                bind(value)
            }
        }
    }

    class HeaderViewHolder(inflater: LayoutInflater, parent: ViewGroup): GroupPickerViewHolder(
        inflater.inflate(R.layout.group_header_item,parent,false)
    ) {
        private val headerLabel = itemView.findViewById<TextView>(R.id.header_label)

        fun bind(item: GroupListItem.Header) {
            headerLabel.text = item.data
        }
    }

    class ItemViewHolder(
        val binding: GroupChooserListItemBinding
    ): GroupPickerViewHolder(binding.root) {
        fun bind(item: GroupListItem.Item) {
            val data = item.data
            binding.name.text = data.name
            binding.due.text = data.getDueText(context)
            binding.root.isActivated = item.selected
        }

        override fun getSelectedItemDetails(): ItemDetailsLookup.ItemDetails<Long?>? = object: ItemDetailsLookup.ItemDetails<Long?>() {
            override fun getPosition(): Int = absoluteAdapterPosition

            override fun getSelectionKey(): Long? = itemId
        }
    }
}

private val callback = object: DiffUtil.ItemCallback<GroupListItem>() {
    override fun areItemsTheSame(oldItem: GroupListItem, newItem: GroupListItem): Boolean
    = when {
        oldItem is GroupListItem.Header && newItem is GroupListItem.Header -> oldItem.data == newItem.data
        oldItem is GroupListItem.Item && newItem is GroupListItem.Item -> oldItem.data.id == newItem.data.id
        else -> false
    }

    override fun areContentsTheSame(oldItem: GroupListItem, newItem: GroupListItem): Boolean
    = oldItem == newItem
}

open class GroupPickerListAdapter: BaseSelectableItemListAdapter<GroupListItem, Long, GroupPickerViewHolder>(callback) {

    companion object {
        const val TYPE_HEADER = 1
        const val TYPE_ITEM = 2
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupPickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_HEADER -> GroupPickerViewHolder.HeaderViewHolder(inflater,parent)
            else -> {
                GroupPickerViewHolder.ItemViewHolder(
                    GroupChooserListItemBinding.inflate(inflater, parent, false)
                ).apply {
                    attachItemClickListener { vh,v -> handleItemClick(vh as GroupPickerViewHolder,v) }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int =
        when(getItem(position)) {
            is GroupListItem.Header ->TYPE_HEADER
            is GroupListItem.Item -> TYPE_ITEM
        }

    override fun onBindViewHolder(holder: GroupPickerViewHolder, position: Int) {
        val data = getItem(position)
        if (data is GroupListItem.Item) {
            data.selected = isSelected(position)
        }
        holder.bind(data)
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return when(item) {
            is GroupListItem.Item -> item.data.id
            else -> item.hashCode().toLong()
        }
    }

    override fun getSelectionKey(position: Int): Long? = getItemId(position)
}