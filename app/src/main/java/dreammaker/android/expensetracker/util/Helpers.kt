package dreammaker.android.expensetracker.util

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun buildLabeledColoredAmountText(
    context: Context,
    amount: Number,
    @ColorRes colorRes: Int,
    currencyCode: String,
    locale: Locale
): CharSequence {
    val color = ResourcesCompat.getColor(context.resources, colorRes, context.theme)
    return buildSpannedString {
        color(color) {
            append(amount.toCurrencyString(currencyCode, locale))
        }
    }
}

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