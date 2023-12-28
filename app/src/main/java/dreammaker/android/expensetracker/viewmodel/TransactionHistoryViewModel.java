package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;
import android.util.Log;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;

@SuppressWarnings("unused")
public class TransactionHistoryViewModel extends DBViewModel {

    private static final String TAG = TransactionHistoryViewModel.class.getSimpleName();

    public static final int SAVE_HISTORY = 1;

    public static final int DELETE_HISTORIES = 2;

    private final LiveData<List<TransactionHistoryModel>> mTransactions;

    private LiveData<TransactionHistoryModel> mTransaction;

    private final MutableLiveData<SourceData> mSourceDataLiveData;

    public TransactionHistoryViewModel(@NonNull Application application) {
        super(application);
        mSourceDataLiveData = new MutableLiveData<>();
        mTransactions = Transformations.switchMap(mSourceDataLiveData,(source)->{
          int entity = source.entity;
          LocalDate start = source.start;
          LocalDate end = source.end;
          long id = source.id;
          if (entity == SourceData.ENTITY_ACCOUNT) {
              return getTransactionHistoryDao().getAllTransactionHistoriesForAccountsBetweenLive(id,end,start);
          }
          else if (entity == SourceData.ENTITY_PERSON) {
              return getTransactionHistoryDao().getAllTransactionHistoriesForPeopleBetweenLive(id,end,start);
          }
          else {
              return getTransactionHistoryDao().getAllTransactionHistoriesBetweenLive(end,start);
          }
        });
    }

    private TransactionHistoryDao getTransactionHistoryDao() {
        return getExpenseDatabase().getTransactionHistoryDao();
    }

    @NonNull
    public LiveData<List<TransactionHistoryModel>> getTransactionsBetweenDates(@NonNull LocalDate start, @NonNull LocalDate end) {
        SourceData source = new SourceData();
        source.start = start;
        source.end = end;
        source.entity = SourceData.ENTITY_ALL;
        mSourceDataLiveData.postValue(source);
        return mTransactions;
    }

    @NonNull
    public LiveData<List<TransactionHistoryModel>> getTransactionsForAccountBetweenDates(long id, @NonNull LocalDate start, @NonNull LocalDate end) {
        SourceData source = new SourceData();
        source.start = start;
        source.end = end;
        source.id = id;
        source.entity = SourceData.ENTITY_ACCOUNT;
        mSourceDataLiveData.postValue(source);
        return mTransactions;
    }

    @NonNull
    public LiveData<List<TransactionHistoryModel>> getTransactionsForPersonBetweenDates(long id, @NonNull LocalDate start, @NonNull LocalDate end) {
        SourceData source = new SourceData();
        source.start = start;
        source.end = end;
        source.id = id;
        source.entity = SourceData.ENTITY_PERSON;
        mSourceDataLiveData.postValue(source);
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

    private static class SourceData {

        public static final int ENTITY_ALL = 0;

        public static final int ENTITY_ACCOUNT = 1;

        public static final int ENTITY_PERSON = 2;

        public LocalDate start;

        public LocalDate end;

        public long id;

        public int entity = ENTITY_ALL;

        public SourceData() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SourceData)) return false;
            SourceData that = (SourceData) o;
            return id == that.id && entity == that.entity && Objects.equals(start, that.start) && Objects.equals(end, that.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end, id, entity);
        }
    }
}
