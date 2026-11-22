package rahulstech.android.expensetracker.domain.model

import java.math.BigDecimal

data class HistoryTotalIncomeTotalExpense(
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
)
