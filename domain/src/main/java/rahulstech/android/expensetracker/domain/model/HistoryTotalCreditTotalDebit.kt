package rahulstech.android.expensetracker.domain.model

import java.math.BigDecimal

data class HistoryTotalCreditTotalDebit(
    val totalCredit: BigDecimal = BigDecimal.ZERO,
    val totalDebit: BigDecimal = BigDecimal.ZERO,
)
