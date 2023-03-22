package dreammaker.android.expensetracker.database.dao;

import java.math.BigDecimal;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import dreammaker.android.expensetracker.database.model.Account;
import dreammaker.android.expensetracker.database.model.AccountDisplayModel;

@Dao
public abstract class AccountDao {

    @Insert
    public abstract long insert(Account account);

    @Update
    public abstract int update(Account account);

    public boolean addBalance(long id, BigDecimal quantity) {
        Account account = getAccountById(id);
        if (null == account) return false;
        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.add(quantity);
        account.setBalance(newBalance);
        return 0 < update(account);
    }

    public boolean reduceBalance(long id, BigDecimal quantity) {
        return addBalance(id,quantity.negate());
    }

    @Delete
    public abstract int delete(Account account);

    @Transaction
    @Query("DELETE FROM `accounts` WHERE `id` IN(:ids)")
    public abstract int deleteMultiple(List<Long> ids);

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    public abstract LiveData<Account> findAccountById(long id);

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    public abstract Account getAccountById(long id);

    @Query("SELECT `id`,`name`,`balance` FROM `accounts`")
    public abstract LiveData<List<AccountDisplayModel>> getAllAccountsForDisplay();
}
