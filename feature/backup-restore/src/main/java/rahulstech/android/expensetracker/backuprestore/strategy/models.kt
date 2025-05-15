package rahulstech.android.expensetracker.backuprestore.strategy

import com.google.gson.annotations.SerializedName
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType

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
    val type: String,
    val primaryAccountId: Long?,
    val secondaryAccountId: Long?,
    val groupId: Long?,
    val amount: Float,
    val date: String,
    val note: String?
) {

    fun toHistoryModel(): HistoryModel
    = HistoryModel(id, HistoryType.valueOf(type),
        primaryAccountId, secondaryAccountId, groupId, null, null, null,
        amount, Date.valueOf(date), note)
}

data class MoneyTransferData(
    val id: Long,
    val amount: Float,
    @SerializedName("when")
    val date: String,
    @SerializedName("payer_account_id")
    val primaryAccountId: Long,
    @SerializedName("payee_account_id")
    val secondaryAccountId: Long,
    @SerializedName("description")
    val note: String?
) {
    fun toHistoryModel(): HistoryModel
    = HistoryModel(id, HistoryType.TRANSFER,
        primaryAccountId, secondaryAccountId, null, null, null, null,
        amount, Date.valueOf(date), note)
}

data class TransactionData(
    @SerializedName("_id")
    val id: Long,
    val amount: Float,
    val date: String,
    @SerializedName("account_id")
    val primaryAccountId: Long,
    @SerializedName("person_id")
    val groupId: Long?,
    @SerializedName("description")
    val note: String?,
    val type: Int,
    val deleted: Boolean
) {
    fun toHistoryModel(): HistoryModel? {
        if (deleted) {
            return null
        }
        val historyType = if (type == 0) HistoryType.DEBIT else HistoryType.CREDIT
        return HistoryModel(
            id, historyType,
            primaryAccountId, null, groupId, null, null, null,
            amount, Date.valueOf(date), note
        )
    }
}
