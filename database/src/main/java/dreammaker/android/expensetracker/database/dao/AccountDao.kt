package dreammaker.android.expensetracker.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dreammaker.android.expensetracker.database.model.AccountEntity

@Dao
interface AccountDao {

    @Insert
    fun insertAccount(account: AccountEntity): Long

    @Insert
    @Transaction
    fun insertAccounts(accounts: List<AccountEntity>)

    @Query("SELECT * FROM `accounts`")
    fun getLiveAllAccounts(): LiveData<List<AccountEntity>>

    @Query("SELECT * FROM `accounts`")
    fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    fun findAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    fun getLiveAccountById(id: Long): LiveData<AccountEntity?>

    @Query("SELECT SUM(`balance`) AS `total_balance` FROM `accounts`")
    fun getLiveTotalBalance(): LiveData<Double?>

    @Query("SELECT * FROM `accounts` WHERE `lastUsed` IS NOT NULL ORDER BY `lastUsed` DESC LIMIT :count")
    fun getLiveRecentlyUsedAccounts(count: Int = 3): LiveData<List<AccountEntity>>

    @Update
    fun updateAccount(account: AccountEntity): Int

    @Query("DELETE FROM `accounts` WHERE `id` = :id")
    fun deleteAccount(id: Long): Int

    @Query("DELETE FROM `accounts` WHERE `id` IN (:ids)")
    fun deleteMultipleAccounts(ids: List<Long>): Int
}