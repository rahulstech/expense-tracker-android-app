package rahulstech.android.expensetracker.backuprestore.util

import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate


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
