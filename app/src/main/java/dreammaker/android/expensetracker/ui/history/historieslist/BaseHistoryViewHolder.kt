package dreammaker.android.expensetracker.ui.history.historieslist

import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.getBackgroundColor
import dreammaker.android.expensetracker.ui.util.getColorOnBackground
import dreammaker.android.expensetracker.ui.util.getLabel
import dreammaker.android.expensetracker.ui.util.invisible
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible

abstract class BaseHistoryViewHolder<VH:BaseHistoryViewHolder<VH>>(
    itemView: View,
) :  ClickableViewHolder<VH>(itemView) {

    abstract fun bind(history: HistoryModel?, selected: Boolean)

    fun setNote(view: TextView, history: HistoryModel?) {
        if (null == history) {
            view.visibilityGone()
            view.text = null
        }
        else {
            view.text = history.note ?: history.type?.getLabel(context)
            view.visible()
        }
    }

    fun setAmount(view: TextView, history: HistoryModel?) {
        if (null == history) {
            view.visibilityGone()
            view.text = null
        }
        else {
            view.text = history.amount?.toCurrencyString()
            view.visible()
        }
    }

    fun setSource(view: TextView, history: HistoryModel?) {
        val source = history?.primaryAccount?.name ?: ""
        val text = when (history?.type) {
            HistoryType.TRANSFER -> {
                getString(R.string.label_history_list_item_transfer_source, source)
            }
            HistoryType.DEBIT -> {
                getString(R.string.label_history_list_item_debit_source, source)
            }
            else -> null
        }
        if (text.isNullOrBlank()) {
            view.visibilityGone()
            view.text = null
        }
        else {
            view.text = text
            view.visible()
        }
    }

    fun setDestination(view: TextView, history: HistoryModel?) {
        val text = when (history?.type) {
            HistoryType.CREDIT -> {
                getString(R.string.label_history_list_item_credit_destination, history.primaryAccount?.name ?: "")
            }
            HistoryType.TRANSFER -> {
                getString(R.string.label_history_item_transfer_destination, history.secondaryAccount?.name ?: "")
            }
            else -> null
        }
        if (text.isNullOrBlank()) {
            view.visibilityGone()
            view.text = null
        }
        else {
            view.text = text
            view.visible()
        }
    }

    fun setType(view: TextView, history: HistoryModel?) {
        val type = history?.type
        if (type != null) {
            view.text = type.getLabel(context)
            ViewCompat.setBackgroundTintList(view, type.getBackgroundColor(context))
            view.setTextColor(type.getColorOnBackground(context))
            view.visible()
        }
        else {
            view.visibilityGone()
            view.text = null
        }
    }

    fun setGroup(view: TextView, history: HistoryModel?) {
        val group = history?.group
        if (null == group) {
            view.invisible()
            view.text = null
        }
        else {
            view.text = group.name
            view.visible()
        }
    }
}