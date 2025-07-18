package dreammaker.android.expensetracker.ui.account.accountlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.AccountListItemBinding
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.util.ClickableViewHolder
import dreammaker.android.expensetracker.util.getBalanceText

class AccountViewModel(
    val binding: AccountListItemBinding,
    onClick: (AccountViewModel,View)->Unit
): ClickableViewHolder<AccountViewModel>(binding.root) {

    init {
        setItemClickListener(onClick)
        attachItemClickListener()
    }

    fun bind(data: AccountModel?, selected: Boolean) {
        if (null == data) {
            binding.name.text = null
            binding.balance.text = null
            binding.root.isSelected = false
        }
        else {
            binding.name.text = data.name
            binding.balance.text = data.getBalanceText(context)
            binding.root.isSelected = selected
        }
    }
}

private val callback = object: DiffUtil.ItemCallback<AccountModel>() {
    override fun areItemsTheSame(oldItem: AccountModel, newItem: AccountModel): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: AccountModel, newItem: AccountModel): Boolean =
        oldItem == newItem
}

class AccountsAdapter :
    BaseSelectableItemListAdapter<AccountModel, Long, AccountViewModel>(callback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewModel {
        val inflate = LayoutInflater.from(parent.context)
        val binding = AccountListItemBinding.inflate(inflate, parent, false)
        return AccountViewModel(binding,this::handleItemClick)
    }

    override fun onBindViewHolder(holder: AccountViewModel, position: Int) {
        val data = getItem(position)
        holder.bind(data,isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position).id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}