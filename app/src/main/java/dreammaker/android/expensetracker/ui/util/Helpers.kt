package dreammaker.android.expensetracker.ui.util

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class OperationResult<out T>(
    val output: T?,
    val error: Throwable?
) {
    fun isFailure(): Boolean = null != error
}

object Constants {
    const val ARG_DESTINATION_LABEL = "arg.destination_label"
    const val ARG_INITIAL_SELECTION = "arg.initial_selection"
    const val ARG_RESULT_KEY = "arg.tag"
    const val ARG_ACTION = "arg_action"
    const val ARG_ID = "arg_id"
    const val ARG_ACCOUNT = "arg_account"
    const val ARG_GROUP = "arg_group"
    const val ACTION_CREATE = "action_create"
    const val ACTION_EDIT = "action_edit"
}

fun Fragment.setActivityTitle(title: CharSequence) {
    val activity = activity ?: return
    if (activity is AppCompatActivity) {
        if (null == activity.supportActionBar) {
            activity.title = title
        }
        else {
            activity.supportActionBar?.title = title
        }
    }
    else {
        activity.title = title
    }
}

fun Fragment.hasArgument(key: String): Boolean = arguments?.containsKey(key) ?: false

fun Bundle.getIfContains(key: String, defaultValue: Any? = null): Any? {
    if (containsKey(key)) {
        return get(key)
    }
    return defaultValue
}

fun Number.toCurrencyString(currencyCode: String = "USD", textLocale: Locale = Locale.ENGLISH, ): String {
    val format = NumberFormat.getCurrencyInstance(textLocale)
    format.currency = Currency.getInstance(currencyCode)
    return format.format(toDouble())
}

