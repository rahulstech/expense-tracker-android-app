package dreammaker.android.expensetracker.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun Number.toCurrencyString(currencyCode: String = "USD", textLocale: Locale = Locale.ENGLISH): String {
    val format = NumberFormat.getCurrencyInstance(textLocale)
    format.currency = Currency.getInstance(currencyCode)
    return format.format(toDouble())
}

fun <T : R, R : Any> List<T>.insertSeparator(
    generator: (before: T?, after: T?) -> R?
): List<R> {
    if (isEmpty()) emptyList<R>()

    val output = mutableListOf<R>()
    for(i in indices) {
        val before = if (i==0) null else this[i-1]
        val after = this[i]

        generator(before,after)?.let { output.add(it) }
        output.add(after)
    }

    return output
}