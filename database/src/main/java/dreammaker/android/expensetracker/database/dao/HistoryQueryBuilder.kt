package dreammaker.android.expensetracker.database.dao

import android.util.Log
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import dreammaker.android.expensetracker.database.Converter
import java.time.LocalDate

class HistoryQueryBuilder {

    private val TAG = HistoryQueryBuilder::class.simpleName

    var historyTypes: List<String> = emptyList()
    var accounts: List<Long> = emptyList()
    var groups: List<Long> = emptyList()
    var dateRange: Pair<LocalDate, LocalDate>? = null
    var oldestFirst = true

    fun build(): SupportSQLiteQuery {

        val table = "`histories` LEFT JOIN `accounts` ON `histories`.`id` = `accounts`.`id` " +
                "LEFT JOIN `groups` ON `histories`.`id` = `groups`.`id`"

        val queryBuilder = SupportSQLiteQueryBuilder.Companion.builder(table)

        val columns = arrayOf(
            "`histories`.*",
            "`accounts`.`id`","`accounts`.`name`",
            "`groups`.`id`","`groups`.`name`"
        )
        queryBuilder.columns(columns)

        where(queryBuilder)
        orderBy(queryBuilder)

        val query = queryBuilder.create()
        Log.d(TAG,"build: query = ${query.sql}")
        return query
    }

    @Deprecated(message = "use buildTypeTotals")
    fun buildTotalCreditDebit(): SupportSQLiteQuery = buildTypeTotals()

    fun buildTypeTotals(): SupportSQLiteQuery {
        val queryBuilder = SupportSQLiteQueryBuilder.Companion.builder("`histories`")
        val columns = arrayOf("type","SUM(amount) AS total")
        queryBuilder.columns(columns)
        queryBuilder.groupBy("type")
        where(queryBuilder)
        val query = queryBuilder.create()

        Log.d(TAG,"buildTypeTotals: query = ${query.sql}")
        return query
    }

    private fun where(builder: SupportSQLiteQueryBuilder) {
        val selection = StringBuilder("1 = 1")
        val selectionArgs = mutableListOf<Any?>()

        if (historyTypes.isNotEmpty()) {
            val sqlArgs = IN("type",historyTypes)
            selection.append(" AND ").append(sqlArgs.first)
            selectionArgs.addAll(sqlArgs.second)
        }
        if (accounts.isNotEmpty()) {
            val first = IN("primaryAccountId",accounts)
            val second = IN("secondaryAccountId", accounts)
            val sqlArgs = OR(first,second)
            selection.append(" AND ").append(sqlArgs.first)
            selectionArgs.addAll(sqlArgs.second)
        }
        if (groups.isNotEmpty()) {
            val sqlArgs = IN("groupId", groups)
            selection.append(" AND ").append(sqlArgs.first)
            selectionArgs.addAll(sqlArgs.second)
        }
        dateRange?.let {
            val sqlArgs = BETWEEN("date",
                Converter.localDateToString(it.first),
                Converter.localDateToString(it.second))
            selection.append(" AND ").append(sqlArgs.first)
            selectionArgs.addAll(sqlArgs.second)
        }

        builder.selection(selection.toString(), selectionArgs.toTypedArray())
    }

    private fun orderBy(builder: SupportSQLiteQueryBuilder) {
        if (oldestFirst) {
            builder.orderBy("`date` ASC")
        }
        else {
            builder.orderBy("`date` DESC")
        }
    }

    private fun IN(column: String, values: List<Any?>): SqlArgs {
        val valuesSet = values.toSet()
        val sql = buildString {
            append("`$column` IN(")
            append(placeholders(valuesSet.size))
            append(")")
        }
        return sql to valuesSet.toList()
    }

    private fun OR(first: SqlArgs, second: SqlArgs): SqlArgs {
        val sql = buildString {
            append("(")
            append(first.first)
            append(" OR ")
            append(second.first)
            append(")")
        }
        val args = mutableListOf<Any?>()
        args.addAll(first.second.toSet())
        args.addAll(second.second.toSet())
        return sql to args
    }

    private fun BETWEEN(column: String, start: String?, end: String?): SqlArgs {
        val sql = "`$column` BETWEEN ? AND ?"
        val args = listOf(start,end)
        return sql to args
    }

    private fun placeholders(size: Int): String = (1..size).joinToString(separator = ",") { "?" }
}