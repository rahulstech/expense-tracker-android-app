package dreammaker.android.expensetracker.ui.util

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.database.PersonModel

object Constants {
    const val ARG_DESTINATION_LABEL = "arg.destination_label"
    const val ARG_INITIAL_SELECTIONS = "arg.initial_selections"
    const val ARG_RESULT_KEY = "arg.tag"
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

fun Bundle.putDate(key: String, date: Date?) {
    putString(key, date?.toString())
}

fun Bundle.getDate(key: String, defaultDate: Date? = null): Date? {
    val dateString = getString(key, null)
    return if (null == dateString) defaultDate else Date.valueOf(dateString)
}

fun Bundle.putHistoryType(key: String, type: HistoryType?) {
    putString(key, type?.name)
}

fun Bundle.getHistoryType(key: String, defaultType: HistoryType? = null): HistoryType? {
    val name = getString(key, null)
    return if (null == name) defaultType else HistoryType.valueOf(name)
}

fun Bundle.putAccountModel(key: String, value: AccountModel?) {
    if (null == value) {
        return
    }
    putBoolean(key,true)
    value.id?.let { putLong("$key::__id", it) }
    value.name?.let { putString("$key::__name", it) }
    value.balance?.let { putFloat("$key::__balance",it) }
}

fun Bundle.getAccountModel(key: String, defaultValue: AccountModel? = null): AccountModel? {
    if (getBoolean(key, false)) {
        val id = getIfContains("$key::__id") as Long?
        val name = getIfContains("$key::__name") as String?
        val balance = getIfContains("$key::__balance") as Float?
        return AccountModel(id, name, balance)
    }
    return defaultValue
}

fun Bundle.putPersonModel(key: String, value: PersonModel?) {
    if (null == value) {
        return
    }
    putBoolean(key,true)
    value.id?.let { putLong("$key::__id", it) }
    value.name?.let { putString("$key::__name", it) }
    value.due?.let { putFloat("$key::__due",it) }
}

fun Bundle.getPersonModel(key: String, defaultValue: PersonModel? = null): PersonModel? {
    if (getBoolean(key, false)) {
        val id = getIfContains("$key::__id") as Long?
        val name = getIfContains("$key::__name") as String?
        val due = getIfContains("$key::__due") as Float?
        return PersonModel(id, name, due)
    }
    return defaultValue
}

fun Bundle.getIfContains(key: String, defaultValue: Any? = null): Any? {
    if (containsKey(key)) {
        return get(key)
    }
    return defaultValue
}
