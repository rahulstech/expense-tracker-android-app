package dreammaker.android.expensetracker.util

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import dreammaker.android.expensetracker.R
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import kotlin.math.absoluteValue

val UNKNOWN_ACCOUNT = Account("Unknown Account")

val UNKNOWN_GROUP = Group("Unknown Group")

//class AccountModelParcel(val id: Long, val name: String, val balance: Float): Parcelable {
//
//    constructor(account: AccountModel): this(
//        account.id ?: 0,account.name ?: "",account.balance ?: 0f
//    )
//
//    private constructor(parcel: Parcel): this(
//        parcel.readLong(),
//        parcel.readString()!!,
//        parcel.readFloat()
//    )
//
//    fun toAccountModel(): AccountModel = AccountModel(id, name, balance)
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeLong(id)
//        parcel.writeString(name)
//        parcel.writeFloat(balance)
//    }
//
//    override fun describeContents(): Int = 0
//
//    companion object CREATOR : Parcelable.Creator<AccountModelParcel> {
//        override fun createFromParcel(parcel: Parcel): AccountModelParcel {
//            return AccountModelParcel(parcel)
//        }
//
//        override fun newArray(size: Int): Array<AccountModelParcel?> {
//            return arrayOfNulls(size)
//        }
//    }
//}
//
//class GroupModelParcel(val id: Long, val name: String, val balance: Float): Parcelable {
//
//    constructor(group: GroupModel): this(
//        group.id ?: 0,
//        group.name ?: "",
//        group.balance ?: 0f
//    )
//
//    private constructor(parcel: Parcel) : this(
//        parcel.readLong(),
//        parcel.readString()!!,
//        parcel.readFloat()
//    )
//
//    fun toGroupModel(): GroupModel = GroupModel(id, name, balance)
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeLong(id)
//        parcel.writeString(name)
//        parcel.writeFloat(balance)
//    }
//
//    override fun describeContents(): Int = 0
//
//    companion object CREATOR : Parcelable.Creator<GroupModelParcel> {
//        override fun createFromParcel(parcel: Parcel): GroupModelParcel {
//            return GroupModelParcel(parcel)
//        }
//
//        override fun newArray(size: Int): Array<GroupModelParcel?> {
//            return arrayOfNulls(size)
//        }
//    }
//}

class AccountParcel(val id: Long, val name: String, val balance: Number): Parcelable {

    constructor(account: Account): this(
        account.id,account.name,account.balance
    )

    private constructor(parcel: Parcel): this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readFloat()
    )

    fun toAccount(): Account = Account(name,balance,id)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeFloat(balance.toFloat())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AccountParcel> {
        override fun createFromParcel(parcel: Parcel): AccountParcel {
            return AccountParcel(parcel)
        }

        override fun newArray(size: Int): Array<AccountParcel?> {
            return arrayOfNulls(size)
        }
    }
}

class GroupParcel(val id: Long, val name: String, val balance: Number): Parcelable {

    constructor(group: Group): this(
        group.id,
        group.name ,
        group.due
    )

    private constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readFloat()
    )

    fun toGroup(): Group = Group(name,balance,id)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeFloat(balance.toFloat())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<GroupParcel> {
        override fun createFromParcel(parcel: Parcel): GroupParcel {
            return GroupParcel(parcel)
        }

        override fun newArray(size: Int): Array<GroupParcel?> {
            return arrayOfNulls(size)
        }
    }
}

fun Bundle.putDate(key: String, date: LocalDate?) {
    putString(key, date?.toString())
}

fun Bundle.getDate(key: String, defaultDate: LocalDate? = null): LocalDate? {
    val dateString = getString(key, null)
    return if (null == dateString) defaultDate else LocalDate.parse(dateString)
}

fun Bundle.putMonthYear(key: String, date: YearMonth?) {
    putString(key, date?.toString())
}

