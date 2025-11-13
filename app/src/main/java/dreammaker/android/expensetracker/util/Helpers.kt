package dreammaker.android.expensetracker.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class OperationResult<out T>(
    val output: T?,
    val error: Throwable?
) {
    fun isFailure(): Boolean = null != error
}


fun Number.toCurrencyString(currencyCode: String = "USD", textLocale: Locale = Locale.ENGLISH, ): String {
    val format = NumberFormat.getCurrencyInstance(textLocale)
    format.currency = Currency.getInstance(currencyCode)
    return format.format(toDouble())
}