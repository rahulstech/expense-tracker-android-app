package rahulstech.android.expensetracker.domain

import dreammaker.android.expensetracker.database.dao.HistoryQueryBuilder
import rahulstech.android.expensetracker.domain.model.HistoryType
import java.time.LocalDate
import java.util.Objects

class HistoryFilterParameters {

    var accountIds: List<Long> = emptyList()
    var groupIds: List<Long> = emptyList()
    var dateStart: LocalDate = LocalDate.now()
    var dateEnd: LocalDate = LocalDate.now()
    var types: List<HistoryType> = emptyList()
    var sortOldestFirst: Boolean = true

    internal fun getHistoryQueryBuilder(): HistoryQueryBuilder = HistoryQueryBuilder().apply {
            accounts = accountIds
            groups = groupIds
            dateRange = dateStart to dateEnd
            historyTypes = types.map { it.name }
            oldestFirst = sortOldestFirst
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is HistoryFilterParameters) {
            return accountIds == other.accountIds
                    && groupIds == other.groupIds
                    && dateStart == other.dateStart
                    && dateEnd == other.dateEnd
                    && types == other.types
                    && sortOldestFirst == other.sortOldestFirst
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