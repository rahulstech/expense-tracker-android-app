package dreammaker.android.expensetracker.database.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.model.AccountModel;

@Dao
@SuppressWarnings("unused")
public interface AccountDao {

    @Insert
    long addAccount(Account account);

    @Update
    int updateAccount(Account account);

    @Query("SELECT `id`,`name`,`balance` FROM `accounts`")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    LiveData<List<AccountModel>> getAllAccountsLive();

    @Query("SELECT `id`,`name`,`balance`, " +
            "(SELECT COUNT(`id`) FROM `transaction_histories` " +
            "WHERE `payeeAccountId` = `accounts`.`id` OR `payerAccountId` = `accounts`.`id`) AS `usageCount` " +
            "FROM `accounts`")
    LiveData<List<AccountModel>> getAllAccountWithUsageCountLive();

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    Account findAccountById(long id);

    @Query("DELETE FROM `accounts` WHERE `id` IN(:ids)")
    int removeAccounts(long[] ids);
}
