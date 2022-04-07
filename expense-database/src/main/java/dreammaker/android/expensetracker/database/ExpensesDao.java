package dreammaker.android.expensetracker.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQueryBuilder;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;

import static dreammaker.android.expensetracker.util.Date.ISO_DATE_PATTERN;

@Dao
public abstract class ExpensesDao {

    ////////////////////////////////////////////////////////////////////////////////////
    ///                                 Account                                     ///
    //////////////////////////////////////////////////////////////////////////////////

    @Insert
    public abstract long insertAccount(Account account);

    @Query("SELECT * FROM `accounts`")
    public abstract LiveData<List<AboutAccount>> getAllAccountsForListOfAccounts();

    @Query("SELECT COUNT(`_id`) FROM `accounts`")
    public abstract long countAccounts();

    @Query("SELECT * FROM `accounts`")
    public abstract LiveData<List<Account>> getAllAccounts();

    @Update
    public abstract int updateAccount(Account account);

    @Query("UPDATE `accounts` SET `balance` = CASE :type WHEN 0 THEN `balance` - :amount ELSE `balance` + :amount END " +
            "WHERE `_id` = :accountId")
    @Deprecated
    abstract void setAccountBalance(long accountId, int type, float amount);

    @Query("UPDATE `accounts` SET `balance` =  `balance` + :amount WHERE `_id` = :accountId")
    abstract void addAccountBalance(long accountId, float amount);

    @Delete
    public abstract int deleteAccounts(Account... accounts);

    @Query("DELETE FROM `accounts`;")
    abstract void clearAccounts();

    ////////////////////////////////////////////////////////////////////////////////////
    ///                                 Person                                      ///
    //////////////////////////////////////////////////////////////////////////////////

    @Insert
    public abstract long insertPerson(Person person);

    @Query("SELECT * FROM `persons` ORDER BY `due` DESC")
    public abstract LiveData<List<AboutPerson>> getAllPersonsForListOfPersons();

    @Query("SELECT * FROM `persons`")
    public abstract LiveData<List<Person>> getAllPersonsNameAndId();

    @Update
    public abstract int updatePerson(Person person);

    @Query("UPDATE `persons` SET `due` = CASE :type WHEN 0 THEN `due` + :amount ELSE `due` - :amount END " +
            "WHERE `_id` = :personId")
    @Deprecated
    abstract void setPersonDue(long personId, int type, float amount);

    @Query("UPDATE `persons` SET `due` = `due` + :amount WHERE `_id` = :personId")
    abstract void addPersonDue(long personId, float amount);

    @Delete
    public abstract int deletePersons(Person... persons);

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

    @androidx.room.Transaction
    @RawQuery(observedEntities = {TransactionDetails.class})
    public abstract DataSource.Factory<Integer, TransactionDetails> filterTransactionPaged(SupportSQLiteQuery query);

