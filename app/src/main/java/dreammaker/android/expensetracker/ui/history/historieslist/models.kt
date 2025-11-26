package dreammaker.android.expensetracker.ui.history.historieslist

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.util.toCurrencyString
import rahulstech.android.expensetracker.domain.model.History
import java.util.Locale

data class HistorySummary(
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0
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
        amount: Double,
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