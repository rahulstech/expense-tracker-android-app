package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.databinding.AccountChooserListItemBinding
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter2
import dreammaker.android.expensetracker.util.ClickableViewHolder
import dreammaker.android.expensetracker.util.getBalanceText
import rahulstech.android.expensetracker.domain.model.Account

class AccountPickerViewHolder(
    private val binding: AccountChooserListItemBinding
) : ClickableViewHolder<AccountPickerViewHolder>(binding.root) {

    fun bind(data: Account?, isSelected: Boolean) {
        if (null == data) {
            binding.accountName.text = null
            binding.accountBalance.text = null
            binding.root.isActivated = false
        }
        else {
            binding.accountName.text = data.name
            binding.accountBalance.text = data.getBalanceText(context)
            binding.root.isActivated = isSelected
        }
    }

    fun getSelectedItemDetails(): ItemDetailsLookup.ItemDetails<Long?>? = object: ItemDetailsLookup.ItemDetails<Long?>() {
        override fun getPosition(): Int = absoluteAdapterPosition

        override fun getSelectionKey(): Long? = itemId
    }
}

private val callback = object: DiffUtil.ItemCallback<Account>() {
    override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean
            = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean
            = oldItem == newItem

}

open class AccountPickerListAdapter: BaseSelectableItemListAdapter2<Account, Long, AccountPickerViewHolder>(callback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountPickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AccountChooserListItemBinding.inflate(inflater, parent, false);
        return AccountPickerViewHolder(binding).apply {
            attachItemClickListener { vh,view -> handleItemClick(vh,view) }
        }
    }

    override fun onBindViewHolder(holder: AccountPickerViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}