fun Bundle.getMonthYear(key: String, defaultDate: YearMonth? = null): YearMonth? {
    val value = getString(key, null)
    return if (null == value) defaultDate else YearMonth.parse(value)
}

//fun Bundle.putHistoryType(key: String, type: HistoryType?) {
//    putString(key, type?.name)
//}
//
//fun Bundle.getHistoryType(key: String, defaultType: HistoryType? = null): HistoryType? {
//    val name = getString(key, null)
//    return if (null == name) defaultType else HistoryType.valueOf(name)
//}

/**
 * HistoryType Extension Method
 */
//fun HistoryType.needsSecondaryAccount(): Boolean = this == HistoryType.TRANSFER
//
//fun HistoryType.getLabel(context: Context): String {
//    return when(this) {
//        HistoryType.CREDIT -> context.getString(R.string.label_history_type_credit)
//        HistoryType.DEBIT -> context.getString(R.string.label_history_type_debit)
//        HistoryType.TRANSFER -> context.getString(R.string.label_history_type_transfer)
//    }
//}
//
//fun HistoryType.getBackgroundColor(context: Context): ColorStateList {
//    val resId = when(this) {
//        HistoryType.CREDIT -> R.color.colorCredit
//        HistoryType.DEBIT -> R.color.colorDebit
//        HistoryType.TRANSFER -> R.color.colorTransfer
//    }
//    return ResourcesCompat.getColorStateList(context.resources, resId, context.theme)!!
//}
//
//fun HistoryType.getColorOnBackground(context: Context): ColorStateList {
//    val resId = when(this) {
//        HistoryType.CREDIT -> R.color.colorOnCredit
//        HistoryType.DEBIT -> R.color.colorOnDebit
//        HistoryType.TRANSFER -> R.color.colorOnTransfer
//    }
//    return ResourcesCompat.getColorStateList(context.resources, resId, context.theme)!!
//}
//
//fun HistoryModel.getNonEmptyNote(context: Context): CharSequence {
//    if (note.isNullOrBlank()) {
//        return type?.getLabel(context) ?: ""
//    }
//    return note!!
//}

/**
 * AccountModel Extension Methods
 */

fun Account.getBalanceText(context: Context, currencyCode: String = "USD", locale: Locale = Locale.ENGLISH): CharSequence {
    val balance = this.balance.toFloat()
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

fun Group.getDueText(context: Context, currencyCode: String = "USD", locale: Locale = Locale.ENGLISH): CharSequence {
    val balance = this.due.toFloat()
    val balanceText = balance.absoluteValue.toCurrencyString(currencyCode,locale)
    return if (balance > 0) {
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
}

fun Group.getDueLabel(context: Context): CharSequence {
    val balance = this.due.toFloat()
    // TODO: compare up to 2 decimal
    return if (balance > 0) context.getString(R.string.label_unsettled)
    else if (balance < 0) context.getString(R.string.label_surplus)
    else ""
}

fun History.getTypeLabel(context: Context): String =
    when(this) {
        is History.CreditHistory -> context.getString(R.string.label_history_type_credit)
        is History.DebitHistory -> context.getString(R.string.label_history_type_debit)
        is History.TransferHistory -> context.getString(R.string.label_history_type_transfer)
    }

fun History.getTypeBackgroundColor(context: Context): ColorStateList {
    val resId = when(this) {
        is History.CreditHistory -> R.color.colorCredit
        is History.DebitHistory -> R.color.colorDebit
        is History.TransferHistory -> R.color.colorTransfer
    }
    return ResourcesCompat.getColorStateList(context.resources, resId, context.theme)!!
}

fun History.getTypeColorOnBackground(context: Context): ColorStateList {
    val resId = when(this) {
        is History.CreditHistory -> R.color.colorOnCredit
        is History.DebitHistory -> R.color.colorOnDebit
        is History.TransferHistory -> R.color.colorOnTransfer
    }
    return ResourcesCompat.getColorStateList(context.resources, resId, context.theme)!!
}
