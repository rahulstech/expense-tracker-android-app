package dreammaker.android.expensetracker.ui.history.historyinput

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import dreammaker.android.expensetracker.database.GroupModel

val NoGroup: GroupModel = GroupModel(-1,"", null)

class GroupPickerViewHolder(itemView: View) {

    private val text1: TextView = itemView.findViewById(android.R.id.text1)

    fun bind(group: GroupModel) {
        text1.text = group.name
    }
}

class GroupPickerAdapter(context: Context): BaseAdapter() {

    private val inflater = LayoutInflater.from(context)

    private var _groups: List<GroupModel> = listOf(NoGroup)

    val groups: List<GroupModel> get() = _groups

    fun submitList(groups: List<GroupModel>) {
        _groups = ArrayList(groups).apply { add(0, NoGroup) }
    }

    override fun getCount(): Int = groups.size

    override fun getItem(position: Int): GroupModel = groups[position]

    override fun getItemId(position: Int): Long = groups[position].id!!

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        val group = getItem(position)
        val itemView: View
        val holder: GroupPickerViewHolder
        if (null != view) {
            holder = view.tag as GroupPickerViewHolder
            itemView = view
        }
        else {
            itemView = inflater.inflate(android.R.layout.simple_list_item_1,parent,false)
            holder = GroupPickerViewHolder(itemView)
            itemView.tag = holder
        }
        holder.bind(group)
        return itemView
    }
}