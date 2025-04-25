package dreammaker.android.expensetracker.ui.util

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryType
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

class AccountModelParcel(val id: Long, val name: String, val balance: Float): Parcelable {

    constructor(account: AccountModel): this(
        account.id ?: 0,account.name ?: "",account.balance ?: 0f
    )

    private constructor(parcel: Parcel): this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readFloat()
    )

    fun toAccountModel(): AccountModel = AccountModel(id, name, balance)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeFloat(balance)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AccountModelParcel> {
        override fun createFromParcel(parcel: Parcel): AccountModelParcel {
            return AccountModelParcel(parcel)
        }

        override fun newArray(size: Int): Array<AccountModelParcel?> {
            return arrayOfNulls(size)
        }
    }
}
