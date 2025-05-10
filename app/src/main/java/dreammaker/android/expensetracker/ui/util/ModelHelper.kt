package dreammaker.android.expensetracker.ui.util

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.util.MonthYear
import java.util.Locale
import kotlin.math.absoluteValue

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

class GroupModelParcel(val id: Long, val name: String, val balance: Float): Parcelable {

    constructor(group: GroupModel): this(
        group.id ?: 0,
        group.name ?: "",
        group.balance ?: 0f
    )

    private constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readFloat()
    )

    fun toGroupModel(): GroupModel = GroupModel(id, name, balance)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeFloat(balance)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<GroupModelParcel> {
        override fun createFromParcel(parcel: Parcel): GroupModelParcel {
            return GroupModelParcel(parcel)
        }

        override fun newArray(size: Int): Array<GroupModelParcel?> {
            return arrayOfNulls(size)
        }
    }
}

fun Bundle.putDate(key: String, date: Date?) {
    putString(key, date?.toString())
}

fun Bundle.getDate(key: String, defaultDate: Date? = null): Date? {
    val dateString = getString(key, null)
    return if (null == dateString) defaultDate else Date.valueOf(dateString)
}

fun Bundle.putMonthYear(key: String, date: MonthYear?) {
    putString(key, date?.toString())
}

fun Bundle.getMonthYear(key: String, defaultDate: MonthYear? = null): MonthYear? {
    val value = getString(key, null)
    return if (null == value) defaultDate else MonthYear.valueOf(value)
}

fun Bundle.putHistoryType(key: String, type: HistoryType?) {
    putString(key, type?.name)
}

fun Bundle.getHistoryType(key: String, defaultType: HistoryType? = null): HistoryType? {
    val name = getString(key, null)
    return if (null == name) defaultType else HistoryType.valueOf(name)
}

/**
 * HistoryType Extension Method
 */
fun HistoryType.needsSecondaryAccount(): Boolean = this == HistoryType.TRANSFER

fun HistoryType.getLabel(context: Context): CharSequence {
    return when(this) {
        HistoryType.CREDIT -> context.getString(R.string.label_history_type_credit)
        HistoryType.DEBIT -> context.getString(R.string.label_history_type_debit)
        HistoryType.TRANSFER -> context.getString(R.string.label_history_type_transfer)
    }
}

fun HistoryType.getBackgroundColor(context: Context): ColorStateList {
    val resId = when(this) {
        HistoryType.CREDIT -> R.color.colorCredit
        HistoryType.DEBIT -> R.color.colorDebit
        HistoryType.TRANSFER -> R.color.colorTransfer
    }
    return ResourcesCompat.getColorStateList(context.resources, resId, context.theme)!!
}

fun HistoryType.getColorOnBackground(context: Context): ColorStateList {
    val resId = when(this) {
        HistoryType.CREDIT -> R.color.colorOnCredit
        HistoryType.DEBIT -> R.color.colorOnDebit
        HistoryType.TRANSFER -> R.color.colorOnTransfer
    }
    return ResourcesCompat.getColorStateList(context.resources, resId, context.theme)!!
}

fun HistoryModel.getNonEmptyNote(context: Context): CharSequence {
    if (note.isNullOrBlank()) {
        return type?.getLabel(context) ?: ""
    }
    return note!!
}

/**
 * AccountModel Extension Methods
 */

fun AccountModel.getBalanceText(context: Context, currencyCode: String = "USD", locale: Locale = Locale.ENGLISH): CharSequence {
    val balance = balance ?: 0f
    val balanceText = balance.toCurrencyString(currencyCode,locale)
    return if (balance < 0) {
        buildSpannedString {
            color(ResourcesCompat.getColor(context.resources,R.color.colorDebit, context.theme)) { append(balanceText) }
        }
    }
    else if (balance > 0) {
        buildSpannedString {
            color(ResourcesCompat.getColor(context.resources,R.color.colorCredit, context.theme)) { append(balanceText) }
        }
    }
    else {
        balanceText
    }
}

/**
 * Group Model Extension Methods
 */

fun GroupModel.getBalanceText(context: Context, currencyCode: String = "USD", locale: Locale = Locale.ENGLISH): CharSequence {
    val balance = balance ?: 0f
    val balanceText = balance.absoluteValue.toCurrencyString(currencyCode,locale)
    val coloredBalanceText = if (balance > 0) {
        buildSpannedString {
            color(ResourcesCompat.getColor(context.resources,R.color.colorDebit, context.theme)) { append(balanceText) }
        }
    }
    else if (balance < 0) {
        buildSpannedString {
            color(ResourcesCompat.getColor(context.resources,R.color.colorCredit, context.theme)) { append(balanceText) }
        }
    }
    else {
        balanceText
    }
    return coloredBalanceText
}

fun GroupModel.getBalanceLabel(context: Context): CharSequence {
    val balance = balance ?: 0f
    return if (balance > 0) context.getString(R.string.label_unsetteled)
    else if (balance < 0) context.getString(R.string.label_surplus)
    else ""
}
