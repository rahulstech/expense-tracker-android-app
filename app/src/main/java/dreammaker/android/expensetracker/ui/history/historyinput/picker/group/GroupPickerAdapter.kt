package dreammaker.android.expensetracker.ui.history.historyinput.picker.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.databinding.GroupChooserListItemBinding
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.getBalanceText

class GroupPickerViewHolder(
    private val binding: GroupChooserListItemBinding,
    onClick: (GroupPickerViewHolder, View)->Unit
) : ClickableViewHolder<GroupPickerViewHolder>(binding.root) {

    init {
        setItemClickListener(onClick)
        attachItemClickListener()
    }

    fun bind(data: GroupModel?, isSelected: Boolean) {
        if (null == data) {
            binding.name.text = null
            binding.balance.text = null
            binding.root.isSelected = false
        }
        else {
            binding.name.text = data.name
            binding.balance.text = data.getBalanceText(context)
            binding.root.isSelected = isSelected
        }
    }
}

private val callback = object: DiffUtil.ItemCallback<GroupModel>() {
    override fun areItemsTheSame(oldItem: GroupModel, newItem: GroupModel): Boolean
    = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: GroupModel, newItem: GroupModel): Boolean
    = oldItem == newItem

}

open class GroupPickerListAdapter: BaseSelectableItemListAdapter<GroupModel, Long, GroupPickerViewHolder>(
    callback
) {
    private val TAG = GroupPickerListAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupPickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GroupChooserListItemBinding.inflate(inflater, parent, false);
        return GroupPickerViewHolder(binding, this::handleItemClick)
    }

    override fun onBindViewHolder(holder: GroupPickerViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}