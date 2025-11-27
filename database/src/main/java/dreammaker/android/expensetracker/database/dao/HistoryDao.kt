package dreammaker.android.expensetracker.database.dao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import dreammaker.android.expensetracker.database.Converters
import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.HistoryType
import dreammaker.android.expensetracker.database.model.toNamesList
import java.time.LocalDate

internal typealias SqlArgs = Pair<String,List<Any?>>

class HistoryQueryBuilder {

    var historyTypes: List<HistoryType> = emptyList()
    var accounts: List<Long> = emptyList()
    var groups: List<Long> = emptyList()
    var dateRange: Pair<LocalDate, LocalDate>? = null
    var oldestFirst = true

    fun build(): SupportSQLiteQuery {

        val table = "`histories` LEFT JOIN `accounts` ON `histories`.`id` = `accounts`.`id` " +
                "LEFT JOIN `groups` ON `histories`.`id` = `groups`.`id`"

        val queryBuilder = SupportSQLiteQueryBuilder.builder(table)

        val columns = arrayOf(
            "`histories`.*",
            "`accounts`.`id`","`accounts`.`name`",
            "`groups`.`id`","`groups`.`name`"
        )
        queryBuilder.columns(columns)

        where(queryBuilder)
        orderBy(queryBuilder)

        val query = queryBuilder.create()
        Log.d("HistoryQueryBuilder","query = ${query.sql}")
        return query
    }

//    fun buildSummary(): SupportSQLiteQuery {
//        val queryBuilder = SupportSQLiteQueryBuilder.builder("`histories`")
//
//        val columns = arrayOf(
//            "SUM(CASE `type` WHEN 'CREDIT' THEN `amount` ELSE 0 END) AS `totalCredit`",
//            "SUM(CASE `type` WHEN 'DEBIT' THEN `amount` ELSE 0 END) AS `totalDebit`",
//        )
//        queryBuilder.columns(columns)
//
//        where(queryBuilder)
//        queryBuilder.groupBy("")
//
//        return queryBuilder.create()
//    }

    private fun where(builder: SupportSQLiteQueryBuilder) {
        val selection = StringBuilder("1 = 1")
        val selectionArgs = mutableListOf<Any?>()

        if (historyTypes.isNotEmpty()) {
            // NOTE: sqlite can not bind Enum, bind the enum name
            val sqlArgs = IN("type",historyTypes.toNamesList())
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
            // NOTE: sqlite can not bind LocalDate, convert it to string
            val sqlArgs = BETWEEN("date",
                Converters.localDateToString(it.first),
                Converters.localDateToString(it.second))
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

    private fun BETWEEN(column: String, start: Any, end: Any): SqlArgs {
        val sql = "`$column` BETWEEN ? AND ?"
        val args = listOf(start,end)
        return sql to args
    }

    private fun placeholders(size: Int): String = (1..size).joinToString(separator = ",") { "?" }
}

@Dao
interface HistoryDao {

    @Insert
    fun insertHistory(history: HistoryEntity): Long

    @Insert
    @Transaction
    fun insertHistories(histories: List<HistoryEntity>)

    @Query("SELECT * FROM `histories` WHERE `id` = :id")
    @Transaction
    fun findHistoryById(id: Long): HistoryDetails?

    @Query("SELECT * FROM `histories` WHERE `id` = :id")
    @Transaction
    fun getLiveHistoryById(id: Long): LiveData<HistoryDetails?>

    @Query("SELECT * FROM `histories` LIMIT :size OFFSET :from")
    fun getHistories(from: Long, size: Long): List<HistoryEntity>

    @RawQuery(observedEntities = [HistoryEntity::class, AccountEntity::class, GroupEntity::class])
    @Transaction
    fun getPagedHistories(query: SupportSQLiteQuery): PagingSource<Int, HistoryDetails>

    @Update
    fun updateHistory(history: HistoryEntity): Int

    @Query("DELETE FROM `histories` WHERE id = :id")
    fun deleteHistory(id: Long): Int

    @Query("DELETE FROM `histories` WHERE id IN(:ids)")
    fun deleteMultipleHistories(ids: List<Long>): Int
}

