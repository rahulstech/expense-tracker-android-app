package rahulstech.android.expensetracker.backuprestore.util

import com.google.gson.annotations.SerializedName
import dreammaker.android.expensetracker.database.Account
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.Group
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.History
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.settings.SettingsModel
import dreammaker.android.expensetracker.settings.ViewHistory

data class AccountData(
    @SerializedName("id", alternate = ["_id"])
    val id: Long,
    @SerializedName("name", alternate = ["account_name"])
    val name: String,
    val balance: Float
) {
    fun toAccountModel(): AccountModel = AccountModel(id,name,balance)
}

data class GroupData(
    @SerializedName("id", alternate = ["_id"])
    val id: Long,
    @SerializedName("name", alternate = ["person_name"])
    val name: String,
    @SerializedName("balance", alternate = ["due"])
    val balance: Float
) {
    fun toGroupModel(): GroupModel = GroupModel(id,name,balance)
}

data class HistoryData(
    val id: Long,
    val type: HistoryType,
    val primaryAccountId: Long?,
    val secondaryAccountId: Long?,
    val groupId: Long?,
    val amount: Float,
    val date: Date,
    val note: String?,

    @Transient
    val deleted: Boolean = false
) {

    fun toHistoryModel(): HistoryModel
    = HistoryModel(id, type,
        primaryAccountId, secondaryAccountId, groupId, null, null, null,
        amount, date, note)
}

data class MoneyTransferData(
    val id: Long,
    val amount: Float,
    @SerializedName("when")
    val date: Date,
    @SerializedName("payer_account_id")
    val primaryAccountId: Long,
    @SerializedName("payee_account_id")
    val secondaryAccountId: Long,
    @SerializedName("description")
    val note: String?
) {
    fun toHistoryData(): HistoryData
    = HistoryData(id, HistoryType.TRANSFER, primaryAccountId, secondaryAccountId, null, amount, date, note)
}

data class TransactionData(
    @SerializedName("_id")
    val id: Long,
    val amount: Float,
    val date: Date,
    @SerializedName("account_id")
    val primaryAccountId: Long,
    @SerializedName("person_id")
    val groupId: Long?,
    @SerializedName("description")
    val note: String?,
    val type: Int,
    val deleted: Boolean
) {
    private fun getHistoryType(): HistoryType = if (type == 0) HistoryType.DEBIT else HistoryType.CREDIT

    fun toHistoryData(): HistoryData {
        return HistoryData(id,getHistoryType(), primaryAccountId, null, groupId, amount, date, note, deleted)
    }
}

data class AppSettingsData(
    val viewHistory: ViewHistory
) {
    fun toSettingsModel(): SettingsModel {
        return SettingsModel(viewHistory)
    }
}

fun Account.toAccountData(): AccountData {
    return AccountData(accountId,accountName,balance)
}

fun Group.toGroupData(): GroupData {
    return GroupData(id,name,balance)
}

fun History.toHistoryData(): HistoryData {
    return HistoryData(id,type,primaryAccountId,secondaryAccountId,groupId,amount,date,note)
}

fun SettingsModel.toSettingsData(): AppSettingsData {
    return AppSettingsData(viewHistory)
}
