package dreammaker.android.expensetracker.ui.history.historieslist

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.ViewCompat
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.util.ClickableViewHolder
import dreammaker.android.expensetracker.util.getBackgroundColor
import dreammaker.android.expensetracker.util.getColorOnBackground
import dreammaker.android.expensetracker.util.getLabel
import dreammaker.android.expensetracker.util.getNonEmptyNote
import dreammaker.android.expensetracker.util.invisible
import dreammaker.android.expensetracker.util.toCurrencyString
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible

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
            view.text = history.getNonEmptyNote(context)
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
        when(history?.type) {
            HistoryType.TRANSFER, HistoryType.DEBIT -> {
                val sourceText = history.primaryAccount?.name
                view.text = boldFieldText(R.string.label_history_list_item_source_account,sourceText)
                view.visible()
            }
            else -> {
                view.visibilityGone()
                view.text = null
            }
        }
    }

    fun setDestination(view: TextView, history: HistoryModel?) {
        val destText = when(history?.type) {
            HistoryType.CREDIT-> history.primaryAccount?.name
            HistoryType.TRANSFER -> history.secondaryAccount?.name
            else -> null
        }
        if (null == destText) {
            view.text = null
            view.visibilityGone()
        }
        else {
            view.text = boldFieldText(R.string.label_history_list_item_destination_account,destText)
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

    private fun boldFieldText(@StringRes labelResId: Int, text: String?): CharSequence = boldFieldText(getString(labelResId),text)

    private fun boldFieldText(label: String?, text: String?): CharSequence {
        return buildSpannedString {
            append(label ?: "")
            append(" ")
            bold { append(text ?: "") }
        }
    }
}