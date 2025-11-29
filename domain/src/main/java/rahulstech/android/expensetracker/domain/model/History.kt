package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.HistoryType
import java.time.LocalDate

internal fun List<History.Type>.toHistoryTypesList(): List<HistoryType> = map { it.toHistoryType() }

sealed class History(
    open var id: Long,
    open val date: LocalDate,
    open val type: Type,
    open val amount: Float,
    open val primaryAccountId: Long?,
    open val secondaryAccountId: Long?,
    open val groupId: Long?,
    open val note: String?,
    open val primaryAccount: Account?,
    open val secondaryAccount: Account?,
    open val group: Group?,
) {

    enum class Type {
        CREDIT,
        DEBIT,
        TRANSFER,
        ;

        internal fun toHistoryType(): HistoryType = when(this) {
            CREDIT -> HistoryType.CREDIT
            DEBIT -> HistoryType.DEBIT
            TRANSFER -> HistoryType.TRANSFER
        }
    }

    fun toHistoryEntity(): HistoryEntity =
        HistoryEntity(id,type.toHistoryType(),primaryAccountId,secondaryAccountId,groupId, amount,date,note)

    data class CreditHistory(
        override var id: Long,
        override val date: LocalDate,
        override val amount: Float,
        override val primaryAccountId: Long,
        override val groupId: Long? = null,
        override val note: String? = null,
        override val primaryAccount: Account? = null,
        override val group: Group? = null,
    ): History(id,date,Type.CREDIT,amount,primaryAccountId,null,groupId,note,primaryAccount,null,group)

    data class DebitHistory(
        override var id: Long,
        override val date: LocalDate,
        override val amount: Float,
        override val primaryAccountId: Long,
        override val groupId: Long? = null,
        override val note: String? = null,
        override val primaryAccount: Account? = null,
        override val group: Group? = null,
    ): History(id,date,Type.DEBIT,amount,primaryAccountId,null,groupId,note,primaryAccount,null,group)

    data class TransferHistory(
        override var id: Long,
        override val date: LocalDate,
        override val amount: Float,
        override val primaryAccountId: Long,
        override val secondaryAccountId: Long,
        override val note: String? = null,
        override val primaryAccount: Account? = null,
        override val secondaryAccount: Account? = null,
    ): History(id,date,Type.DEBIT,amount,primaryAccountId,secondaryAccountId,null,note,primaryAccount,secondaryAccount,null)
}

fun HistoryEntity.toHistory(): History =
    when(type) {
        HistoryType.CREDIT -> {
            History.CreditHistory(id,date, amount,primaryAccountId!!,groupId,note)
        }
        HistoryType.DEBIT -> {
            History.DebitHistory(id,date,amount,primaryAccountId!!,groupId,note)
        }
        HistoryType.TRANSFER -> {
            History.TransferHistory(id,date,amount,primaryAccountId!!,secondaryAccountId!!,note)
        }
    }

fun HistoryDetails.toHistory(): History =
    when(history.type) {
        HistoryType.CREDIT -> {
            History.CreditHistory(history.id,history.date, history.amount,history.primaryAccountId!!,history.groupId, history.note,
                primaryAccount?.toAccount(),group?.toGroup())
        }
        HistoryType.DEBIT -> {
            History.DebitHistory(history.id,history.date, history.amount,history.primaryAccountId!!,history.groupId, history.note,
                primaryAccount?.toAccount(),group?.toGroup())
        }
        HistoryType.TRANSFER -> {
            History.TransferHistory(history.id,history.date, history.amount,history.primaryAccountId!!,history.secondaryAccountId!!, history.note,
                primaryAccount?.toAccount(),secondaryAccount?.toAccount())
        }
    }