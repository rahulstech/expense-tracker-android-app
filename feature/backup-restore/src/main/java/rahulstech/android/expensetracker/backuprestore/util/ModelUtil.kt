package rahulstech.android.expensetracker.backuprestore.util

import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate
import java.time.LocalDateTime

//data class AccountData(
//    val id: Long,
//    val name: String,
//    val balance: Float,
//    val lastUsed: LocalDateTime?,
//    val totalUsed: Long,
//    val isDefault: Boolean,
//) {
//    fun toAccount(): Account = Account(
//        id = id, name = name, balance = balance,
//        lastUsed = lastUsed, totalUsed = totalUsed, isDefault = isDefault
//    )
//}
//
//fun Account.toAccountData(): AccountData = AccountData(
//    id = id, name = name, balance = balance,
//    lastUsed = lastUsed, totalUsed = totalUsed, isDefault = isDefault
//)
//
//data class GroupData(
//    val id: Long,
//    val name: String,
//    val balance: Float,
//    val lastUsed: LocalDateTime?,
//    val totalUsed: Long,
//    val isDefault: Boolean,
//) {
//    fun toGroup(): Group = Group(
//        id = id,name = name, balance = balance,
//        lastUsed = lastUsed, totalUsed = totalUsed, isDefault = isDefault
//    )
//}
//
//fun Group.toGroupData(): GroupData = GroupData(
//    id = id,name = name, balance = balance,
//    lastUsed = lastUsed, totalUsed = totalUsed, isDefault = isDefault
//)

data class HistoryData(
    val id: Long,
    val type: History.Type,
    val amount: Float,
    val date: LocalDate,
    val primaryAccountId: Long?,
    val secondaryAccountId: Long? = null,
    val groupId: Long? = null,
    val note: String? = null,
) {
    fun toHistory(): History = when(type) {
        History.Type.CREDIT -> {
            History.CreditHistory(
                id = id,
                date = date,
                amount = amount,
                primaryAccountId = primaryAccountId ?: 0,
                groupId = groupId,
                note = note
            )
        }
        History.Type.DEBIT -> {
            History.DebitHistory(
                id = id,
                date = date,
                amount = amount,
                primaryAccountId = primaryAccountId ?: 0,
                groupId = groupId,
                note = note
            )
        }
        History.Type.TRANSFER -> {
            History.TransferHistory(
                id = id,
                date = date,
                amount = amount,
                primaryAccountId = primaryAccountId ?: 0,
                secondaryAccountId = secondaryAccountId ?: 0,
                note = note
            )
        }
    }
}

fun History.toHistoryData(): HistoryData = HistoryData(
    id = id,
    date = date,
    type = type,
    amount = amount,
    primaryAccountId = primaryAccountId,
    secondaryAccountId = secondaryAccountId,
    groupId = groupId,
    note = note
)
