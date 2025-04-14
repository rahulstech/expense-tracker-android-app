package dreammaker.android.expensetracker.feature.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HistoryListItemBinding

class HistoryViewHolder(private val binding: HistoryListItemBinding) : RecyclerView.ViewHolder(binding.root) {


    fun bind(history: HistoryModel?) {
        if (history == null) {
            binding.amount.text = null
            binding.date.text = null
            binding.note.text = null
            binding.type.text = null
            binding.source.text = null
            binding.destination.text = null
        }
        else {
            binding.amount.text = history.amount?.toString()
            binding.date.text = history.date?.toString()
            binding.note.text = history.note
            val type = history.type
            binding.type.text = type?.name
            when (type) {
                HistoryType.TRANSFER -> {
                    binding.source.text = history.srcAccount?.name ?: ""
                    binding.destination.text = history.destAccount?.name ?: ""
                }
                HistoryType.CREDIT -> {
                    binding.source.text = history.srcPerson?.name ?: ""
                    binding.destination.text = history.destAccount?.name ?: ""
                }
                HistoryType.DEBIT -> {
                    binding.source.text = history.srcAccount?.name ?: ""
                    binding.destination.text = history.destPerson?.name ?: ""
                }

                HistoryType.EXPENSE -> TODO()
                HistoryType.INCOME -> TODO()
                null -> TODO()
            }
        }
    }
}

private val callback = object : DiffUtil.ItemCallback<HistoryModel>() {
    override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean
    = oldItem.id == newItem.id && oldItem.type == newItem.type


    override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean = oldItem.equals(newItem)
}

class HistoryListAdapter: ListAdapter<HistoryModel, HistoryViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HistoryListItemBinding.inflate(inflater, parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = getItem(position)
        holder.bind(history)
    }
}