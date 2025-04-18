package dreammaker.android.expensetracker.util

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan

fun boldText(text: CharSequence?): CharSequence {
    if (text.isNullOrBlank()) return ""
    val spannableText = SpannableString(text)
    spannableText.setSpan(StyleSpan(Typeface.BOLD),0,text.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    return spannableText
}

