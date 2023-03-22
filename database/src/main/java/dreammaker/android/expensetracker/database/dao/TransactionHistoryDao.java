package dreammaker.android.expensetracker.database.dao;

import java.math.BigDecimal;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import dreammaker.android.expensetracker.database.DatabaseException;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.model.TransactionHistory;
import dreammaker.android.expensetracker.database.model.TransactionHistoryDisplayModel;
import dreammaker.android.expensetracker.database.type.TransactionType;

@Dao
public abstract class TransactionHistoryDao {

    private ExpensesDatabase db;

    private AccountDao accountDao;

    private PeopleDao peopleDao;

    public TransactionHistoryDao(@NonNull ExpensesDatabase db) {
        this.db = db;
        this.accountDao = db.getAccountDao();
        this.peopleDao = db.getPeopleDao();
    }

    public long insert(TransactionHistory t) {
        return db.runInTransaction(()->{
            TransactionType type = t.getType();
            Long payeePersonId = t.getPayeePersonId();
            Long payerPersonId = t.getPayerPersonId();
            Long payeeAccountId = t.getPayeeAccountId();
            Long payerAccountId = t.getPayerAccountId();
            BigDecimal amount = t.getAmount();

            long tid = _insert(t);
            if (tid <= 0) return 0L;
            boolean updated = false;
            switch (type) {
                case MONEY_TRANSFER: {
                    updated = accountDao.reduceBalance(payerAccountId,amount)
                            && accountDao.addBalance(payeeAccountId,amount);
                }
                break;
                case INCOME: {
                    updated = accountDao.addBalance(payeeAccountId,amount);
                }
                break;
                case EXPENSE: {
                    updated = accountDao.reduceBalance(payerAccountId,amount);
                }
                break;
                case DUE: {
                    updated = accountDao.reduceBalance(payerAccountId,amount)
                            && peopleDao.addDue(payeePersonId,amount);
                }
                break;
                case PAY_DUE: {
                    updated = accountDao.addBalance(payeeAccountId,amount)
                            && peopleDao.reduceDue(payerPersonId,amount);
                }
                break;
                case BORROW: {
                    updated = peopleDao.addBorrow(payerPersonId,amount)
                            && accountDao.addBalance(payeeAccountId,amount);
                }
                break;
                case PAY_BORROW: {
                    updated = accountDao.reduceBalance(payerAccountId,amount)
                            && peopleDao.reduceBorrow(payeePersonId,amount);
                }
            }
            if (!updated) {
                throw new DatabaseException("fail to update other entities while inserting new transaction history");
            }
            return tid;
        });
    }

    public boolean delete(TransactionHistory t) {
        return db.runInTransaction(()->{
            TransactionType type = t.getType();
            Long payeePersonId = t.getPayeePersonId();
            Long payerPersonId = t.getPayerPersonId();
            Long payeeAccountId = t.getPayeeAccountId();
            Long payerAccountId = t.getPayerAccountId();
            BigDecimal amount = t.getAmount();

            int changes = _delete(t);
            if (changes != 1) return true;
            boolean updated = false;
            switch (type) {
                case MONEY_TRANSFER: {
                    updated = accountDao.reduceBalance(payeeAccountId,amount) &&
                            accountDao.addBalance(payerAccountId,amount);
                }
                break;
                case INCOME: {
                    updated = accountDao.reduceBalance(payeeAccountId,amount);
                }
                break;
                case EXPENSE: {
                    updated = accountDao.addBalance(payerAccountId,amount);
                }
                break;
                case DUE: {
                    updated = accountDao.addBalance(payerAccountId,amount)
                            && peopleDao.reduceDue(payeePersonId,amount);
                }
                break;
                case PAY_DUE: {
                    updated = accountDao.reduceBalance(payeeAccountId,amount)
                            && peopleDao.addDue(payerPersonId,amount);
                }
                break;
                case BORROW: {
                    updated = peopleDao.reduceBorrow(payerPersonId,amount)
                            && accountDao.reduceBalance(payeeAccountId,amount);
                }
                break;
                case PAY_BORROW: {
                    updated = accountDao.addBalance(payerAccountId,amount)
                            && peopleDao.addBorrow(payeePersonId,amount);
                }
            }
            if (!updated) {
                throw new DatabaseException("fail to update other entities while deleting transaction history");
            }
            return true;
        });
    }

    @Query("DELETE FROM `transaction_histories` WHERE `id` IN(:ids)")
    public abstract int deleteMultiple(List<Long> ids);

    @Transaction
    @Query("SELECT * FROM `transaction_histories` WHERE `id` = :id")
    public abstract LiveData<TransactionHistoryDisplayModel> findTransactionHistoryDisplayById(long id);

    @Query("SELECT * FROM `transaction_histories` WHERE `id` = :id")
    public abstract TransactionHistory getTransactionHistoryById(long id);

    // TODO: will be removed to updated

    @Transaction
    @Query("SELECT * FROM `transaction_histories` WHERE `payeeAccountId` = :accountId OR `payerAccountId` = :accountId ORDER BY `date` DESC")
    public abstract DataSource.Factory<Integer,TransactionHistoryDisplayModel> getTransactionsForAccount(long accountId);

    @Transaction
    @Query("SELECT * FROM `transaction_histories` WHERE `payeePersonId` = :personId OR `payerPersonId` = :personId ORDER BY `date` DESC")
    public abstract DataSource.Factory<Integer,TransactionHistoryDisplayModel> getTransactionsForPerson(long personId);

    @Insert
    protected abstract long _insert(TransactionHistory t);

    @Delete
    protected abstract int _delete(TransactionHistory t);
}
