package dreammaker.android.expensetracker.ui.person

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.PersonChooserListItemBinding
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.SelectionStore
import java.util.Locale

class PersonChooserViewHolder(private val binding: PersonChooserListItemBinding, onClick: (PersonChooserViewHolder,View)->Unit)
    : ClickableViewHolder<PersonChooserViewHolder>(binding.root, onClick) {

        init {
            attachItemClickListener()
        }

        fun bind(data: PersonModel?, selected: Boolean) {
            if (null == data) {
                binding.checkbox.isChecked = false
                binding.personName.text = null
                binding.personDue.text = null
            }
            else {
                binding.checkbox.isChecked = selected
                binding.personName.text = data.name
                binding.personDue.text = String.format(Locale.ENGLISH, "%.2f", data.due)
            }
        }
}

private val callback = object : DiffUtil.ItemCallback<PersonModel>() {
    override fun areItemsTheSame(oldItem: PersonModel, newItem: PersonModel): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PersonModel, newItem: PersonModel): Boolean
    = oldItem.equals(newItem)
}

class PersonChooserListAdapter: BaseSelectableItemListAdapter<PersonModel, Long, PersonChooserViewHolder>(callback) {

    override var itemClickListener: ((RecyclerView.Adapter<PersonChooserViewHolder>, View, Int) -> Unit)? = null

    override var selectionStore: SelectionStore<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonChooserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PersonChooserListItemBinding.inflate(inflater,parent,false)
        return PersonChooserViewHolder(binding, this::handleItemClick)
    }

    override fun onBindViewHolder(holder: PersonChooserViewHolder, position: Int) {
        val data = getItem(position)
        val selected = isSelected(position)
        holder.bind(data, selected)
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: -1

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}