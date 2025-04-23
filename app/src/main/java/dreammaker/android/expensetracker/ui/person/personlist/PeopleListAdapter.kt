package dreammaker.android.expensetracker.ui.person.personlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.PersonListItemBinding
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.toCurrencyString

class PeopleListViewHolder(
    val binding: PersonListItemBinding,
    onClick: (PeopleListViewHolder, View)->Unit
): ClickableViewHolder<PeopleListViewHolder>(binding.root,onClick) {
    init {
        attachItemClickListener()
    }

    fun bind(person: PersonModel?, selected: Boolean) {
        if (null == person) {
            binding.name.text = null
            binding.due.text = null
        }
        else {
            binding.name.text = person.name
            binding.due.text = person.due!!.toCurrencyString()
        }
    }
}

private val callback = object : DiffUtil.ItemCallback<PersonModel>() {
    override fun areItemsTheSame(oldItem: PersonModel, newItem: PersonModel): Boolean
    = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PersonModel, newItem: PersonModel): Boolean
    = oldItem == newItem
}

class PeopleListAdapter: BaseSelectableItemListAdapter<PersonModel,Long,PeopleListViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PersonListItemBinding.inflate(inflater,parent,false)
        return PeopleListViewHolder(binding,this::handleItemClick)
    }

    override fun onBindViewHolder(holder: PeopleListViewHolder, position: Int) {
        val person = getItem(position)
        holder.bind(person,isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}