package dreammaker.android.expensetracker.database.dao;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import dreammaker.android.expensetracker.database.DatabaseException;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.database.type.TransactionType;

@Dao
@Deprecated
public abstract class TransactionDao {

    private ExpensesDatabase db;

    private AccountDao accountDao;

    private PeopleDao peopleDao;

    protected TransactionDao(@NonNull ExpensesDatabase db) {
        this.db = db;
        //this.accountDao = db.getAccountDao();
        //this.peopleDao = db.getPeopleDao();
    }

    @NonNull
    public long[] addTransactions(@NonNull Transaction... transactions) {
        if (transactions.length == 0) return new long[0];
        return db.runInTransaction(()->{
            long[] ids = new long[transactions.length];
            for (int i = 0; i < transactions.length; i++) {
                Transaction transaction = transactions[i];
                long newId = addSingleTransaction(transaction);
                ids[i] = newId;
            }
            return ids;
        });
    }

    private long addSingleTransaction(@NonNull Transaction transaction) {
        /*Long payerId = transaction.getPayerId();
        Long  payeeId = transaction.getPayeeId();
        BigDecimal amount = transaction.getTotalAmount();
        TransactionType type = transaction.getTransactionType();

        long newId = insert(transaction);
        if (newId <= 0) throw new DatabaseException("transaction="+transaction+" not inserted");

        boolean payerUpdated = null == payerId || updatePayer(type,amount,payerId);
        boolean payeeUpdated = null == payeeId || updatePayee(type,amount,payeeId);
        if (!payerUpdated) throw new DatabaseException("payer with id="+payerId+" and type="+type+" not updated");
        if (!payeeUpdated) throw new DatabaseException("payee with id="+payeeId+" and type="+type+" not updatd");

        return newId;*/
        return 0;
    }

    public boolean updateTransactions(@NonNull Transaction... transactions) {
        if (transactions.length == 0) return true;
        return db.runInTransaction(() -> {
            for (Transaction t : transactions) {
                updateSingleTransaction(t);
            }
            return true;
        });
    }

    private void updateSingleTransaction(@NonNull Transaction transaction) {
        /*Long payerId = transaction.getPayerId();
        Long payeeId = transaction.getPayeeId();
        BigDecimal newAmount = transaction.getTotalAmount();
        TransactionType type = transaction.getTransactionType();
        long id = transaction.getTransactionId();

        Transaction old = findTransactionById(id);
        if (null == old) throw new DatabaseException("no transaction found with id="+id);

        BigDecimal oldAmount = old.getTotalAmount();

        boolean updated = 1 == update(transaction);
        if (!updated) throw new DatabaseException("transaction="+transaction+" not updated");

        BigDecimal quantity;
        if (type.shouldDeductPayerAmount()) {
            quantity = oldAmount.subtract(newAmount);
        }
        else {
            quantity = newAmount.subtract(oldAmount);
        }

        boolean payerUpdated = null == payerId || updatePayer(type,quantity,payerId);
        boolean payeeUpdated = null == payeeId || updatePayee(type,quantity,payeeId);

        if (!payerUpdated) throw new DatabaseException("payer with id="+payerId+" not updated while updated transaction");
        if (!payeeUpdated) throw new DatabaseException("payee with id="+payeeId+" not updated while updating transaction");


         */
    }

    //@Query("SELECT * FROM transactions WHERE `transactionId` = :tid")
    //public abstract Transaction findTransactionById(long tid);

    @Insert
    protected abstract long insert(Transaction transaction);

    //@Update
    //protected abstract int update(Transaction transaction);

    @Delete
    protected abstract int delete(Transaction transaction);


    private boolean updatePayer(@NonNull TransactionType type, @NonNull BigDecimal quantity, @NonNull Long payerId) {
        return false;
    }

    private boolean updatePayee(@NonNull TransactionType type, @NonNull BigDecimal amount, @Nullable Long payeeId) {

        return false;
    }
}
