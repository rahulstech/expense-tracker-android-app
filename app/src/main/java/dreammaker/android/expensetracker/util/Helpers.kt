package dreammaker.android.expensetracker.util

import android.content.Context
import android.icu.util.Currency
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
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


fun Number.toCurrencyString(
    currencyCode: String = "INR",
    textLocale: Locale = Locale.forLanguageTag("en-IN")
): String {
    val bigDecimal = toString().toBigDecimal()
    val currency = Currency.getInstance(currencyCode)
    val formatter = NumberFormat.getNumberInstance(textLocale) as DecimalFormat

    formatter.apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
        isGroupingUsed = true
        roundingMode = RoundingMode.HALF_UP
    }

    val sign = if (bigDecimal.signum() < 0) "-" else ""
    val currencySymbol = currency.getSymbol(textLocale)
    val formattedNumber = formatter.format(bigDecimal.abs())

    return "$currencySymbol$sign$formattedNumber"
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

fun String.isNumber(): Boolean =
    this.matches(Regex("(-)?\\d+(\\.\\d+)?"))