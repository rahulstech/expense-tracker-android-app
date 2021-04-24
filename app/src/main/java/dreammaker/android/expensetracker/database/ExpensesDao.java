package dreammaker.android.expensetracker.database;

import java.util.ArrayList;
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

import static dreammaker.android.expensetracker.database.ExpensesContract.AboutAccountColumns.BALANCE;
import static dreammaker.android.expensetracker.database.ExpensesContract.AboutPersonColumns.DUE_PAYMENT;
import static dreammaker.android.expensetracker.database.ExpensesContract.AccountsColumns.ACCOUNT_NAME;
import static dreammaker.android.expensetracker.database.ExpensesContract.BalanceAndDueSummaryColumns.COUNT_ACCOUNTS;
import static dreammaker.android.expensetracker.database.ExpensesContract.BalanceAndDueSummaryColumns.COUNT_PEOPLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.BalanceAndDueSummaryColumns.TOTAL_BALANCE;
import static dreammaker.android.expensetracker.database.ExpensesContract.BalanceAndDueSummaryColumns.TOTAL_DUE;
import static dreammaker.android.expensetracker.database.ExpensesContract.PersonsColumns.PERSON_NAME;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.ACCOUNTS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.PERSONS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.TRANSACTIONS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.ACCOUNT_ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.AMOUNT;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.DATE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.PERSON_ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE_CREDIT;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE_DEBIT;
import static dreammaker.android.expensetracker.util.Date.ISO_DATE_PATTERN;

@Dao
public abstract class ExpensesDao {

    @Insert
    public abstract long insertAccount(Account account);

    @Query("SELECT * FROM "+ACCOUNTS_TABLE)
    public abstract LiveData<List<AboutAccount>> getAllAccountsForListOfAccounts();

    @Query("SELECT COUNT("+ ExpensesContract.AccountsColumns._ID +") FROM "+ACCOUNTS_TABLE)
    public abstract long countAccounts();

    @Query("SELECT "+ ExpensesContract.AccountsColumns._ID+","+ACCOUNT_NAME+", 0 AS "+BALANCE+" FROM "+ACCOUNTS_TABLE)
    public abstract LiveData<List<Account>> getAllAccountsNameAndId();

    @Update
    public abstract int updateAccount(Account account);

    @Delete
    public abstract int deleteAccounts(Account... accounts);

    @Insert
    public abstract long insertPerson(Person person);

    @Query("SELECT * FROM "+PERSONS_TABLE+" ORDER BY "+DUE_PAYMENT+" DESC")
    public abstract LiveData<List<AboutPerson>> getAllPersonsForListOfPersons();

    @Query("SELECT "+ ExpensesContract.PersonsColumns._ID+","+PERSON_NAME+", 0 AS "+DUE_PAYMENT+" FROM "+PERSONS_TABLE)
    public abstract LiveData<List<Person>> getAllPersonsNameAndId();

    @Update
    public abstract int updatePerson(Person person);

    @Delete
    public abstract int deletePersons(Person... persons);


    public long insertTransaction(Transaction t) {
        long _id = insert_transaction_internal(t);
        if (_id > 0) {
            float newAmount = TYPE_CREDIT == t.getType() ? -t.getAmount() : t.getAmount();
            long accountId = t.getAccountId();
            Long personId = t.getPersonId();
            float oldBalance = getAccountBalance(accountId);
            float newBalance = oldBalance+newAmount;
            setAccountBalance(accountId,newBalance);
            if (null != personId) {
                float oldDue = getPersonDue(personId);
                float newDue = oldDue-newAmount;
                setPersonDue(personId,newDue);
            }
        }
        return _id;
    }

    @Query("SELECT * FROM " +
            "(SELECT SUM("+BALANCE+") AS "+TOTAL_BALANCE+", COUNT("+ ExpensesContract.AccountsColumns._ID+") AS "+COUNT_ACCOUNTS+
            " FROM "+ACCOUNTS_TABLE+" WHERE "+BALANCE+" > 0), " +
            "(SELECT SUM("+DUE_PAYMENT+") AS "+TOTAL_DUE+", COUNT("+ ExpensesContract.PersonsColumns._ID+") AS "+COUNT_PEOPLE+
            " FROM "+PERSONS_TABLE+" WHERE "+DUE_PAYMENT+" > 0);")
    public abstract LiveData<BalanceAndDueSummary> getBalanceAndDueSummary();

    @androidx.room.Transaction
    @RawQuery(observedEntities = {TransactionDetails.class})
    public abstract DataSource.Factory<Integer, TransactionDetails> filterTransactionPaged(SupportSQLiteQuery query);


