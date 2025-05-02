package dreammaker.android.expensetracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import static dreammaker.android.expensetracker.database.ExpensesContract.AboutAccountColumns.BALANCE;
import static dreammaker.android.expensetracker.database.ExpensesContract.AboutPersonColumns.DUE_PAYMENT;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.ACCOUNTS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.PERSONS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.TRANSACTIONS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE_CREDIT;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE_DEBIT;

@Dao
public abstract class ExpensesDao {

    ////////////////////////////////////////////////////////////////////////////////////
    ///                                 Account                                     ///
    //////////////////////////////////////////////////////////////////////////////////

    @Query("UPDATE "+ACCOUNTS_TABLE+" SET "+
            BALANCE+" = CASE :type WHEN "+TYPE_CREDIT+" THEN "+BALANCE+" - :amount ELSE "+BALANCE+" + :amount END " +
            "WHERE "+ ExpensesContract.AccountsColumns._ID+" = :accountId")
    abstract void setAccountBalance(long accountId, int type, float amount);

    @Query("DELETE FROM `accounts`;")
     abstract void clearAccounts();

    ////////////////////////////////////////////////////////////////////////////////////
    ///                                 Person                                      ///
    //////////////////////////////////////////////////////////////////////////////////

    @Query("UPDATE "+PERSONS_TABLE+" SET "+
            DUE_PAYMENT+" = CASE :type WHEN "+TYPE_CREDIT+" THEN "+DUE_PAYMENT+" + :amount ELSE "+DUE_PAYMENT+" - :amount END " +
            "WHERE "+ ExpensesContract.PersonsColumns._ID+" = :personId")
    abstract void setPersonDue(long personId, int type, float amount);

    @Query("DELETE FROM `persons`")
    abstract void clearPeople();

    ////////////////////////////////////////////////////////////////////////////////////
    ///                              Transaction                                    ///
    //////////////////////////////////////////////////////////////////////////////////

    public long insertTransaction(Transaction t) {
        long _id = insert_transaction_internal(t);
        if (_id > 0) {
            setAccountBalance(t.getAccountId(),t.getType(),t.getAmount());
            if (null != t.getPersonId()) {
                setPersonDue(t.getPersonId(),t.getType(),t.getAmount());
            }
        }
        return _id;
    }

    public int updateTransaction(Transaction newT) {
        Transaction oldT = getTransactionById(newT.getTransactionId());
        int changes = update_transaction_internal(newT);
        if (changes > 0) {
            setAccountBalance(oldT.getAccountId(),TYPE_CREDIT == oldT.getType() ? TYPE_DEBIT : TYPE_CREDIT,oldT.getAmount());
            setAccountBalance(newT.getAccountId(),newT.getType(),newT.getAmount());
            if (null != oldT.getPersonId()) {
                setPersonDue(oldT.getPersonId(),TYPE_CREDIT == oldT.getType() ? TYPE_DEBIT : TYPE_CREDIT, oldT.getAmount());
            }
            if (null != newT.getPersonId()) {
                setPersonDue(newT.getPersonId(),newT.getType(),newT.getAmount());
            }
        }
        return changes;
    }

    public int deleteTransactions(Transaction t) {
        t.setDeleted(true);
        int changes = update_transaction_internal(t);
        if (changes > 0) {
            setAccountBalance(t.getAccountId(),TYPE_CREDIT == t.getType() ? TYPE_DEBIT : TYPE_CREDIT,t.getAmount());
            if (null != t.getPersonId()) {
                setPersonDue(t.getPersonId(),TYPE_CREDIT == t.getType() ? TYPE_DEBIT : TYPE_CREDIT, t.getAmount());
            }
        }
        return changes;
    }

    @Query("UPDATE `transactions` SET `deleted` = 1 WHERE `date` < :date")
    public abstract void markTransactionDeletedOlderThan(Date date);

    @Query("DELETE FROM `transactions` WHERE `deleted` = 1;")
    public abstract void removeTransactionsMarkedDeleted();

    @Query("SELECT * FROM "+TRANSACTIONS_TABLE+" WHERE "+ ExpensesContract.TransactionsColumns._ID+" = :transactionId;")
    abstract Transaction getTransactionById(long transactionId);

    @Insert
    abstract long insert_transaction_internal(Transaction newTransaction);

    @Update
    abstract int update_transaction_internal(Transaction transaction);

    ////////////////////////////////////////////////////////////////////////////////////
    ///                             Money Transfer                                  ///
    //////////////////////////////////////////////////////////////////////////////////

    public long insertMoneyTransfer(MoneyTransfer mt) {
        long id = insert_money_transfer_internal(mt);
        if (id > 0) {
            setAccountBalance(mt.getPayee_account_id(), TYPE_DEBIT, mt.getAmount());
            setAccountBalance(mt.getPayer_account_id(),TYPE_CREDIT,mt.getAmount());
        }
        return id;
    }

    public int updateMoneyTransfer(MoneyTransfer newMT) {
        MoneyTransfer oldMT = getMoneyTransferById(newMT.getId());
        int changes = update_money_transfer_internal(newMT);
        if (changes > 0) {
            setAccountBalance(oldMT.getPayee_account_id(),TYPE_CREDIT,oldMT.getAmount());
            setAccountBalance(oldMT.getPayer_account_id(),TYPE_DEBIT,oldMT.getAmount());
            setAccountBalance(newMT.getPayee_account_id(),TYPE_DEBIT,newMT.getAmount());
            setAccountBalance(newMT.getPayer_account_id(),TYPE_CREDIT,newMT.getAmount());
        }
        return changes;
    }

    public int deleteMoneyTransfer(MoneyTransfer mt) {
        MoneyTransfer oldMT = getMoneyTransferById(mt.getId());
        int changes = delete_money_transfer_internal(mt);
        if (changes > 0) {
            setAccountBalance(oldMT.getPayee_account_id(),TYPE_CREDIT,oldMT.getAmount());
            setAccountBalance(oldMT.getPayer_account_id(),TYPE_DEBIT,oldMT.getAmount());
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

    ////////////////////////////////////////////////////////////////////////////////////
    ///                              Miscellaneous                                  ///
    //////////////////////////////////////////////////////////////////////////////////

    @androidx.room.Transaction
    public void clearAll() {
        clearAccounts();
        clearPeople();
    }
}
