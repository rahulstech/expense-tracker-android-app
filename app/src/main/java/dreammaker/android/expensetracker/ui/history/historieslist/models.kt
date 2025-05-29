package dreammaker.android.expensetracker.ui.history.historieslist

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import java.util.Locale

class HistoryFilterData {

    private var _types: Array<HistoryType>? = null

    fun setTypes(types: Array<HistoryType>?) {
        _types = types
    }

    fun match(history: HistoryModel): Boolean {
        val hasType = _types?.let { types -> history.type in types } ?: true
        return hasType
    }

    override fun toString(): String {
        return "HistoryFilterData(types=$_types)"
    }
}

data class HistorySummary(
    val totalCredit: Float = 0f,
    val totalDebit: Float = 0f
) {

    fun getTotalCreditText(
        context: Context,
        currencyCode: String = "USD",
        locale: Locale = Locale.ENGLISH
    ): CharSequence {
        return buildLabeledColoredAmountText(
            context = context,
            labelResId = R.string.label_total_credit,
            amount = totalCredit,
            colorRes = R.color.colorCredit,
            currencyCode = currencyCode,
            locale = locale
        )
    }

    fun getTotalDebitText(
        context: Context,
        currencyCode: String = "USD",
        locale: Locale = Locale.ENGLISH
    ): CharSequence {
        return buildLabeledColoredAmountText(
            context,
            R.string.label_total_debit,
            totalDebit,
            R.color.colorDebit,
            currencyCode,
            locale
        )
    }

    private fun buildLabeledColoredAmountText(
        context: Context,
        @StringRes labelResId: Int,
        amount: Float,
        @ColorRes colorRes: Int,
        currencyCode: String,
        locale: Locale
    ): CharSequence {
        val color = ResourcesCompat.getColor(context.resources, colorRes, context.theme)
        return buildSpannedString {
            append(context.getString(labelResId))
            append(" ")
            color(color) {
                append(amount.toCurrencyString(currencyCode, locale))
            }
        }
    }
}