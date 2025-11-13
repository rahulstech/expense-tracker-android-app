package dreammaker.android.expensetracker.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dreammaker.android.expensetracker.database.History
import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HistoryDao {

    @Insert
    fun insertHistory(history: HistoryEntity): Long

    @Insert
    fun insertHistories(histories: List<History>)

    @Query("SELECT * FROM `histories` " +
            "WHERE :start <= `date` AND `date` <= :end AND (`primaryAccountId` = :accountId or `secondaryAccountId` = :accountId) " +
            "ORDER BY `date` ASC")
    @Transaction
    fun getHistoriesBetweenDatesOnlyForAccount(start: LocalDate, end: LocalDate, accountId: Long): LiveData<List<HistoryDetails>>

    @Query("SELECT * FROM `histories` " +
            "WHERE :start <= `date` AND `date` <= :end AND `groupId` = :groupId " +
            "ORDER BY `date` ASC")
    @Transaction
    fun getHistoriesBetweenDatesOnlyForGroup(start: LocalDate, end: LocalDate, groupId: Long): LiveData<List<HistoryDetails>>

    @Update
    fun updateHistory(history: HistoryEntity): Int

//    fun insertHistory(history: History): Long {
//        if (history.type == HistoryType.TRANSFER) {
//            val moneyTransfer = history.toMoneyTransfer()
//            return expensesDao.insertMoneyTransfer(moneyTransfer)
//        }
//        else {
//            val transaction = history.toTransaction()
//            return expensesDao.insertTransaction(transaction)
//        }
//    }

//    fun updateHistory(history: History) {
//        if (history.type == HistoryType.TRANSFER) {
//            val moneyTransfer = history.toMoneyTransfer()
//            expensesDao.updateMoneyTransfer(moneyTransfer)
//        }
//        else {
//            val transfer = history.toTransaction()
//            expensesDao.updateTransaction(transfer)
//        }
//    }

    @Query("SELECT * FROM `histories` WHERE `id` = :id")
    @Transaction
    fun findHistoryDetailsById(id: Long): HistoryDetails?

    @Query("SELECT * FROM `histories` WHERE `id` = :id")
    @Transaction
    fun findHistoryDetailsByIdAsFlow(id: Long): Flow<HistoryDetails?>

    @Query("SELECT * FROM `histories` WHERE `id` = :id")
    fun findHistoryById(id: Long): HistoryEntity

    @Query("SELECT * FROM `histories` LIMIT :size OFFSET :from")
    abstract fun getHistories(from: Long, size: Long): List<HistoryEntity>

//    @Delete
//    fun deleteHistory(history: History) {
//        val type = history.type
//        if (type == HistoryType.TRANSFER) {
//            val moneyTransfer = history.toMoneyTransfer()
//            expensesDao.deleteMoneyTransfer(moneyTransfer)
//        }
//        else {
//            val transaction = history.toTransaction()
//            expensesDao.deleteTransactions(transaction)
//        }
//    }

    @Delete
    fun deleteHistory(history: HistoryEntity)

    @Query("DELETE FROM `histories` WHERE `id` IN(:ids)")
    fun deleteMultipleHistories(ids: List<Long>)

//    @Transaction
//    open fun insertHistories(histories: List<History>) {
//        val transactions = histories.filter { history -> history.type != HistoryType.TRANSFER }.map { history -> history.toTransaction() }
//        val moneyTransfers = histories.filter { history -> history.type == HistoryType.TRANSFER }.map { history -> history.toMoneyTransfer() }
//
//        expenseBackupDao.insertTransactions(transactions)
//        expenseBackupDao.insertMoneyTransfers(moneyTransfers)
//    }


}

