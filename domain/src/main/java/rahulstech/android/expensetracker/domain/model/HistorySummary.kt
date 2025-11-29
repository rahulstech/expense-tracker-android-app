package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.TotalCreditDebit

data class HistoryTotalCreditTotalDebit(
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0,
)

fun TotalCreditDebit.toHistoryTotalCreditTotalDebit(): HistoryTotalCreditTotalDebit =
    HistoryTotalCreditTotalDebit(
        totalCredit = this.totalCredit,
        totalDebit = this.totalDebit,
    )