package rahulstech.android.expensetracker.backuprestore.util

import com.google.gson.annotations.SerializedName
import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.settings.SettingsModel

data class AccountData(
    @SerializedName("id", alternate = ["_id"])
    val id: Long,
    @SerializedName("name", alternate = ["account_name"])
    val name: String,
    val balance: Float
) {
//    fun toAccountModel(): AccountModel = AccountModel(id,name,balance)
}

data class GroupData(
    @SerializedName("id", alternate = ["_id"])
    val id: Long,
    @SerializedName("name", alternate = ["person_name"])
    val name: String,
    @SerializedName("balance", alternate = ["due"])
    val balance: Float
) {
//    fun toGroupModel(): GroupModel = GroupModel(id,name,balance)
}

data class HistoryData(
    val id: Long,
    val type: Any,
    val primaryAccountId: Long?,
    val secondaryAccountId: Long?,
    val groupId: Long?,
    val amount: Float,
    val date: Any,
    val note: String?,

    @Transient
    val deleted: Boolean = false
) {

//    fun toHistoryModel(): HistoryModel
//    = HistoryModel(id, type,
//        primaryAccountId, secondaryAccountId, groupId, null, null, null,
//        amount, date, note)
}

data class MoneyTransferData(
    val id: Long,
    val amount: Float,
    @SerializedName("when")
    val date: Any,
    @SerializedName("payer_account_id")
    val primaryAccountId: Long,
    @SerializedName("payee_account_id")
    val secondaryAccountId: Long,
    @SerializedName("description")
    val note: String?
) {
    fun toHistoryData(): HistoryData
    = HistoryData(id, Any(), primaryAccountId, secondaryAccountId, null, amount, date, note)
}

data class TransactionData(
    @SerializedName("_id")
    val id: Long,
    val amount: Float,
    val date: Any,
    @SerializedName("account_id")
    val primaryAccountId: Long,
    @SerializedName("person_id")
    val groupId: Long?,
    @SerializedName("description")
    val note: String?,
    val type: Int,
    val deleted: Boolean
) {
    private fun getHistoryType(): Any = Any()

    fun toHistoryData(): HistoryData {
        return HistoryData(id,getHistoryType(), primaryAccountId, null, groupId, amount, date, note, deleted)
    }
}

class AppSettingsData {
    
    fun toSettingsModel(): SettingsModel {
        return SettingsModel()
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is AppSettingsData
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

fun AccountEntity.toAccountData(): AccountData {
    return AccountData(id,name,balance)
}

fun GroupEntity.toGroupData(): GroupData {
    return GroupData(id,name,due)
}

fun HistoryEntity.toHistoryData(): HistoryData {
    return HistoryData(id,type,primaryAccountId,secondaryAccountId,groupId,amount,date,note)
}

fun SettingsModel.toSettingsData(): AppSettingsData {
    return AppSettingsData()
}
