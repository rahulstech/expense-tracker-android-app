package rahulstech.android.expensetracker.domain

import androidx.lifecycle.LiveData
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

interface HistoryRepository {

    fun insertHistory(history: History): History

    fun findHistoryById(id: Long): History?

    fun getLiveHistoryById(id: Long): LiveData<History?>

    fun getLiveHistoriesForAccountBetweenDates(accountId: Long, start: LocalDate, end: LocalDate): LiveData<List<History>>

    fun getLiveHistoriesForGroupBetweenDates(groupId: Long, start: LocalDate, end: LocalDate): LiveData<List<History>>

    fun updateHistory(history: History): Boolean

    fun deleteHistory(id: Long, reset: Boolean = true)

    fun deleteMultipleHistories(ids: List<Long>, reset: Boolean = true)

}