package dreammaker.android.expensetracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.model.AccountListModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM `accounts`")
    suspend fun getAllAccounts(): List<AccountEntity>

    //----------------------------------------------------
    //      Kotlin Coroutine and Flow based DAO methods
    //----------------------------------------------------

    @Insert
    suspend fun insert(entity: AccountEntity): Long

    @Insert
    @Transaction
    suspend fun insertMultiple(entities: List<AccountEntity>): List<Long>

    @Query("SELECT `id`,`name`,`balance` FROM `accounts` ORDER BY `name` ASC")
    fun getAccountsFlow(): Flow<List<AccountListModel>>

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    fun findAccountByIdFlow(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    fun findByIdFlow(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM `accounts` WHERE `lastUsed` IS NOT NULL ORDER BY `lastUsed` DESC LIMIT :count")
    fun getLastUsedAccountsFlow(count: Int = 3): Flow<List<AccountEntity>>

    @Update
    suspend fun update(entity: AccountEntity)

    @Query("DELETE FROM `accounts` WHERE `id` = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM `accounts` WHERE `id` IN (:ids)")
    suspend fun deleteMultipleByIds(ids: List<Long>): Int
}
