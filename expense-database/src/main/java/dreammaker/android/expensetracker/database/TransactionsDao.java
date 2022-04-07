package dreammaker.android.expensetracker.database;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

import androidx.collection.LongSparseArray;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;
import dreammaker.android.expensetracker.util.Date;

import static dreammaker.android.expensetracker.BuildConfig.DEBUG;

@Dao
public abstract class TransactionsDao {

    private static final String TAG = "TransactionDao";

    private final AccountDao accountDao;
    private final PersonDao personDao;

    public TransactionsDao(ExpensesDatabase db) {
        this.accountDao = db.getAccountDao();
        this.personDao = db.getPersonDao();
    }

    public long insertTransaction(Transaction t) {
        long _id = insert_transaction_internal(t);
        if (_id > 0) {
            // in debit transaction balance increases and due decreases
            // in credit transaction balance decreases and due increases
            // therefore the amount is determined w.r.t. balance to add
            // and add the negated amount for person due
            float amount = t.getType() == 1 ? t.getAmount() : -t.getAmount();
            accountDao.addAccountBalance(t.getAccountId(),amount);
            personDao.addPersonDue(t.getPersonId(),-amount);
        }
        return _id;
    }

    @androidx.room.Transaction
    @RawQuery(observedEntities = {TransactionDetails.class})
    public abstract DataSource.Factory<Integer, TransactionDetails> filterTransactionPaged(SupportSQLiteQuery query);

    public int updateTransaction(Transaction newT) {
        Transaction oldT = getTransactionById(newT.getTransactionId());
        int changes = update_transaction_internal(newT);
        if (changes > 0) {
            // if old transaction was a debit then deduct the old
            // amount otherwise add the old amount from old account. same applies for due but negated
            // value for old person. if new transaction is debit then add the new amount otherwise
            // deduct the new amount for new account. same applies for due but negated value for new person.
            float oldAmount = oldT.getType() == 1 ? -oldT.getAmount() : oldT.getAmount();
            float newAmount = newT.getType() == 1 ? newT.getAmount() : newT.getAmount();

            accountDao.addAccountBalance(oldT.getAccountId(),oldAmount);
            personDao.addPersonDue(oldT.getPersonId(),-oldAmount);
            accountDao.addAccountBalance(newT.getAccountId(),newAmount);
            personDao.addPersonDue(newT.getPersonId(),-newAmount);
        }
        return changes;
    }

    public int removeTransaction(List<Transaction> transactions) {
        return remove_transactions_internal(transactions);
    }

    public int removeTransaction(Transaction t) {
        return removeTransaction(Arrays.asList(t));
    }

    @Query("DELETE FROM `transactions` WHERE `date` < :date")
    public abstract int removeTransactionsOlderThan(Date date);

    public int moveToTrash(List<Transaction> transactions) {
        return setTransactionDeleted(transactions,true);
    }

    public boolean moveToTrash(Transaction t) {
        return 1==moveToTrash(Arrays.asList(t));
    }

    public int restoreFromTrash(List<Transaction> transactions) {
        return setTransactionDeleted(transactions,false);
    }

    public boolean restoreFromTrash(Transaction t) {
        return 1==restoreFromTrash(Arrays.asList(t));
    }

    @Deprecated
    public void markTransactionDeletedOlderThan(Date date) {
        removeTransactionsOlderThan(date);
    }

    @Query("SELECT * FROM `transactions` WHERE `_id` = :transactionId;")
    public abstract Transaction getTransactionById(long transactionId);

    @Insert
    abstract long insert_transaction_internal(Transaction newTransaction);

    @Update
    abstract int update_transaction_internal(Transaction transaction);

    @androidx.room.Transaction
    @Delete
    abstract int remove_transactions_internal(List<Transaction> transactions);

    private int setTransactionDeleted(List<Transaction> transactions, boolean deleted) {
        LongSparseArray<Float> balances = new LongSparseArray<>();
        LongSparseArray<Float> dues = new LongSparseArray<>();
        int changes = 0;
        for (Transaction t : transactions) {
            final boolean oldDeleted = t.isDeleted();
            t.setDeleted(deleted);
            if (update_transaction_internal(t) > 0) {
                float amount;
                if (deleted) amount = t.getType() == 1 ? -t.getAmount() : t.getAmount();
                else amount = t.getType() == 0 ? -t.getAmount() : t.getAmount();

                balances.put(t.getAccountId(), balances.get(t.getAccountId(), 0F) + amount);
                dues.put(t.getPersonId(), dues.get(t.getPersonId(), 0F) - amount);

                changes++;
            }
            else t.setDeleted(oldDeleted);
        }
        if (changes > 0) {
            for (int i = 0; i < balances.size(); i++) {
                final long _id = balances.keyAt(i);
                final float amount = balances.valueAt(i);
                accountDao.addAccountBalance(_id,amount);
            }
            for (int i = 0; i < dues.size(); i++) {
                final long _id = dues.keyAt(i);
                final float amount = dues.valueAt(i);
                personDao.addPersonDue(_id,amount);
            }
        }
        if (DEBUG) Log.d(TAG,"setTransactionDeleted(transactions.size()="+transactions.size()+",deleted="+deleted+"): " +
                "changes="+changes+", balances.size()="+balances.size()+", dues.size()="+dues.size());
        return changes;
    }

    @Query("SELECT * FROM transactions WHERE account_id = :accId AND date BETWEEN :start AND :end AND deleted = 0  ORDER BY date DESC")
    public abstract List<Transaction> getAccountTransactionsBetween(long accId, Date start, Date end);

    @Query("SELECT * FROM transactions WHERE person_id = :personId AND date BETWEEN :start AND :end AND deleted = 0  ORDER BY date DESC")
    public abstract List<Transaction> getPersonTransactionsBetween(long personId, Date start, Date end);







}
