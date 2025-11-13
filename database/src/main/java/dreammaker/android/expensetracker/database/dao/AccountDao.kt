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

    @Query("SELECT `id`, `name`, `balance` FROM `accounts` ORDER BY `name` ASC")
    fun getLiveAccounts(): LiveData<List<AccountEntity>>

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    fun findAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    fun getLiveAccountById(id: Long): LiveData<AccountEntity?>

    @Query("SELECT SUM(`balance`) AS `total_balance` FROM `accounts`")
    fun getTotalBalance(): LiveData<Double?>

//    @Query("SELECT `_id`, `account_name`, `balance` FROM `accounts` WHERE `_id` IN " +
//            "(SELECT `primaryAccountId` FROM `histories` WHERE `primaryAccountId` IS NOT NULL GROUP BY primaryAccountId ORDER BY Max(`date`) DESC LIMIT 3)")
//    fun getLatestUsedThreeAccounts(): LiveData<List<Account>>

    @Query("SELECT * FROM `accounts`")
    fun getAccounts(): List<AccountEntity>

    @Update
    fun updateAccount(account: AccountEntity): Int

    @Query("DELETE FROM `accounts` WHERE `id` = :id")
    fun deleteAccount(id: Long): Int

    @Query("DELETE FROM `accounts` WHERE `id` IN(:ids)")
    fun deleteMultipleAccount(ids: List<Long>): Int
}