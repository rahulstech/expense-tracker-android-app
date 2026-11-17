package dreammaker.android.expensetracker.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.TotalCreditDebit
import dreammaker.android.expensetracker.database.model.TypeTotal
import kotlinx.coroutines.flow.Flow

internal typealias SqlArgs = Pair<String,List<Any?>>

@Dao
interface HistoryDao {

    @RawQuery(observedEntities = [HistoryEntity::class, AccountEntity::class, GroupEntity::class])
    @Transaction
    fun getPagedHistories(query: SupportSQLiteQuery): PagingSource<Int, HistoryDetails>

    @Deprecated(message = "use getTypeTotals")
    @RawQuery(observedEntities = [HistoryEntity::class])
    fun getHistoryTotalCreditDebit(query: SupportSQLiteQuery): Flow<TotalCreditDebit>

    @RawQuery(observedEntities = [HistoryEntity::class])
    fun getHistoryTypeTotals(query: SupportSQLiteQuery): Flow<List<TypeTotal>>

    @Insert
    suspend fun insert(history: HistoryEntity): Long

    @Insert
    suspend fun insertMultiple(histories: List<HistoryEntity>)

    @Query("SELECT * FROM `histories` WHERE `id` = :id")
    @Transaction
    fun findHistoryDetailsByIdFlow(id: Long): Flow<HistoryDetails?>

    @Query("SELECT * FROM `histories` LIMIT :size OFFSET :from")
    suspend fun getHistories(size: Int, from: Long = 0): List<HistoryEntity>

    @Update
    suspend fun update(history: HistoryEntity): Int

    @Query("DELETE FROM `histories` WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM `histories` WHERE id IN(:ids)")
    suspend fun deleteMultiple(ids: List<Long>): Int
}