package dreammaker.android.expensetracker.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import dreammaker.android.expensetracker.R
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate
import java.util.Locale

val UNKNOWN_ACCOUNT = Account("Unknown Account")

val UNKNOWN_GROUP = Group("Unknown Group")

class AccountParcelable(val id: Long, val name: String, val balance: Double): Parcelable {

    constructor(account: Account): this(
        account.id,account.name,account.balance
    )

    private constructor(parcel: Parcel): this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readDouble()
    )

    fun toAccount(): Account = Account(name,balance,id)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeDouble(balance)
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

class GroupParcelable(val id: Long, val name: String, val balance: Double): Parcelable {

    constructor(group: Group): this(
        group.id,
        group.name ,
        group.balance
    )

    private constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readDouble()
    )

    fun toGroup(): Group = Group(name,balance,id)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeDouble(balance)
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

internal fun Account.getBalanceText(context: Context, currencyCode: String = "USD", locale: Locale = Locale.ENGLISH): CharSequence {
    val balance = this.balance
    val balanceText = balance.toCurrencyString(currencyCode,locale)
    return if (balance < 0) {
        buildSpannedString {
            color(ResourcesCompat.getColor(context.resources, dreammaker.android.expensetracker.core.R.color.colorDebit, context.theme)) { append(balanceText) }
        }
    }
    else if (balance > 0) {
        buildSpannedString {
            color(ResourcesCompat.getColor(context.resources,dreammaker.android.expensetracker.core.R.color.colorCredit, context.theme)) { append(balanceText) }
        }
    }
    else {
        balanceText
    }
}

/**
 * Group Model Extension Methods
 */

// TODO: fix Group.getBalanceLabelResource and Group.getBalanceText after db migration
@StringRes
internal fun Group.getBalanceLabelResource(): Int = if (balance < 0.0) {
    R.string.extra_credit
}
else if (balance > 0.0) {
    R.string.extra_debit
}
else {
    R.string.balanced
}

internal fun Group.getBalanceText(context: Context, currencyCode: String = "USD", locale: Locale = Locale.ENGLISH): CharSequence {
    val balance = this.balance
    val balanceText = balance.toCurrencyString(currencyCode,locale)
    if (balance == 0.0) {
        return balanceText
    }
    val colorRes = if (balance < 0.0) {
        dreammaker.android.expensetracker.core.R.color.colorCredit
    }
    else {
        dreammaker.android.expensetracker.core.R.color.colorDebit
    }
    val color = ResourcesCompat.getColor(context.resources, colorRes, context.theme)
    return  buildSpannedString {
            color(color) { append(balanceText) }
        }
}

fun History.getTypeLabel(context: Context): String =
    when(this) {
        is History.CreditHistory -> context.getString(R.string.label_history_type_credit)
        is History.DebitHistory -> context.getString(R.string.label_history_type_debit)
        is History.TransferHistory -> context.getString(R.string.label_history_type_transfer)
    }

fun History.getTypeBackgroundColor(context: Context): ColorStateList {
    val resId = when(this) {
        is History.CreditHistory -> dreammaker.android.expensetracker.core.R.color.colorCredit
        is History.DebitHistory -> dreammaker.android.expensetracker.core.R.color.colorDebit
        is History.TransferHistory -> dreammaker.android.expensetracker.core.R.color.colorTransfer
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
        is History.CreditHistory -> dreammaker.android.expensetracker.core.R.color.colorOnCredit
        is History.DebitHistory -> dreammaker.android.expensetracker.core.R.color.colorOnDebit
        is History.TransferHistory -> dreammaker.android.expensetracker.core.R.color.colorOnTransfer
    }
    return ResourcesCompat.getColorStateList(context.resources, resId, context.theme)!!
}