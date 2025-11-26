package rahulstech.android.expensetracker.domain

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dreammaker.android.expensetracker.database.dao.HistoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.toHistory
import java.time.LocalDate

sealed class HistoryFilterParameters {

    protected open val accountId: Long = 0L
    protected open val groupId: Long = 0L
    private var dateStart: LocalDate = LocalDate.now()
    private var dateEnd: LocalDate = LocalDate.now()

    fun setDateRange(start: LocalDate, end: LocalDate) {
        dateStart = start
        dateEnd = end
    }

    class AccountHistories(override val accountId: Long): HistoryFilterParameters()

    class GroupHistories(override val groupId: Long): HistoryFilterParameters()

    internal fun getPagedHistories(dao: HistoryDao): Flow<PagingData<History>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30
            ),
            pagingSourceFactory = {
                // NOTE: must create new PagingSource everytime, can not return a cached source from this factory
                //      otherwise it will throw error same PagingSource reused
                when(this) {
                    is AccountHistories -> {
                        dao.getPagedHistoriesOfAccountBetweenDates(accountId,dateStart,dateEnd)
                    }
                    is GroupHistories -> {
                        dao.getPagedHistoriesOfGroupBetweenDates(groupId,dateStart,dateEnd)
                    }
                }
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toHistory() }
        }
    }

    override fun toString(): String {
        return "HistoryFilterParameters(accountId=$accountId, groupId=$groupId, dateStart=$dateStart, dateEnd=$dateEnd)"
    }
}

interface HistoryRepository {

    fun insertHistory(history: History): History

    fun findHistoryById(id: Long): History?

    fun getLiveHistoryById(id: Long): LiveData<History?>

    fun getPagedHistories(params: HistoryFilterParameters): Flow<PagingData<History>>

    fun updateHistory(history: History): Boolean

    fun deleteHistory(id: Long, reset: Boolean = true)

    fun deleteMultipleHistories(ids: List<Long>, reset: Boolean = true)
}