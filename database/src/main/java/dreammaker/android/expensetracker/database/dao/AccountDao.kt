package dreammaker.android.expensetracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.model.AccountListModel
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface AccountDao {

    //----------------------------------------------------
    //      Kotlin Coroutine and Flow based DAO methods
    //----------------------------------------------------

    @Query("SELECT * FROM `accounts`")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Insert
    suspend fun insert(entity: AccountEntity): Long

    @Insert
    @Transaction
    suspend fun insertMultiple(entities: List<AccountEntity>): List<Long>

    @Query("SELECT `id`,`name`,`balance` FROM `accounts` ORDER BY `name` ASC")
    fun getAccountsFlow(): Flow<List<AccountListModel>>

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    fun findByIdFlow(id: Long): Flow<AccountEntity?>

    @Query("SELECT `id`,`name`,`balance` FROM `accounts` WHERE `lastUsed` IS NOT NULL ORDER BY `lastUsed` DESC LIMIT :count")
    fun getLastUsedAccountsFlow(count: Int = 3): Flow<List<AccountListModel>>

    @Query("SELECT `id`, `name`, `balance` FROM `accounts` ORDER BY `totalUsed` DESC LIMIT :count")
    fun getFrequentlyUserAccountsFlow(count: Int = 3): Flow<List<AccountListModel>>

    @Query("SELECT COALESCE(SUM(`balance`), 0) AS `total_balance` FROM `accounts`")
    fun getTotalBalanceOfAllAccounts(): Flow<BigDecimal>

    @Update
    suspend fun update(entity: AccountEntity)

    @Query("DELETE FROM `accounts` WHERE `id` = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM `accounts` WHERE `id` IN (:ids)")
    suspend fun deleteMultipleByIds(ids: List<Long>): Int
}
