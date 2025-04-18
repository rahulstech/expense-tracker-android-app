package dreammaker.android.expensetracker.ui.history.viewhistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.database.HistoryModel

abstract class HistoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    open fun bind(history: HistoryModel?) {}

    protected fun getString(@StringRes id: Int, vararg args: Any): String = itemView.context.getString(id, *args)
}

private val callback = object : DiffUtil.ItemCallback<HistoryModel>() {

    override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean
    = oldItem.id == newItem.id && oldItem.type == newItem.type

    override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean
    = oldItem.equals(newItem)
}

abstract class HistoryListAdapter<VH: HistoryViewHolder>: ListAdapter<HistoryModel, VH>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        return onCreateHistoryViewHolder(inflater,parent,viewType)
    }

    abstract fun onCreateHistoryViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VH

    override fun onBindViewHolder(holder: VH, position: Int) {
        val history = getItem(position)
        holder.bind(history)
    }
}