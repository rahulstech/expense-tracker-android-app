package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.AccountChooserListItemBinding
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.toCurrencyString

class AccountPickerViewHolder(
    private val binding: AccountChooserListItemBinding,
    onClick: (AccountPickerViewHolder, View)->Unit
) : ClickableViewHolder<AccountPickerViewHolder>(binding.root) {

    init {
        setItemClickListener(onClick)
        attachItemClickListener()
    }

    fun bind(data: AccountModel?, isSelected: Boolean) {
        if (null == data) {
            binding.accountName.text = null
            binding.accountBalance.text = null
            binding.root.isSelected = false
        }
        else {
            binding.accountName.text = data.name
            binding.accountBalance.text = data.balance!!.toCurrencyString()
            binding.root.isSelected = isSelected
        }
    }
}

private val callback = object: DiffUtil.ItemCallback<AccountModel>() {
    override fun areItemsTheSame(oldItem: AccountModel, newItem: AccountModel): Boolean
            = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: AccountModel, newItem: AccountModel): Boolean
            = oldItem == newItem

}

open class AccountPickerListAdapter: BaseSelectableItemListAdapter<AccountModel, Long, AccountPickerViewHolder>(callback) {
    private val TAG = AccountPickerListAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountPickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AccountChooserListItemBinding.inflate(inflater, parent, false);
        return AccountPickerViewHolder(binding, this::handleItemClick)
    }

    override fun onBindViewHolder(holder: AccountPickerViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}