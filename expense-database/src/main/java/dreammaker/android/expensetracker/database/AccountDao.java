package dreammaker.android.expensetracker.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public abstract class AccountDao {

    @Insert
    public abstract long insertAccount(Account account);

    @Transaction
    @Insert
    public abstract void insertAccounts(List<Account> accounts);

    @Query("SELECT * FROM `accounts`")
    public abstract LiveData<List<Account>> getAllAccounts();

    @Query("SELECT COUNT(`_id`) FROM `accounts`")
    public abstract long countAccounts();

    @Update
    public abstract int updateAccount(Account account);

    @Query("UPDATE `accounts` SET `balance` =  `balance` + :amount WHERE `_id` = :accountId")
    public abstract int addAccountBalance(long accountId, float amount);

    @Delete
    public abstract int deleteAccounts(Account... accounts);

    @Query("DELETE FROM `accounts`")
    public abstract void clearAccounts();
}
