package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.time.LocalDate;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;

@SuppressWarnings("unused")
public class TransactionHistoryViewModel extends DBViewModel {

    public static final int SAVE_HISTORY = 1;

    private LiveData<List<TransactionHistoryModel>> mTransactions;

    private LiveData<TransactionHistoryModel> mTransaction;

    public TransactionHistoryViewModel(@NonNull Application application) {
        super(application);
    }

    private TransactionHistoryDao getTransactionHistoryDao() {
        return getExpenseDatabase().getTransactionHistoryDao();
    }

    @NonNull
    public LiveData<List<TransactionHistoryModel>> getTransactionsForAccountBetweenDates(long id, @NonNull LocalDate start, @NonNull LocalDate end) {
        if (null == mTransactions) {
            mTransactions = getTransactionHistoryDao().getAllTransactionHistoriesForAccountsBetweenLive(id,start,end);
        }
        return mTransactions;
    }

    @NonNull
    public LiveData<List<TransactionHistoryModel>> getTransactionsForPeopleBetweenDates(long id, @NonNull LocalDate start, @NonNull LocalDate end) {
        if (null == mTransactions) {
            mTransactions = getTransactionHistoryDao().getAllTransactionHistoriesForPeopleBetweenLive(id,start,end);
        }
        return mTransactions;
    }

    @NonNull
    public LiveData<TransactionHistoryModel> getTransactionById(long id) {
        if (mTransaction == null) {
            mTransaction = getTransactionHistoryDao().getTransactionHistoryByIdLive(id);
        }
        return mTransaction;
    }
}
