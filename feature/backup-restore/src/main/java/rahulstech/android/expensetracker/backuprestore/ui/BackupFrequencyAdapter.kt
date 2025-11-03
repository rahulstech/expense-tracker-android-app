package rahulstech.android.expensetracker.backuprestore.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import rahulstech.android.expensetracker.backuprestore.settings.BackupFrequency

class BackupFrequencyAdapter: BaseAdapter() {

    private val frequencies = BackupFrequency.entries.toTypedArray()

    override fun getCount(): Int = frequencies.size

    fun getPosition(frequency: BackupFrequency): Int = frequency.ordinal

    override fun getItem(position: Int): BackupFrequency = frequencies[position]

    override fun getItemId(position: Int): Long = frequencies[position].hashCode().toLong()

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val itemView: View
        val text1: TextView
        if (null == view) {
            itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
            text1 = itemView.findViewById(android.R.id.text1)
            itemView.tag = text1
        }
        else {
            itemView = view
            text1 = view.tag as TextView
        }
        text1.text = getItem(position).getLabel(parent.context)
        return itemView
    }

    override fun getDropDownView(position: Int, view: View?, parent: ViewGroup): View {
        val itemView: View
        val text1: TextView
        if (null == view) {
            itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_single_choice, parent, false)
            text1 = itemView.findViewById(android.R.id.text1)
            itemView.tag = text1
        }
        else {
            itemView = view
            text1 = view.tag as TextView
        }
        text1.text = getItem(position).getLabel(parent.context)
        return itemView
    }
}