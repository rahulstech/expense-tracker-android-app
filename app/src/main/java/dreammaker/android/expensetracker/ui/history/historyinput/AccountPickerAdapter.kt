package dreammaker.android.expensetracker.ui.history.historyinput

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.AccountChooserListItemBinding
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.toCurrencyString

class AccountPickerViewHolder(
    private val binding: AccountChooserListItemBinding,
    onClick: (AccountPickerViewHolder, View)->Unit
)
    : ClickableViewHolder<AccountPickerViewHolder>(binding.root, onClick) {

    init {
        attachItemClickListener()
    }

    fun bind(data: AccountModel?, isSelected: Boolean) {
        if (null == data) {
            binding.checkbox.isChecked = false
            binding.accountName.text = null
            binding.accountBalance.text = null
        }
        else {
            binding.checkbox.isChecked = isSelected
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
            = oldItem.equals(newItem)

}

open class AccountPickerListAdapter: BaseSelectableItemListAdapter<AccountModel, Long, AccountPickerViewHolder>(
    callback
) {

    private val TAG = AccountPickerListAdapter::class.simpleName

    override var itemClickListener: ((RecyclerView.Adapter<AccountPickerViewHolder>, View, Int)->Unit)? = null

    override var selectionStore: SelectionStore<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountPickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AccountChooserListItemBinding.inflate(inflater, parent, false);
        return AccountPickerViewHolder(binding, this::handleItemClick)
    }

    override fun onBindViewHolder(holder: AccountPickerViewHolder, position: Int) {
        val data = getItem(position)
        val id = getItemId(position)
        val selected = selectionStore?.isSelected(id) ?: false
        holder.bind(data, selected)
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: -1

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}