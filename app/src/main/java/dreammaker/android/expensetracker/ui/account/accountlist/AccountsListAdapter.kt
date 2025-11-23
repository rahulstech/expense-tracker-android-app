package dreammaker.android.expensetracker.ui.account.accountlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.databinding.AccountListItemBinding
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.util.SelectableViewHolder
import dreammaker.android.expensetracker.util.getBalanceText
import rahulstech.android.expensetracker.domain.model.Account

class AccountViewHolder(
    val binding: AccountListItemBinding
): SelectableViewHolder<Long>(binding.root) {

    fun bind(data: Account?, selected: Boolean) {
        if (null == data) {
            binding.name.text = null
            binding.balance.text = null
            binding.root.isActivated = false
        }
        else {
            binding.name.text = data.name
            binding.balance.text = data.getBalanceText(context)
            binding.root.isActivated = selected
        }
    }

    override fun getSelectedItemDetails(): ItemDetailsLookup.ItemDetails<Long?> = object : ItemDetailsLookup.ItemDetails<Long?>() {
        override fun getPosition(): Int = absoluteAdapterPosition

        override fun getSelectionKey(): Long? = itemId
    }
}

private val callback = object: DiffUtil.ItemCallback<Account>() {
    override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean =
        oldItem == newItem
}

class AccountsAdapter: BaseSelectableItemListAdapter<Account, Long, AccountViewHolder>(callback) {

    init {
        setHasStableIds(true)
    }

    override fun getSelectionKey(position: Int): Long? {
        val id = getItemId(position)
        return if (id == RecyclerView.NO_ID) null else id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val inflate = LayoutInflater.from(parent.context)
        val binding = AccountListItemBinding.inflate(inflate, parent, false)
        return AccountViewHolder(binding).apply {
            attachItemClickListener {  vh, v -> handleItemClick(vh as AccountViewHolder,v) }
            attachItemLongClickListener { vh, v -> handleItemLongClick(vh as AccountViewHolder,v) }
        }
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position),isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID
}