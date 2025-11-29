package rahulstech.android.expensetracker.domain

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dreammaker.android.expensetracker.database.dao.HistoryDao
import dreammaker.android.expensetracker.database.dao.HistoryQueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.HistoryTotalCreditTotalDebit
import rahulstech.android.expensetracker.domain.model.toHistory
import rahulstech.android.expensetracker.domain.model.toHistoryTotalCreditTotalDebit
import rahulstech.android.expensetracker.domain.model.toHistoryTypesList
import java.time.LocalDate
import java.util.Objects

class HistoryFilterParameters {

    var accountIds: List<Long> = emptyList()
    var groupIds: List<Long> = emptyList()
    var dateStart: LocalDate = LocalDate.now()
    var dateEnd: LocalDate = LocalDate.now()
    var types: List<History.Type> = emptyList()
    var sortOldestFirst: Boolean = true

    internal fun getHistoryQueryBuilder(): HistoryQueryBuilder = HistoryQueryBuilder().apply {
            accounts = accountIds
            groups = groupIds
            dateRange = dateStart to dateEnd
            historyTypes = types.toHistoryTypesList()
            oldestFirst = sortOldestFirst
        }

    internal fun getPagedHistories(dao: HistoryDao): Flow<PagingData<History>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30
            ),
            pagingSourceFactory = {
                // NOTE: must create new PagingSource everytime, can not return a cached source from this factory
                //      otherwise it will throw error same PagingSource reused
                val builder = getHistoryQueryBuilder()
                dao.getPagedHistories(builder.build())
            }
        ).flow.flowOn(Dispatchers.IO).map { pagingData ->
            pagingData.map { it.toHistory() }
        }
    }

    internal fun getTotalCreditDebit(dao: HistoryDao): Flow<HistoryTotalCreditTotalDebit> {
        val builder = getHistoryQueryBuilder()
        return dao.getHistoryTotalCreditDebit(builder.buildTotalCreditDebit())
            .map { it?.toHistoryTotalCreditTotalDebit() ?: HistoryTotalCreditTotalDebit() }
    }

    override fun equals(other: Any?): Boolean {
        // NOTE: == is equivalent to calling .equals, so if i used == then infinite recursion may occur
        // === is referential equality means if the object points to same memory then true otherwise false
        if (this === other) return true
        if (other is HistoryFilterParameters) {
            return accountIds == other.accountIds && groupIds == other.groupIds
                    && dateStart == other.dateStart && dateEnd == other.dateEnd
                    && types == other.types && sortOldestFirst == other.sortOldestFirst
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(accountIds,groupIds,dateStart,dateEnd,types,sortOldestFirst)
    }

    override fun toString(): String {
        return "HistoryFilterParameters(accountIds=$accountIds, groupIds=$groupIds, " +
                "dateStart=$dateStart, dateEnd=$dateEnd, types=$types, " +
                "sortOldestFirst=$sortOldestFirst)"
    }
}

interface HistoryRepository {

    fun insertHistory(history: History): History

    fun findHistoryById(id: Long): History?

    fun getLiveHistoryById(id: Long): LiveData<History?>

    fun getPagedHistories(params: HistoryFilterParameters): Flow<PagingData<History>>

    fun getTotalCreditDebit(params: HistoryFilterParameters): Flow<HistoryTotalCreditTotalDebit>

    fun updateHistory(history: History): Boolean

    fun deleteHistory(id: Long, reset: Boolean = true)

    fun deleteMultipleHistories(ids: List<Long>, reset: Boolean = true)
}