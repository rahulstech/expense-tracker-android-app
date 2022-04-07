package dreammaker.android.expensetracker.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import dreammaker.android.expensetracker.util.Date;

@Dao
public abstract class MoneyTransferDao {

    private final AccountDao accountDao;

    public MoneyTransferDao(ExpensesDatabase db) {
        this.accountDao = db.getAccountDao();
    }

    public long insertMoneyTransfer(MoneyTransfer mt) {
        long id = insert_money_transfer_internal(mt);
        if (id > 0) {
            accountDao.addAccountBalance(mt.getPayee_account_id(), mt.getAmount());
            accountDao.addAccountBalance(mt.getPayer_account_id(),-mt.getAmount());
        }
        return id;
    }

    @Transaction
    @Query("SELECT * FROM `money_transfers` ORDER BY `when` DESC")
    public abstract LiveData<List<MoneyTransferDetails>> getMoneyTransferHistory();

    public int updateMoneyTransfer(MoneyTransfer newMT) {
        MoneyTransfer oldMT = getMoneyTransferById(newMT.getId());
        int changes = update_money_transfer_internal(newMT);
        if (changes > 0) {
            accountDao.addAccountBalance(oldMT.getPayee_account_id(),-oldMT.getAmount());
            accountDao.addAccountBalance(oldMT.getPayer_account_id(),oldMT.getAmount());
            accountDao.addAccountBalance(newMT.getPayee_account_id(), newMT.getAmount());
            accountDao.addAccountBalance(newMT.getPayer_account_id(),-newMT.getAmount());
        }
        return changes;
    }

    public int deleteMoneyTransfer(MoneyTransfer mt) {
        MoneyTransfer oldMT = getMoneyTransferById(mt.getId());
        int changes = delete_money_transfer_internal(mt);
        if (changes > 0) {
            accountDao.addAccountBalance(oldMT.getPayee_account_id(), -oldMT.getAmount());
            accountDao.addAccountBalance(oldMT.getPayer_account_id(), oldMT.getAmount());
        }
        return changes;
    }

    @Query("DELETE FROM `money_transfers` WHERE `when` < :date;")
    public abstract void deleteMoneyTransfersOlderThan(Date date);

    @Insert
    abstract long insert_money_transfer_internal(MoneyTransfer mt);

    @Query("SELECT * FROM `money_transfers` WHERE `id` = :id;")
    abstract MoneyTransfer getMoneyTransferById(long id);

    @Update
    abstract int update_money_transfer_internal(MoneyTransfer mt);

    @Delete
    abstract int delete_money_transfer_internal(MoneyTransfer mt);
}
