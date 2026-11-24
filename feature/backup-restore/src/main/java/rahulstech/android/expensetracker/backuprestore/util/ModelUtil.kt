package rahulstech.android.expensetracker.backuprestore.util

import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.HistoryType
import java.math.BigDecimal
import java.time.LocalDate


data class HistoryData(
    val id: Long,
    val type: HistoryType,
    val amount: Double,
    val date: LocalDate,
    val primaryAccountId: Long?,
    val secondaryAccountId: Long? = null,
    val groupId: Long? = null,
    val note: String? = null,
) {
    fun toHistory(): History = when(type) {
        HistoryType.CREDIT -> {
            History.CreditHistory(
                id = id,
                date = date,
                amount = BigDecimal.ZERO,
                primaryAccountId = primaryAccountId ?: 0,
                groupId = groupId,
                note = note
            )
        }
        HistoryType.DEBIT -> {
            History.DebitHistory(
                id = id,
                date = date,
                amount = BigDecimal.ZERO,
                primaryAccountId = primaryAccountId ?: 0,
                groupId = groupId,
                note = note
            )
        }
        HistoryType.TRANSFER -> {
            History.TransferHistory(
                id = id,
                date = date,
                amount = BigDecimal.ZERO,
                primaryAccountId = primaryAccountId ?: 0,
                secondaryAccountId = secondaryAccountId ?: 0,
                note = note
            )
        }

        else -> {
            History.TransferHistory(
                id = id,
                date = date,
                amount = BigDecimal.ZERO,
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
    amount = 0.0,
    primaryAccountId = primaryAccountId,
    secondaryAccountId = secondaryAccountId,
    groupId = groupId,
    note = note
)
