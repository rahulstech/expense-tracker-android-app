package dreammaker.android.expensetracker.database

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.util.Objects

@Entity(tableName = "accounts")
open class Account (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var accountId: Long,
    @ColumnInfo(name = "account_name")
    var accountName: String,
    open var balance: Float
): Cloneable {

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other is Account) {
            return other.accountId == accountId && other.accountName == accountName && other.balance == balance;
        }
        return false
    }

    override fun hashCode(): Int = Objects.hash(accountId,accountName,balance)

    @Deprecated("", ReplaceWith("equals(o)"))
    fun equalContents(o: Account?): Boolean = equals(o)

    override fun toString(): String {
        return "Account{" +
                "accountName='" + accountName + '\'' +
                ", accountId=" + accountId +
                ", balance=" + balance +
                '}'
    }

    public override fun clone(): Account = Account(accountId, accountName, balance)
}

data class AccountModel(
    @ColumnInfo(name = "_id")
    val id: Long?,
    @ColumnInfo(name = "account_name")
    val name: String?,
    val balance: Float?
) {
    fun toAccount(): Account {
        return Account(
            id ?: 0,
            name!!,
            balance!!
        )
    }
}

@Dao
interface AccountDao {

    @Insert
    fun insertAccount(account: Account): Long

    @Update
    fun updateAccount(account: Account)

    @Query("SELECT `_id`, `account_name`, `balance` FROM `accounts` ORDER BY `account_name` ASC")
    fun getAllAccounts(): LiveData<List<AccountModel>>

    @Query("SELECT * FROM `accounts` WHERE `_id` = :id")
    fun findAccountById(id: Long): LiveData<AccountModel?>

    @Query("SELECT SUM(`balance`) AS `total_balance` FROM `accounts`")
    fun getTotalBalance(): LiveData<Double?>

    @Delete
    fun deleteAccount(account: Account)

    @Query("DELETE FROM `accounts` WHERE `_id` IN(:ids)")
    fun deleteMultipleAccount(ids: List<Long>)

    @Query("SELECT `_id`, `account_name`, `balance` FROM `accounts` WHERE `_id` IN " +
            "(SELECT `primaryAccountId` FROM `histories` WHERE `primaryAccountId` IS NOT NULL GROUP BY primaryAccountId ORDER BY Max(`date`) DESC LIMIT 3)")
    fun getLatestUsedThreeAccounts(): LiveData<List<AccountModel>>

    @Insert
    @Transaction
    fun insertAccounts(accounts: List<Account>)

    @Query("SELECT * FROM `accounts`")
    fun getAccounts(): List<Account>
}
