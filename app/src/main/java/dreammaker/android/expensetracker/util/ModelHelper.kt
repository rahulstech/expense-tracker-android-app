package dreammaker.android.expensetracker.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.BundleCompat
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

class AccountParcelable(val id: Long, val name: String, val balance: Float): Parcelable {

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

    companion object CREATOR : Parcelable.Creator<AccountParcelable> {
        override fun createFromParcel(parcel: Parcel): AccountParcelable {
            return AccountParcelable(parcel)
        }

        override fun newArray(size: Int): Array<AccountParcelable?> {
            return arrayOfNulls(size)
        }
    }
}

class GroupParcelable(val id: Long, val name: String, val balance: Float): Parcelable {

    constructor(group: Group): this(
        group.id,
        group.name ,
        group.balance
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

    companion object CREATOR : Parcelable.Creator<GroupParcelable> {
        override fun createFromParcel(parcel: Parcel): GroupParcelable {
            return GroupParcelable(parcel)
        }

        override fun newArray(size: Int): Array<GroupParcelable?> {
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

fun Bundle.putMonthYear(key: String, month: YearMonth?) {
    putString(key, month?.toString())
}

fun Bundle.getMonthYear(key: String, defaultMonth: YearMonth? = null): YearMonth? =
    getString(key, null)?.let {
        YearMonth.parse(it)
    } ?: defaultMonth

fun Bundle.putAccountParcelable(key: String, account: AccountParcelable) {
    putParcelable(key,account)
}

fun Bundle.getAccountParcelable(key: String): AccountParcelable? =
    BundleCompat.getParcelable(this,key, AccountParcelable::class.java)

fun Bundle.putGroupParcelable(key: String, group: GroupParcelable) {
    putParcelable(key,group)
}

fun Bundle.getGroupParcelable(key: String): GroupParcelable? =
    BundleCompat.getParcelable(this,key, GroupParcelable::class.java)

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

fun Group.getBalanceText(context: Context, currencyCode: String = "USD", locale: Locale = Locale.ENGLISH): CharSequence {
    val balance = this.balance
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
    val balance = this.balance.toFloat()
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

fun History.getTypeBackground(context: Context): Drawable {
    val resId = when(this) {
        is History.CreditHistory -> R.drawable.history_type_credit_background
        is History.DebitHistory -> R.drawable.history_type_dedit_background
        is History.TransferHistory -> R.drawable.history_type_transfer_background
    }
    return ResourcesCompat.getDrawable(context.resources, resId, context.theme)!!
}

fun History.getTypeColorOnBackground(context: Context): ColorStateList {
    val resId = when(this) {
        is History.CreditHistory -> R.color.colorOnCredit
        is History.DebitHistory -> R.color.colorOnDebit
        is History.TransferHistory -> R.color.colorOnTransfer
    }
    return ResourcesCompat.getColorStateList(context.resources, resId, context.theme)!!
}
