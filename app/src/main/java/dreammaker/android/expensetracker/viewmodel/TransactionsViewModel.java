package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.database.BalanceAndDueSummary;
import dreammaker.android.expensetracker.database.ExpensesDao;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.database.TransactionDetails;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.util.Helper;
import dreammaker.android.expensetracker.util.ResultCallback;

import static dreammaker.android.expensetracker.util.Helper.ACTION_DELETE;
import static dreammaker.android.expensetracker.util.Helper.ACTION_EDIT;
import static dreammaker.android.expensetracker.util.Helper.ACTION_INSERT;

public class TransactionsViewModel extends BaseViewModel {

    private static final boolean DEBUG = true;

    private static final String TAG = "TransactionsViewModel";

    public static final int OPERATION_SHOW_TRANSACTION_FOR_ACCOUNT = 4;
    public static final int OPERATION_SHOW_TRANSACTION_FOR_PERSON = 5;
    public static final int OPERATION_SHOW_TRANSACTION = 6;

    private LiveData<BalanceAndDueSummary> balanceAndDueSummaryLiveData;
    private LiveData<List<Account>> accountNameIdLiveData;
    private LiveData<List<Person>> personNameIdLiveDate;
    private MutableLiveData<ExpensesDao.TransactionDetailsQueryBuilder> transactionDetailsQueryBuilderLiveData;
    private LiveData<PagedList<TransactionDetails>> transactionsPaged;

    private Stack<FilterTransactionParams> filterParamStack = new Stack<>();

    private MutableLiveData<FilterTransactionParams> workingFilterParamLiveData;
    private MutableLiveData<Transaction> workingTransactionLiveData;

    private int showOperationCode = OPERATION_SHOW_TRANSACTION;
    private Object extraData = null;

    public TransactionsViewModel(@NonNull Application application) {
        super(application);
        balanceAndDueSummaryLiveData = getDao().getBalanceAndDueSummary();
        accountNameIdLiveData = getDao().getAllAccounts();
        personNameIdLiveDate = getDao().getAllPersonsNameAndId();
        transactionDetailsQueryBuilderLiveData = new MutableLiveData<>();
        transactionsPaged = Transformations.switchMap(transactionDetailsQueryBuilderLiveData, input -> new LivePagedListBuilder<>(
                getDao().filterTransactionPaged(input.build()), 20).build());
        workingFilterParamLiveData = new MutableLiveData<>();
        workingTransactionLiveData = new MutableLiveData<>();
    }

    ////////////////////////////////////////////////////////////////////////////////
    ///                    Methods For Filter Transaction                       ///
    //////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param person
     */
    public void loadTransactionsForPerson(@Nullable Person person) {
        showOperationCode = OPERATION_SHOW_TRANSACTION_FOR_PERSON;
        extraData = person;
        loadTransactions_internal();
    }

    /**
     *
     * @param account
     */
    public void loadTransactionsForAccount(@NonNull Account account) {
        showOperationCode = OPERATION_SHOW_TRANSACTION_FOR_ACCOUNT;
        extraData = account;
        loadTransactions_internal();
    }

    /**
     *
     */
    public void  loadAllTransactions() {
        showOperationCode = OPERATION_SHOW_TRANSACTION;
        extraData = null;
        loadTransactions_internal();
    }

    private void loadTransactions_internal() {
        FilterTransactionParams params = new FilterTransactionParams()
                .setDateType(FilterTransactionParams.DATE_CUSTOM_RANGE)
                .setMaxDate(new Date());
        if (OPERATION_SHOW_TRANSACTION_FOR_ACCOUNT == showOperationCode) {
            params.addSelectedAccount((Account) extraData);
        }
        else if (OPERATION_SHOW_TRANSACTION_FOR_PERSON == showOperationCode) {
            params.addSelectedPerson((Person) extraData);
        }
        filter(params);
    }

    /**
     *
     */
    public void reloadWithLastFilterTransactionParameters() {
        if (!filterParamStack.isEmpty()) {
            filter_transactions(filterParamStack.peek());
        }
    }

    /**
     *
     * @param params
     */
    public void filter(FilterTransactionParams params) {
        if (!filterParamStack.isEmpty()) {
            final FilterTransactionParams top = filterParamStack.peek();
            if (DEBUG) Log.d(TAG, "top="+top+" | param="+params);
            if (top.equals(params)) return;
        }
        if (Check.isNonNull(params)) {
            final FilterTransactionParams copy = params.clone();
            filterParamStack.push(copy);
            filter_transactions(params);
        }
    }

    /**
     *
     * @return
     */
    public boolean reset(){
        filterParamStack.pop();
        if (!filterParamStack.isEmpty()) {
            filter_transactions(filterParamStack.peek());
            showOperationCode = -1;
            extraData = null;
            return true;
        }
        return false;
    }

