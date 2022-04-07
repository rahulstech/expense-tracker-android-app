package dreammaker.android.expensetracker.database;

import android.database.Cursor;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
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
            "(SELECT COUNT(`_id`) AS count_accounts  FROM `accounts`) AS a," +
            " (SELECT COUNT(`_id`) AS count_people FROM `persons`) AS p;")
    public abstract boolean isDatabaseEmpty();

    @Query("SELECT * FROM `accounts`")
    public abstract List<Account> getAccounts();

    @Query("SELECT * FROM `persons`")
    public abstract List<Person> getPeople();

    @Query("SELECT * FROM `transactions`")
    public abstract List<Transaction> getTransactions();

    @Query("SELECT * FROM `money_transfers`;")
    public abstract List<MoneyTransfer> getMoneyTransfers();

    @Query("SELECT `account_id`, SUM(`amount`) `balance` FROM `transactions` GROUP BY `account_id`")
    abstract Cursor calculateAccountsBalances();

    @Query("SELECT `person_id`, SUM(`amount`) `due` FROM `transactions` GROUP BY `person_id`")
    abstract Cursor calculatePeopleDues();

    @Query("UPDATE `accounts` SET `balance` = :balance WHERE `_id` = :accountId;")
    abstract void setAccountBalance(long accountId, float balance);

    @Query("UPDATE `persons` SET `due` = :due WHERE `_id` = :personId;")
    abstract void setPersonDue(long personId, float due);

    public void setAccountsBalancesAndPeopleDues() {
        final Cursor cBalances = calculateAccountsBalances();
        if (null != cBalances) {
            while (cBalances.moveToNext()) {
                final long accountId = cBalances.getLong(0);
                final float balance = cBalances.getFloat(1);
                setAccountBalance(accountId,balance);
            }
        }
        final Cursor cDues = calculatePeopleDues();
        if (null != cDues) {
            while (cDues.moveToNext()) {
                final long personId = cDues.getLong(0);
                final float due = cDues.getFloat(1);
                setPersonDue(personId,due);
            }
        }
    }
}
