package dreammaker.android.expensetracker.ui.history.historieslist

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.ViewCompat
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.util.ClickableViewHolder
import dreammaker.android.expensetracker.util.getTypeBackgroundColor
import dreammaker.android.expensetracker.util.getTypeColorOnBackground
import dreammaker.android.expensetracker.util.getTypeLabel
import dreammaker.android.expensetracker.util.invisible
import dreammaker.android.expensetracker.util.toCurrencyString
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import rahulstech.android.expensetracker.domain.model.History

abstract class BaseHistoryViewHolder<VH:BaseHistoryViewHolder<VH>>(
    itemView: View,
) :  ClickableViewHolder<VH>(itemView) {

    abstract fun bind(history: History?, selected: Boolean)

    fun setNote(view: TextView, history: History?) {
        if (null == history) {
            view.visibilityGone()
            view.text = null
        }
        else {
            view.text = history.note
            view.visible()
        }
    }

    fun setAmount(view: TextView, history: History?) {
        if (null == history) {
            view.visibilityGone()
            view.text = null
        }
        else {
            view.text = history.amount.toCurrencyString()
            view.visible()
        }
    }

    fun setSource(view: TextView, history: History?) {
        when(history) {
            is History.TransferHistory,
            is History.DebitHistory -> {
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

    fun setDestination(view: TextView, history: History?) {
        val destText = when(history) {
            is History.CreditHistory-> history.primaryAccount?.name
            is History.TransferHistory -> history.secondaryAccount?.name
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

    fun setType(view: TextView, history: History?) {
        if (history != null) {
            view.text = history.getTypeLabel(context)
            ViewCompat.setBackgroundTintList(view, history.getTypeBackgroundColor(context))
            view.setTextColor(history.getTypeColorOnBackground(context))
            view.visible()
        }
        else {
            view.visibilityGone()
            view.text = null
        }
    }

    fun setGroup(view: TextView, history: History?) {
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
            if (!text.isNullOrBlank()) {
                bold { append(text) }
            }
        }
    }
}