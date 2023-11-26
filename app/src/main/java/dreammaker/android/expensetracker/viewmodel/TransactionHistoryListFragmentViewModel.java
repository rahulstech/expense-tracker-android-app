package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.time.LocalDate;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;

public class TransactionHistoryListFragmentViewModel extends AndroidViewModel {

    private ExpensesDatabase mDb;
    private TransactionHistoryDao mTransactionHistoryDao;

    private LiveData<List<TransactionHistoryModel>> mTransactionsForAccount;

    private LiveData<List<TransactionHistoryModel>> mTransactionsForPeople;

    public TransactionHistoryListFragmentViewModel(@NonNull Application application) {
        super(application);

        mDb = ExpensesDatabase.getInstance(application);
        mTransactionHistoryDao = mDb.getTransactionHistoryDao();
    }

    @NonNull
    public ExpensesDatabase getExpenseDatabase() {
        return mDb;
    }

    @NonNull
    public TransactionHistoryDao getTransactionHistoryDao() {
        return mTransactionHistoryDao;
    }

    @NonNull
    public LiveData<List<TransactionHistoryModel>> getTransactionsForAccountBetweenDates(long id, @NonNull LocalDate start, @NonNull LocalDate end) {
        if (null == mTransactionsForAccount) {
            mTransactionsForAccount = getTransactionHistoryDao().getAllTransactionHistoriesForAccountsBetweenLive(id,start,end);
        }
        return mTransactionsForAccount;
    }

    @NonNull
    public LiveData<List<TransactionHistoryModel>> getTransactionsForPeopleBetweenDates(long id, @NonNull LocalDate start, @NonNull LocalDate end) {
        if (null == mTransactionsForPeople) {
            mTransactionsForPeople = getTransactionHistoryDao().getAllTransactionHistoriesForPeopleBetweenLive(id,start,end);
        }
        return mTransactionsForPeople;
    }
}
