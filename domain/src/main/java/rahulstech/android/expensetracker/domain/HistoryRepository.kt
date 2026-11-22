package rahulstech.android.expensetracker.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.HistoryTotalCreditTotalDebit
import rahulstech.android.expensetracker.domain.model.HistoryTotalIncomeTotalExpense
import java.time.YearMonth

interface HistoryRepository {

    suspend fun createHistory(history: History): History

    @Deprecated(message = "getHistoryById")
    suspend fun getHistory(id: Long): History?

    fun getHistoryById(id: Long): Flow<History?>

    fun getPagedHistories(params: HistoryFilterParameters): Flow<PagingData<History>>

    fun getTotalCreditDebit(params: HistoryFilterParameters): Flow<HistoryTotalCreditTotalDebit>

    fun getTotalIncomeExpense(start: YearMonth, end: YearMonth): Flow<HistoryTotalIncomeTotalExpense>

    suspend fun editHistory(history: History): Boolean

    suspend fun removeHistory(id: Long, reset: Boolean = true)

    suspend fun removeMultipleHistories(ids: List<Long>, reset: Boolean = true)
}
