package dreammaker.android.expensetracker.ui.group.groupslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.databinding.GroupListItemBinding
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.util.ClickableViewHolder
import dreammaker.android.expensetracker.util.getBalanceLabel
import dreammaker.android.expensetracker.util.getBalanceText
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible

class GroupsListViewHolder(
    val binding: GroupListItemBinding,
): ClickableViewHolder<GroupsListViewHolder>(binding.root) {
    init {
        attachItemClickListener()
    }

    fun bind(group: GroupModel?, selected: Boolean) {
        if (null == group) {
            binding.name.text = null
            binding.balance.text = null
        }
        else {
            val label = group.getBalanceLabel(context)
            binding.name.text = group.name
            if (label.isBlank()) {
                binding.labelBalance.visibilityGone()
            }
            else {
                binding.labelBalance.text = label
                binding.labelBalance.visible()
            }
            binding.balance.text = group.getBalanceText(context) // TODO: add country code and locale
            binding.root.isSelected = selected
        }
    }
}

private val callback = object : DiffUtil.ItemCallback<GroupModel>() {
    override fun areItemsTheSame(oldItem: GroupModel, newItem: GroupModel): Boolean
    = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: GroupModel, newItem: GroupModel): Boolean
    = oldItem == newItem
}

class GroupsListAdapter: BaseSelectableItemListAdapter<GroupModel, Long, GroupsListViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GroupListItemBinding.inflate(inflater,parent,false)
        return GroupsListViewHolder(binding).apply {
            setItemClickListener { holder,view-> handleItemClick(holder,view) }
            setLongClickListener { holder,view -> handleItemLongClick(holder,view) }
        }
    }

    override fun onBindViewHolder(holder: GroupsListViewHolder, position: Int) {
        val person = getItem(position)
        holder.bind(person,isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}