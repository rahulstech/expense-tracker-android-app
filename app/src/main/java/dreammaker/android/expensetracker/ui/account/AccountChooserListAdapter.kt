package dreammaker.android.expensetracker.ui.account

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
import java.util.Locale

class AccountChooserViewHolder(
    private val binding: AccountChooserListItemBinding,
    onClick: (AccountChooserViewHolder, View)->Unit
) : ClickableViewHolder<AccountChooserViewHolder>(binding.root) {

    init {
        setItemClickListener(onClick)
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
            binding.accountBalance.text = String.format(Locale.ENGLISH, "%.2f", data.balance)
        }
    }
}

private val callback = object: DiffUtil.ItemCallback<AccountModel>() {
    override fun areItemsTheSame(oldItem: AccountModel, newItem: AccountModel): Boolean
    = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: AccountModel, newItem: AccountModel): Boolean
    = oldItem.equals(newItem)

}

open class AccountChooserListAdapter: BaseSelectableItemListAdapter<AccountModel, Long, AccountChooserViewHolder>(callback) {

    private val TAG = AccountChooserListAdapter::class.simpleName

    override var itemClickListener: ((RecyclerView.Adapter<AccountChooserViewHolder>, View, Int)->Unit)? = null

    override var selectionStore: SelectionStore<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountChooserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AccountChooserListItemBinding.inflate(inflater, parent, false);
        return AccountChooserViewHolder(binding, this::handleItemClick)
    }

    override fun onBindViewHolder(holder: AccountChooserViewHolder, position: Int) {
        val data = getItem(position)
        val id = getItemId(position)
        val selected = selectionStore?.isSelected(id) ?: false
        holder.bind(data, selected)
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: -1

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}