    public int updateTransaction(Transaction newT) {
        Transaction oldT = getTransactionById(newT.getTransactionId());
        int changes = update_transaction_internal(newT);
        if (changes > 0) {
            float oldAmount = TYPE_CREDIT == oldT.getType() ? -oldT.getAmount() : oldT.getAmount();
            long oldAccountId = oldT.getAccountId();
            Long oldPersonId = oldT.getPersonId();
            float newAmount = TYPE_CREDIT == newT.getType() ? -newT.getAmount() : newT.getAmount();
            long newAccountId = newT.getAccountId();
            Long newPersonId = newT.getPersonId();
            if (oldAccountId == newAccountId) {
                float oldBalance = getAccountBalance(oldAccountId);
                float newBalance = oldBalance - oldAmount + newAmount;
                setAccountBalance(newAccountId,newBalance);
            }
            else {
                float oldAccountOldBalance = getAccountBalance(oldAccountId);
                float oldAccountNewBalance = oldAccountOldBalance - oldAmount;
                float newAccountOldBalance = getAccountBalance(newAccountId);
                float newAccountNewBalance = newAccountOldBalance + newAmount;
                setAccountBalance(oldAccountId,oldAccountNewBalance);
                setAccountBalance(newAccountId,newAccountNewBalance);
            }
            if (oldPersonId == newPersonId && null != oldPersonId) {
                float oldDue = getPersonDue(oldPersonId);
                float newDue = oldDue+oldAmount-newAmount;
                setPersonDue(newPersonId,newDue);
            }
            else {
                if (null != oldPersonId) {
                    float oldPersonOldDue = getPersonDue(oldPersonId);
                    float oldPersonNewDue = oldPersonOldDue+oldAmount;
                    setPersonDue(oldPersonId,oldPersonNewDue);
                }
                if (null != newPersonId) {
                    float newPersonOldDue = getPersonDue(newPersonId);
                    float newPersonNewDue = newPersonOldDue-newAmount;
                    setPersonDue(newPersonId,newPersonNewDue);
                }
            }
        }
        return changes;
    }


    public int deleteTransactions(Transaction t) {
        int changes = delete_transaction_internal(t);
        if (changes > 0) {
            float oldAmount = TYPE_CREDIT == t.getType() ? -t.getAmount() : t.getAmount();
            long accountId = t.getAccountId();
            Long personId = t.getPersonId();
            float oldBalance = getAccountBalance(accountId);
            float newBalance = oldBalance - oldAmount;
            setAccountBalance(accountId, newBalance);
            if (null != personId) {
                float oldDue = getPersonDue(personId);
                float newDue = oldDue + oldAmount;
                setPersonDue(personId, newDue);
            }
        }
        return changes;
    }

    @Query("SELECT "+BALANCE+" FROM "+ACCOUNTS_TABLE+" WHERE "+ ExpensesContract.AccountsColumns._ID+" = :accountId")
    abstract float getAccountBalance(long accountId);

    @Query("UPDATE "+ACCOUNTS_TABLE+" SET "+BALANCE+" = :newBalance WHERE "+ ExpensesContract.AccountsColumns._ID+" = :accountId")
    abstract void setAccountBalance(long accountId, float newBalance);

    @Query("SELECT "+DUE_PAYMENT+" FROM "+PERSONS_TABLE+" WHERE "+ ExpensesContract.PersonsColumns._ID+" = :personId")
    abstract float getPersonDue(long personId);

    @Query("UPDATE "+PERSONS_TABLE+" SET "+DUE_PAYMENT+" = :newDue WHERE "+ ExpensesContract.PersonsColumns._ID+" = :personId")
    abstract void setPersonDue(long personId, float newDue);

    @Query("SELECT * FROM "+TRANSACTIONS_TABLE+" WHERE "+ ExpensesContract.TransactionsColumns._ID+" = :transactionId;")
    abstract Transaction getTransactionById(long transactionId);

    @Insert
    abstract long insert_transaction_internal(Transaction newTransaction);

    @Update
    abstract int update_transaction_internal(Transaction transaction);

    @Delete
    abstract int delete_transaction_internal(Transaction transaction);

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
            this.minDate = minDate;
            return this;
        }

        public TransactionDetailsQueryBuilder maxDate(Date maxDate){
            this.maxDate = maxDate;
            return this;
        }

        public TransactionDetailsQueryBuilder accounts(List<Account> accounts) {
            this.accounts = accounts;
            return this;
        }

        public TransactionDetailsQueryBuilder people(List<Person> people) {
            this.people = people;
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
            if (null != minDate) {
                parts.add(DATE+" >= \""+minDate.format(ISO_DATE_PATTERN)+"\"");
            }
            if (null != maxDate) {
                parts.add(DATE+" <= \""+maxDate.format(ISO_DATE_PATTERN)+"\"");
            }
            if (!typeCredit || !typeDebit) {
                if (typeCredit) {
                    parts.add(TYPE+" = "+TYPE_CREDIT);
                }
                if (typeDebit) {
                    parts.add(TYPE+" = "+TYPE_DEBIT);
                }
            }
            if (Float.MIN_VALUE != minAmount || Float.MAX_VALUE != maxAmount) {
                if (Float.MIN_VALUE != minAmount) {
                    parts.add(AMOUNT+" >= "+minAmount);
                }
                if (Float.MAX_VALUE != maxAmount) {
                    parts.add(AMOUNT+" <= "+maxAmount);
                }
            }
            if (null != accounts && !accounts.isEmpty()) {
                i = 0;
                StringBuilder sBuilder = new StringBuilder(ACCOUNT_ID + " IN(");
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
                    if (null == p  || null == p.getPersonId()) {
                        hasNull = true;
                        it.remove();
                    }
                }
                if (hasNull) parts.add(PERSON_ID+" IS NULL");
                if (!people.isEmpty()) {
                    StringBuilder s = new StringBuilder(PERSON_ID + " IN(");
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
                    .builder(TRANSACTIONS_TABLE)
                    .selection(selection.toString(),null)
                    .orderBy(DATE+" DESC")
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
