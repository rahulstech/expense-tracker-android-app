package dreammaker.android.expensetracker.ui.history.historyinput.picker.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.databinding.GroupChooserListItemBinding
import dreammaker.android.expensetracker.util.BaseSelectableItemListAdapter2
import dreammaker.android.expensetracker.util.ClickableViewHolder
import dreammaker.android.expensetracker.util.getDueText
import rahulstech.android.expensetracker.domain.model.Group

class GroupPickerViewHolder(
    private val binding: GroupChooserListItemBinding,
) : ClickableViewHolder<GroupPickerViewHolder>(binding.root) {

    fun bind(data: Group?, isSelected: Boolean) {
        if (null == data) {
            binding.name.text = null
            binding.due.text = null
            binding.root.isActivated = false
        }
        else {
            binding.name.text = data.name
            binding.due.text = data.getDueText(context)
            binding.root.isActivated = isSelected
        }
    }

    fun getSelectedItemDetails(): ItemDetailsLookup.ItemDetails<Long?>? = object: ItemDetailsLookup.ItemDetails<Long?>() {
        override fun getPosition(): Int = absoluteAdapterPosition

        override fun getSelectionKey(): Long? = itemId
    }
}

private val callback = object: DiffUtil.ItemCallback<Group>() {
    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean
    = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean
    = oldItem == newItem

}

open class GroupPickerListAdapter: BaseSelectableItemListAdapter2<Group, Long, GroupPickerViewHolder>(callback) {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupPickerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GroupChooserListItemBinding.inflate(inflater, parent, false);
        return GroupPickerViewHolder(binding).apply {
            attachItemClickListener { vh,v -> handleItemClick(vh,v)}
        }
    }

    override fun onBindViewHolder(holder: GroupPickerViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long? = getItemId(position)
}