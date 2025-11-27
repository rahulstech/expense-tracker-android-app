package dreammaker.android.expensetracker.ui.group.groupslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.databinding.GroupListItemBinding
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.util.SelectableViewHolder
import dreammaker.android.expensetracker.util.getDueLabel
import dreammaker.android.expensetracker.util.getBalanceText
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import rahulstech.android.expensetracker.domain.model.Group

class GroupsListViewHolder(
    val binding: GroupListItemBinding,
): SelectableViewHolder<Long>(binding.root) {

    private var stableItemId: Long? = null

    override fun getSelectionKey(): Long? = stableItemId

    fun bind(group: Group?, selected: Boolean) {
        stableItemId = group?.id
        if (null == group) {
            binding.name.text = null
            binding.due.text = null
            binding.root.isActivated = false
        }
        else {
            val label = group.getDueLabel(context)
            binding.name.text = group.name
            if (label.isBlank()) {
                binding.labelBalance.visibilityGone()
            }
            else {
                binding.labelBalance.text = label
                binding.labelBalance.visible()
            }
            binding.due.text = group.getBalanceText(context) // TODO: add country code and locale
            binding.root.isActivated = selected
        }
    }
}

private val callback = object : DiffUtil.ItemCallback<Group>() {
    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean
    = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean
    = oldItem == newItem
}

class GroupsListAdapter: BaseSelectableItemListAdapter<Group, Long, GroupsListViewHolder>(callback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GroupListItemBinding.inflate(inflater,parent,false)
        return GroupsListViewHolder(binding).apply {
            attachItemClickListener { holder,view-> handleItemClick(holder as GroupsListViewHolder,view) }
            attachItemLongClickListener { holder,view -> handleItemLongClick(holder as GroupsListViewHolder,view) }
        }
    }

    override fun onBindViewHolder(holder: GroupsListViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group,isSelected(group.id))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKeyAtPosition(position: Int): Long? {
        val id = getItemId(position)
        if (id == RecyclerView.NO_ID) return null
        return id
    }
}