package dreammaker.android.expensetracker.database;

import android.database.Cursor;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import static dreammaker.android.expensetracker.database.ExpensesContract.AboutAccountColumns.BALANCE;
import static dreammaker.android.expensetracker.database.ExpensesContract.AboutPersonColumns.DUE_PAYMENT;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.ACCOUNTS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.PERSONS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.TRANSACTIONS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.ACCOUNT_ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.AMOUNT;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.PERSON_ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE_CREDIT;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE_DEBIT;

//@Dao
public abstract class ExpensesBackupDao {

    @androidx.room.Transaction
    @Insert
    public abstract void insertAccounts(List<Account> accounts);

    @androidx.room.Transaction
    @Insert
    public abstract void insertPeople(List<Person> people);

    @androidx.room.Transaction
    @Insert
    public abstract void insertTransactions(List<Transaction> transactions);

    @androidx.room.Transaction
    @Insert
    public abstract void insertMoneyTransfers(List<MoneyTransfer> moneyTransfers);

    @Query("SELECT CASE WHEN (count_accounts+count_people) > 0 THEN 0 ELSE 1 END AS is_empty FROM " +
            "(SELECT COUNT("+ ExpensesContract.AccountsColumns._ID +") AS count_accounts  FROM "+ACCOUNTS_TABLE+") AS a," +
            " (SELECT COUNT("+ ExpensesContract.PersonsColumns._ID +") AS count_people FROM "+PERSONS_TABLE+") AS p;")
    public abstract boolean isDatabaseEmpty();

    @Query("SELECT * FROM "+ACCOUNTS_TABLE)
    public abstract List<Account> getAccounts();

    @Query("SELECT * FROM "+PERSONS_TABLE)
    public abstract List<Person> getPeople();

    @Query("SELECT * FROM "+TRANSACTIONS_TABLE)
    public abstract List<Transaction> getTransactions();

    @Query("SELECT * FROM `money_transfers`;")
    public abstract List<MoneyTransfer> getMoneyTransfers();

    @Query("SELECT "+ACCOUNT_ID +
            ", SUM(CASE "+TYPE+" WHEN "+TYPE_CREDIT+" THEN -"+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN "+AMOUNT+" ELSE 0 END) AS "+BALANCE+
            " FROM "+TRANSACTIONS_TABLE+" GROUP BY "+ACCOUNT_ID)
    abstract Cursor calculateAccountsBalances();

    @Query("SELECT "+PERSON_ID+
            ", SUM(CASE "+TYPE+" WHEN "+TYPE_CREDIT+" THEN "+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN -"+AMOUNT+" ELSE 0 END) AS "+DUE_PAYMENT+
            " FROM "+TRANSACTIONS_TABLE+" GROUP BY "+PERSON_ID)
    abstract Cursor calculatePeopleDues();

    @Query("UPDATE "+ACCOUNTS_TABLE+" SET "+BALANCE+" = :balance " +
            "WHERE "+ ExpensesContract.AccountsColumns._ID+" = :accountId;")
    abstract void setAccountBalance(long accountId, float balance);

    @Query("UPDATE "+PERSONS_TABLE+" SET "+DUE_PAYMENT+" = :due" +
            " WHERE "+ ExpensesContract.PersonsColumns._ID+" = :personId;")
    abstract void setPersonDue(long personId, float due);

    public void setAccountsBalancesAndPeopleDues() {
        final Cursor cBalances = calculateAccountsBalances();
        if (null != cBalances) {
            while (cBalances.moveToNext()) {
                final long accountId = cBalances.getLong(cBalances.getColumnIndex(ACCOUNT_ID));
                final float balance = cBalances.getFloat(cBalances.getColumnIndex(BALANCE));
                setAccountBalance(accountId,balance);
            }
        }
        final Cursor cDues = calculatePeopleDues();
        if (null != cDues) {
            while (cDues.moveToNext()) {
                final long personId = cDues.getLong(cDues.getColumnIndex(PERSON_ID));
                final float due = cDues.getFloat(cDues.getColumnIndex(DUE_PAYMENT));
                setPersonDue(personId,due);
            }
        }
    }
}
