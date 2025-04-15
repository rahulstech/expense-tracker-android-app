package dreammaker.android.expensetracker.util

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import dreammaker.android.expensetracker.R

fun boldText(text: CharSequence): CharSequence {
    val spannableText = SpannableString(text)
    spannableText.setSpan(StyleSpan(Typeface.BOLD),0,text.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    return spannableText
}

