package dreammaker.android.expensetracker.ui.group.groupslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.databinding.GroupListItemBinding
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter2
import dreammaker.android.expensetracker.util.ClickableViewHolder
import dreammaker.android.expensetracker.util.getBalanceLabel
import dreammaker.android.expensetracker.util.getBalanceText
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible

class GroupsListViewHolder(
    val binding: GroupListItemBinding,
): ClickableViewHolder<GroupsListViewHolder>(binding.root) {

    fun bind(group: GroupModel?, selected: Boolean) {
        if (null == group) {
            binding.name.text = null
            binding.balance.text = null
            binding.root.isActivated = false
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
            binding.root.isActivated = selected
        }
    }

    fun getSelectedItemDetails(): ItemDetailsLookup.ItemDetails<Long?>? = object: ItemDetailsLookup.ItemDetails<Long?>() {
        override fun getPosition(): Int = absoluteAdapterPosition

        override fun getSelectionKey(): Long? = itemId
    }
}

private val callback = object : DiffUtil.ItemCallback<GroupModel>() {
    override fun areItemsTheSame(oldItem: GroupModel, newItem: GroupModel): Boolean
    = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: GroupModel, newItem: GroupModel): Boolean
    = oldItem == newItem
}

class GroupsListAdapter: BaseSelectableItemListAdapter2<GroupModel, Long, GroupsListViewHolder>(callback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GroupListItemBinding.inflate(inflater,parent,false)
        return GroupsListViewHolder(binding).apply {
            attachItemClickListener { holder,view-> handleItemClick(holder,view) }
            attachItemLongClickListener { holder,view -> handleItemLongClick(holder,view) }
        }
    }

    override fun onBindViewHolder(holder: GroupsListViewHolder, position: Int) {
        val person = getItem(position)
        holder.bind(person,isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long? {
        val id = getItemId(position)
        return if (id == RecyclerView.NO_ID) null else id
    }
}