    /**
     *
     */
    public void resetToTop() {
        if (1 == filterParamStack.size()) return;
        final FilterTransactionParams bottom = filterParamStack.firstElement();
        filterParamStack.clear();
        filter(bottom);
    }

    /**
     *
     * @param params
     */
    private void filter_transactions(@NonNull final FilterTransactionParams params) {
        ExpensesDao.TransactionDetailsQueryBuilder qb = new ExpensesDao.TransactionDetailsQueryBuilder();
        qb.accounts(params.selectedAccounts)
                .people(params.selectedPeople)
                .minAmount(params.minAmount)
                .maxAmount(params.maxAmount)
                .credits(params.credit)
                .debits(params.debit)
                .minDate(params.minDate)
                .maxDate(params.maxDate);
        transactionDetailsQueryBuilderLiveData.setValue(qb);
        workingFilterParamLiveData.setValue(params);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    ///                                       CRUD Methods                                        ///
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void insertTransaction(final Transaction transaction){
        AppExecutor.getDiskOperationsExecutor().execute(() -> {
            boolean success = false;
            try{
                success = getDao().insertTransaction(transaction) > 0;
            }
            catch (Exception e){
                Log.e(TAG, "error on inserting transaction with message: "+e.getMessage());
            }
            finally {
                notifyOperationCallback(ACTION_INSERT, success);
            }
        });
    }

    public LiveData<BalanceAndDueSummary> getBalanceAndDueSummaryLiveData() {
        return balanceAndDueSummaryLiveData;
    }

    public LiveData<PagedList<TransactionDetails>> getTransactionsPaged() { return transactionsPaged;}

    public void updateTransaction(final Transaction transaction){
        AppExecutor.getDiskOperationsExecutor().execute(() -> {
            boolean success = false;
            try {
                success = getDao().updateTransaction(transaction) > 0;
            }
            catch (Exception e){
                Log.e(TAG, "error on updating transaction with message: "+e.getMessage());
            }
            finally {
                notifyOperationCallback(ACTION_EDIT, success);
            }
        });
    }

    public void deleteTransaction(final Transaction transactions) {
        if (Check.isNonNull(transactions)) {
            AppExecutor.getDiskOperationsExecutor().execute(() -> {
                int changes = 0;
                try {
                    changes = getDao().deleteTransactions(transactions);
                } catch (Exception e) {
                    Log.e(TAG, "error on delete transaction(s) with message: " + e.getMessage());
                } finally {
                    notifyOperationCallback(ACTION_DELETE, changes > 0, changes);
                }
            });
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///                                   Misc Methods                                           ///
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void hasAnyAccount(@NonNull ResultCallback<Boolean> callback) {
        AppExecutor.getDiskOperationsExecutor().execute(() -> {
            boolean hasAdequateAccounts = false;
            try {
                hasAdequateAccounts = getDao().countAccounts() > 0;
            }
            catch (Exception e) {
                Log.e(TAG, "Error occurred during countAccounts operation: "+e.getMessage());
            }
            finally {
                final boolean result = hasAdequateAccounts;
                AppExecutor.getMainThreadExecutor().execute(() -> {
                    callback.onResult(result);
                });
            }
        });
    }

    public LiveData<List<Account>> getAllAccountsNameAndId(){
        return accountNameIdLiveData;
    }

    public LiveData<List<Person>> getAllPersonNamAndId(){
        return personNameIdLiveDate;
    }

    public FilterTransactionParams getWorkingFilterParams() { return workingFilterParamLiveData.getValue(); }

    public LiveData<FilterTransactionParams> getWorkingFilterParamsLiveData() { return workingFilterParamLiveData; }

    public LiveData<Transaction> getWorkingTransactionLiveData() { return workingTransactionLiveData; }

    public void setWorkingTransaction(Transaction transaction) {
        final Transaction t = null == transaction ? new Transaction() : transaction.clone();
        if (OPERATION_SHOW_TRANSACTION_FOR_ACCOUNT == showOperationCode) {
            t.setAccountId(((Account) extraData).getAccountId());
        }
        else if (OPERATION_SHOW_TRANSACTION_FOR_PERSON == showOperationCode) {
            t.setPersonId(((Person) extraData).getPersonId());
        }
        workingTransactionLiveData.setValue(t);
    }

    public Transaction getWorkingTransaction() { return workingTransactionLiveData.getValue(); }

    /**
     *
     */
    public static class FilterTransactionParams {
        private static final String TAG = "FilterTransactionParams";

        public static final int DATE_ALL = 0;
        public static final int DATE_TODAY = 1;
        public static final int DATE_YESTERDAY = 2;
        public static final int DATE_THIS_WEEK = 3;
        public static final int DATE_LAST_WEEK = 4;
        public static final int DATE_THIS_MONTH = 5;
        public static final int DATE_LAST_MONTH = 6;
        public static final int DATE_SPECIFIC = 7;
        public static final int DATE_CUSTOM_RANGE = 8;

        boolean credit = true,debit = true;
        int dateType = DATE_ALL;
        Date minDate = null,maxDate = null;
        float  minAmount = Float.MIN_VALUE,maxAmount = Float.MAX_VALUE;
        List<Account> selectedAccounts;
        List<Person> selectedPeople;

        public FilterTransactionParams() {
            selectedAccounts = new ArrayList<>();
            selectedPeople = new ArrayList<>();
        }

        public boolean isCredit() { return credit; }

        public boolean isDebit() { return debit; }

        public int getDateType() { return dateType; }

        public Date getMinDate() { return minDate; }

        public Date getMaxDate() { return maxDate; }

        public List<Account> getSelectedAccounts() {
            return selectedAccounts;
        }

        public List<Person> getSelectedPeople() {
            return selectedPeople;
        }

        public String getMinAmountText() { return minAmount > Float.MIN_VALUE ? Helper.floatToString(minAmount) : null; }

        public String getMaxAmountText() { return maxAmount < Float.MAX_VALUE ? Helper.floatToString(maxAmount) : null; }

        public FilterTransactionParams setMaxAmount(Float maxAmount) {
            this.maxAmount = null == maxAmount ? Float.MAX_VALUE : maxAmount;
            return this;
        }

        public FilterTransactionParams setMinAmount(Float minAmount) {
            this.minAmount = null == minAmount ? Float.MIN_VALUE : minAmount;
            return this;
        }

        public void setCredit(boolean credit) {
            this.credit = credit;
        }

        public FilterTransactionParams setDebit(boolean debit) {
            this.debit = debit;
            return this;
        }

        public FilterTransactionParams setDateType(int dateType) {
            if (DEBUG) Log.d(TAG,"oldDateType="+this.dateType+" | newDateType="+dateType);
            this.dateType = dateType;
            return this;
        }

        public FilterTransactionParams setDateRange(int type, Date min, Date max) {
            setDateType(type).setMinDate(min).setMaxDate(max);
            return this;
        }

        public FilterTransactionParams setMinDate(Date minDate) {
            if (DEBUG) Log.d(TAG,"oldMinDate="+this.minDate+" | newMinDate="+minDate);
            this.minDate = minDate;
            return this;
        }

        public FilterTransactionParams setMaxDate(Date maxDate) {
            if (DEBUG) Log.d(TAG,"oldMaxDate="+this.maxDate+" | newMaxDate="+maxDate);
            this.maxDate = maxDate;
            return this;
        }

        public FilterTransactionParams addSelectedAccount(@NonNull Account account) {
            selectedAccounts.add(account);
            return this;
        }

        public FilterTransactionParams removeSelectedAccount(Account account) {
            selectedAccounts.remove(account);
            return this;
        }

        public FilterTransactionParams addSelectedPerson(Person person) {
            selectedPeople.add(person);
            return this;
        }

        public FilterTransactionParams removeSelectedPerson(Person person) {
            selectedPeople.remove(person);
            return this;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o instanceof FilterTransactionParams) {
                return equals((FilterTransactionParams) o);
            }
            return false;
        }

        public boolean equals(FilterTransactionParams o) {
            if (null != o) {
                return this.credit == o.credit
                        && this.debit == o.debit
                        && this.maxAmount == o.maxAmount
                        && this.minAmount == o.minAmount
                        && Check.isEquals(this.minDate, o.minDate)
                        && Check.isEquals(this.maxDate, o.maxDate)
                        && Check.isEquals(this.selectedAccounts, o.selectedAccounts)
                        && Check.isEquals(this.selectedPeople, o.selectedPeople);
            }
            return false;
        }

        @Override
        public String toString() {
            return "FilterTransactionParams{" +
                    "credit=" + credit +
                    ", debit=" + debit +
                    ", dateType=" + dateType +
                    ", minDate=" + minDate +
                    ", maxDate=" + maxDate +
                    ", minAmount=" + minAmount +
                    ", maxAmount=" + maxAmount +
                    ", selectedAccounts=" + selectedAccounts +
                    ", selectedPeople=" + selectedPeople +
                    '}';
        }

        @NonNull
        @Override
        protected FilterTransactionParams clone() {
            final FilterTransactionParams copy = new FilterTransactionParams();
            copy.credit = this.credit;
            copy.debit = this.debit;
            copy.maxAmount = this.maxAmount;
            copy.minAmount = this.minAmount;
            copy.minDate = null == this.minDate ? null : this.minDate.clone();
            copy.maxDate = null == this.maxDate ? null : this.maxDate.clone();
            copy.selectedAccounts = new ArrayList<>(this.selectedAccounts);
            copy.selectedPeople = new ArrayList<>(this.selectedPeople);
            return copy;
        }
    }
}
