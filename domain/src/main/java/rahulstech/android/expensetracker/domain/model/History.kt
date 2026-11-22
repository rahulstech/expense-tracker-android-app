package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.HistoryEntity
import java.math.BigDecimal
import java.time.LocalDate

sealed class History(
    open val id: Long,
    open val date: LocalDate,

    internal open val type: HistoryType,
    open val amount: BigDecimal,
    open val primaryAccountId: Long?,
    open val secondaryAccountId: Long?,
    open val groupId: Long?,
    open val note: String?,
    open val primaryAccount: Account?,
    open val secondaryAccount: Account?,
    open val group: Group?
) {

    internal abstract fun copyId(id: Long): History

    internal fun toHistoryEntity(): HistoryEntity =
        HistoryEntity(id,type.name,primaryAccountId,secondaryAccountId,groupId,amount,date,note)

    data class CreditHistory(
        override val id: Long,
        override val date: LocalDate,
        override val amount: BigDecimal,
        override val primaryAccountId: Long,
        override val groupId: Long? = null,
        override val note: String? = null,
        override val primaryAccount: Account? = null,
        override val group: Group? = null,
    ): History(
        id = id,
        date = date,
        type = HistoryType.CREDIT,
        amount = amount,
        primaryAccountId = primaryAccountId,
        secondaryAccountId = null,
        groupId = groupId,
        note = note,
        primaryAccount = primaryAccount,
        secondaryAccount = null,
        group = group,
    ) {
        override fun copyId(id: Long): History = this.copy(id=id)
    }

    data class DebitHistory(
        override var id: Long,
        override val date: LocalDate,
        override val amount: BigDecimal,
        override val primaryAccountId: Long,
        override val groupId: Long? = null,
        override val note: String? = null,
        override val primaryAccount: Account? = null,
        override val group: Group? = null,
    ): History(
        id = id,
        date = date,
        type = HistoryType.DEBIT,
        amount = amount,
        primaryAccountId = primaryAccountId,
        secondaryAccountId = null,
        groupId = groupId,
        note = note,
        primaryAccount = primaryAccount,
        secondaryAccount = null,
        group = group
    ) {
        override fun copyId(id: Long): History = this.copy(id=id)
    }


    data class CreditIncomeHistory(
        override val id: Long,
        override val date: LocalDate,
        override val amount: BigDecimal,
        override val primaryAccountId: Long,
        override val note: String? = null,
        override val primaryAccount: Account? = null,
    ): History(id,date,HistoryType.CREDIT,amount,primaryAccountId,null,null,note,primaryAccount,null,null) {

        override fun copyId(id: Long): History = this.copy(id=id)
    }

    data class DebitExpenseHistory(
        override var id: Long,
        override val date: LocalDate,
        override val amount: BigDecimal,
        override val primaryAccountId: Long,
        override val note: String? = null,
        override val primaryAccount: Account? = null,
    ): History(
        id = id,
        date = date,
        type = HistoryType.DEBIT,
        amount = amount,
        primaryAccountId = primaryAccountId,
        secondaryAccountId = null,
        groupId = null,
        note = note,
        primaryAccount = primaryAccount,
        secondaryAccount = null,
        group = null
    ) {
        override fun copyId(id: Long): History = this.copy(id=id)
    }



    data class TransferHistory(
        override var id: Long,
        override val date: LocalDate,
        override val amount: BigDecimal,
        override val primaryAccountId: Long,
        override val secondaryAccountId: Long,
        override val note: String? = null,
        override val primaryAccount: Account? = null,
        override val secondaryAccount: Account? = null,
    ): History(
        id = id,
        date = date,
        type = HistoryType.TRANSFER,
        amount = amount,
        primaryAccountId = primaryAccountId,
        secondaryAccountId = secondaryAccountId,
        groupId = null,
        note = note,
        primaryAccount = primaryAccount,
        secondaryAccount = secondaryAccount,
        group = null
    ) {
        override fun copyId(id: Long): History = this.copy(id=id)
    }
}