    public int updateTransaction(Transaction newT) {
        Transaction oldT = getTransactionById(newT.getTransactionId());
        int changes = update_transaction_internal(newT);
        if (changes > 0) {
            setAccountBalance(oldT.getAccountId(),0 == oldT.getType() ? 1 : 0,oldT.getAmount());
            setAccountBalance(newT.getAccountId(),newT.getType(),newT.getAmount());
            if (null != oldT.getPersonId()) {
                setPersonDue(oldT.getPersonId(),0 == oldT.getType() ? 1 : 0, oldT.getAmount());
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
            setAccountBalance(t.getAccountId(),0 == t.getType() ? 1 : 0,t.getAmount());
            if (null != t.getPersonId()) {
                setPersonDue(t.getPersonId(),0 == t.getType() ? 1 : 0, t.getAmount());
            }
        }
        return changes;
    }

    @Query("UPDATE `transactions` SET `deleted` = 1 WHERE `date` < :date")
    public abstract void markTransactionDeletedOlderThan(Date date);

    @Query("DELETE FROM `transactions` WHERE `deleted` = 1;")
    public abstract void removeTransactionsMarkedDeleted();

    @Query("SELECT * FROM `transactions` WHERE `_id` = :transactionId;")
    public abstract Transaction getTransactionById(long transactionId);

    @Insert
    protected abstract long insert_transaction_internal(Transaction newTransaction);

    @Update
    protected abstract int update_transaction_internal(Transaction transaction);

    ////////////////////////////////////////////////////////////////////////////////////
    ///                             Money Transfer                                  ///
    //////////////////////////////////////////////////////////////////////////////////

    public long insertMoneyTransfer(MoneyTransfer mt) {
        long id = insert_money_transfer_internal(mt);
        if (id > 0) {
            //setAccountBalance(mt.getPayee_account_id(), 1, mt.getAmount());
            //setAccountBalance(mt.getPayer_account_id(),0,mt.getAmount());

            addAccountBalance(mt.getPayee_account_id(), mt.getAmount());
            addAccountBalance(mt.getPayer_account_id(),-mt.getAmount());
        }
        return id;
    }

    @androidx.room.Transaction
    @Query("SELECT * FROM `money_transfers`;")
    public abstract LiveData<List<MoneyTransferDetails>> getMoneyTransferHistory();

    public int updateMoneyTransfer(MoneyTransfer newMT) {
        MoneyTransfer oldMT = getMoneyTransferById(newMT.getId());
        int changes = update_money_transfer_internal(newMT);
        if (changes > 0) {
            /*setAccountBalance(oldMT.getPayee_account_id(),0,oldMT.getAmount());
            setAccountBalance(oldMT.getPayer_account_id(),1,oldMT.getAmount());
            setAccountBalance(newMT.getPayee_account_id(),1,newMT.getAmount());
            setAccountBalance(newMT.getPayer_account_id(),0,newMT.getAmount());*/

            addAccountBalance(oldMT.getPayee_account_id(),-oldMT.getAmount());
            addAccountBalance(oldMT.getPayer_account_id(),oldMT.getAmount());
            addAccountBalance(newMT.getPayee_account_id(), newMT.getAmount());
            addAccountBalance(newMT.getPayer_account_id(),-newMT.getAmount());
        }
        return changes;
    }

    public int deleteMoneyTransfer(MoneyTransfer mt) {
        MoneyTransfer oldMT = getMoneyTransferById(mt.getId());
        int changes = delete_money_transfer_internal(mt);
        if (changes > 0) {
            //setAccountBalance(oldMT.getPayee_account_id(),0,oldMT.getAmount());
            //setAccountBalance(oldMT.getPayer_account_id(),1,oldMT.getAmount());

            addAccountBalance(oldMT.getPayee_account_id(), -oldMT.getAmount());
            addAccountBalance(oldMT.getPayer_account_id(), oldMT.getAmount());
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

    @Query("SELECT * FROM " +
            "(SELECT SUM(`balance`) AS `total_balance`, COUNT(`_id`) AS `count_accounts`"+
            " FROM `accounts` WHERE `balance` > 0), " +
            "(SELECT SUM(`due`) AS `total_due`, COUNT(`_id`) AS `count_people`"+
            " FROM `persons` WHERE included = 0 AND `due` > 0);")
    public abstract LiveData<BalanceAndDueSummary> getBalanceAndDueSummary();

    @androidx.room.Transaction
    public void clearAll() {
        clearAccounts();
        clearPeople();
    }


    public static class TransactionDetailsQueryBuilder{
        private static final String TAG = "TransactionDetailsQB";

        private List<Account> accounts = null;
        private List<Person> people = null;
        private boolean typeCredit = true, 
                        typeDebit = true;
        private float minAmount = Float.MIN_VALUE,
                      maxAmount = Float.MAX_VALUE;
        private Date minDate = null,
                     maxDate = null;

        public TransactionDetailsQueryBuilder minAmount(float minAmount) {
            this.minAmount = minAmount;
            return this;
        }

        public TransactionDetailsQueryBuilder maxAmount(float maxAmount) {
            this.maxAmount = maxAmount;
            return this;
        }

        public TransactionDetailsQueryBuilder minDate(Date minDate){
            if (null != minDate)
                this.minDate = minDate.clone();
            return this;
        }

        public TransactionDetailsQueryBuilder maxDate(Date maxDate){
            if (null != maxDate)
                this.maxDate = maxDate.clone();
            return this;
        }

        public TransactionDetailsQueryBuilder accounts(List<Account> accounts) {
            this.accounts = null == accounts || accounts.isEmpty() ? Collections.EMPTY_LIST
                    : new ArrayList<>(accounts);
            return this;
        }

        public TransactionDetailsQueryBuilder people(List<Person> people) {
            this.people = null == people || people.isEmpty() ? Collections.EMPTY_LIST
                    : new ArrayList<>(people);
            return this;
        }

        public TransactionDetailsQueryBuilder credits(boolean show){
            this.typeCredit = show;
            return this;
        }

        public TransactionDetailsQueryBuilder debits(boolean show) {
            this.typeDebit = show;
            return this;
        }

        public SupportSQLiteQuery build(){
            final List<Account> accounts = this.accounts;
            final List<Person> people = this.people;
            final boolean typeCredit = this.typeCredit;
            final boolean typeDebit = this.typeDebit;
            final float minAmount = this.minAmount;
            final float maxAmount = this.maxAmount;
            final Date minDate = this.minDate;
            final Date maxDate = this.maxDate;
            ArrayList<String> parts = new ArrayList<>();
            StringBuilder selection = new StringBuilder();
            int i;
            parts.add("deleted = 0");
            if (null != minDate) {
                parts.add("date >= \""+minDate.format(ISO_DATE_PATTERN)+"\"");
            }
            if (null != maxDate) {
                parts.add("date <= \""+maxDate.format(ISO_DATE_PATTERN)+"\"");
            }
            if (!typeCredit || !typeDebit) {
                if (typeCredit) {
                    parts.add("type = "+0);
                }
                if (typeDebit) {
                    parts.add("type = "+1);
                }
            }
            if (Float.MIN_VALUE != minAmount || Float.MAX_VALUE != maxAmount) {
                if (Float.MIN_VALUE != minAmount) {
                    parts.add("amount >= "+minAmount);
                }
                if (Float.MAX_VALUE != maxAmount) {
                    parts.add("amount <= "+maxAmount);
                }
            }
            if (null != accounts && !accounts.isEmpty()) {
                i = 0;
                StringBuilder sBuilder = new StringBuilder("account_id IN(");
                for (Account a : accounts) {
                    if (i > 0) sBuilder.append(",");
                    sBuilder.append(a.getAccountId());
                    i++;
                }
                parts.add(sBuilder.append(")").toString());
            }
            if (null != people && !people.isEmpty()) {
                boolean hasNull = false;
                ListIterator<Person> it = people.listIterator();
                if (it.hasNext()) {
                    Person p = it.next();
                    if (null == p  || p.getPersonId() <= 0) {
                        hasNull = true;
                        it.remove();
                    }
                }
                if (hasNull) parts.add("person_id IS NULL");
                if (!people.isEmpty()) {
                    StringBuilder s = new StringBuilder("person_id IN(");
                    i = 0;
                    for (Person p : people) {
                        if (i > 0) s.append(",");
                        s.append(p.getPersonId());
                        i++;
                    }
                    parts.add(s.append(")").toString());
                }
            }
            i = 0;
            for (String p : parts) {
                if (i > 0) selection.append(" AND ");
                selection.append(p);
                i++;
            }
            return SupportSQLiteQueryBuilder
                    .builder("transactions")
                    .selection(selection.toString(),null)
                    .orderBy("date DESC")
                    .create();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TransactionDetailsQueryBuilder that = (TransactionDetailsQueryBuilder) o;
            return typeCredit == that.typeCredit &&
                    typeDebit == that.typeDebit &&
                    Float.compare(that.minAmount, minAmount) == 0 &&
                    Float.compare(that.maxAmount, maxAmount) == 0 &&
                    Check.isEquals(accounts,that.accounts) &&
                    Check.isEquals(people,that.people) &&
                    Check.isEquals(minDate,that.minDate) &&
                    Check.isEquals(maxDate,that.maxDate);
        }
    }
}
