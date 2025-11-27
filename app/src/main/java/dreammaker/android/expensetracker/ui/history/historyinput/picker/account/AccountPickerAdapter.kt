package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.AccountChooserListItemBinding
import dreammaker.android.expensetracker.ui.AccountListItem
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.util.SelectableViewHolder
import dreammaker.android.expensetracker.util.getBalanceText

sealed class AccountPickerViewHolder(itemView: View): SelectableViewHolder<Long>(itemView) {

    fun bind(value: AccountListItem) {
        when {
            this is HeaderViewHolder
                    && value is AccountListItem.Header -> {
                        bind(value)
                    }
            this is ItemViewHolder
                    && value is AccountListItem.Item -> {
                        bind(value)
                    }
        }
    }

    class HeaderViewHolder(inflater: LayoutInflater, parent: ViewGroup):
        AccountPickerViewHolder(
            inflater.inflate(R.layout.account_header_item,parent,false)
        )
    {
        private val headerLabel = itemView.findViewById<TextView>(R.id.header_label)

         fun bind(value: AccountListItem.Header) {
            headerLabel.text = value.data
        }
    }

    class ItemViewHolder(
        private val binding: AccountChooserListItemBinding
    ): AccountPickerViewHolder(binding.root) {

        private var stableItemId: Long? = null

        override fun getSelectionKey(): Long? = stableItemId

         fun bind(item: AccountListItem.Item) {
             val account = item.data
             stableItemId = account.id
             binding.accountName.text = account.name
             binding.accountBalance.text = account.getBalanceText(context)
             binding.root.isActivated = item.selected
             binding.root.isEnabled = item.enabled
        }
    }
}

private val callback = object: DiffUtil.ItemCallback<AccountListItem>() {
    override fun areItemsTheSame(oldItem: AccountListItem, newItem: AccountListItem): Boolean =
        when {
            oldItem is AccountListItem.Header && newItem is AccountListItem.Header -> {
                oldItem.data == newItem.data
            }
            oldItem is AccountListItem.Item && newItem is AccountListItem.Item -> {
                oldItem.data.id == newItem.data.id
            }
            else -> false
        }

    override fun areContentsTheSame(oldItem: AccountListItem, newItem: AccountListItem): Boolean = oldItem == newItem
}

open class AccountPickerListAdapter: BaseSelectableItemListAdapter<AccountListItem, Long, AccountPickerViewHolder>(callback) {

    companion object {
        const val TYPE_HEADER = 1
        const val TYPE_ITEM = 2
    }

    var disabledId: Long? = null

    override fun getItemViewType(position: Int): Int =
        when(getItem(position)) {
            is AccountListItem.Header -> TYPE_HEADER
            is AccountListItem.Item -> TYPE_ITEM
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountPickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_HEADER -> AccountPickerViewHolder.HeaderViewHolder(inflater,parent)
            else -> {
                AccountPickerViewHolder.ItemViewHolder(
                    AccountChooserListItemBinding.inflate(inflater, parent, false)
                ).apply {
                    attachItemClickListener { vh,v -> handleItemClick(vh as AccountPickerViewHolder,v) }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: AccountPickerViewHolder, position: Int) {
        val item = getItem(position)
        if (item is AccountListItem.Item) {
            val id = item.data.id
            item.selected = isSelected(id)
            item.enabled = id != disabledId
        }
        holder.bind(item)
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return when(item) {
            is AccountListItem.Item -> item.data.id
            else -> RecyclerView.NO_ID
        }
    }

    override fun getSelectionKeyAtPosition(position: Int): Long? {
        val id = getItemId(position)
        if (id == RecyclerView.NO_ID) return null
        return id
    }

    override fun canSelectKey(key: Long?): Boolean = null != key && key != disabledId
}