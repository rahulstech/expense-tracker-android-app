package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;
import android.util.Log;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;

@SuppressWarnings("unused")
public class TransactionHistoryViewModel extends DBViewModel {

    private static final String TAG = TransactionHistoryViewModel.class.getSimpleName();

    public static final int SAVE_HISTORY = 1;

    public static final int DELETE_HISTORIES = 2;

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
    public LiveData<List<TransactionHistoryModel>> getTransactionsForPersonBetweenDates(long id, @NonNull LocalDate start, @NonNull LocalDate end) {
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

    @NonNull
    public LiveData<AsyncQueryResult> saveTransactionHistory(TransactionHistory history) {
        return execute(SAVE_HISTORY,()->{
            if (BuildConfig.DEBUG) {
                Log.d(TAG,"saveTransactionHistory: history="+history);
            }
            if (history.getId() > 0) {
                if (getTransactionHistoryDao().updateTransactionHistory(history) != 1) {
                    return null;
                }
            }
            else {
                long id = getTransactionHistoryDao().addTransactionHistory(history);
                if (id <= 0) {
                    return null;
                }
                history.setId(id);
            }
            return history;
        });
    }

    @NonNull
    public LiveData<AsyncQueryResult> removeTransactionHistories(long[] ids) {
        Objects.requireNonNull(ids,"array of history ids is null");
        return execute(DELETE_HISTORIES,()->{
            if (ids.length == 0) {
                return true;
            }
            int count = getTransactionHistoryDao().removeTransactionHistories(ids);
            return ids.length == count;
        });
    }
}
