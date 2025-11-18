package dreammaker.android.expensetracker.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import java.time.LocalDate

@Dao
interface HistoryDao {

    @Insert
    fun insertHistory(history: HistoryEntity): Long

    @Insert
    @Transaction
    fun insertHistories(histories: List<HistoryEntity>)

    @Query("SELECT * FROM `histories` " +
            "WHERE :start <= `date` AND :end >= `date` AND (`primaryAccountId` = :accountId OR `secondaryAccountId` = :accountId) " +
            "ORDER BY `date` ASC")
    @Transaction
    fun getLiveHistoriesForAccountBetweenDates( accountId: Long, start: LocalDate, end: LocalDate): LiveData<List<HistoryDetails>>

    @Query("SELECT * FROM `histories` " +
            "WHERE :start <= `date` AND :end >= `date` AND `groupId` = :groupId " +
            "ORDER BY `date` ASC")
    @Transaction
    fun getLiveHistoriesForGroupBetweenDates(groupId: Long, start: LocalDate, end: LocalDate): LiveData<List<HistoryDetails>>

    @Query("SELECT * FROM `histories` WHERE `id` = :id")
    fun findHistoryById(id: Long): HistoryEntity?

    @Query("SELECT * FROM `histories` WHERE `id` = :id")
    @Transaction
    fun getLiveHistoryById(id: Long): LiveData<HistoryDetails?>

    @Query("SELECT * FROM `histories` LIMIT :size OFFSET :from")
    fun getHistories(from: Long, size: Long): List<HistoryEntity>

    @Update
    fun updateHistory(history: HistoryEntity): Int

    @Query("DELETE FROM `histories` WHERE id = :id")
    fun deleteHistory(id: Long): Int

    @Query("DELETE FROM `histories` WHERE id IN(:ids)")
    fun deleteMultipleHistories(ids: List<Long>): Int
}

