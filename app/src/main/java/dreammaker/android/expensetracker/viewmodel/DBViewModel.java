package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import dreammaker.android.expensetracker.database.ExpensesDatabase;

@SuppressWarnings("unused")
public class DBViewModel extends AndroidViewModel {

    @SuppressWarnings("FieldMayBeFinal")
    private Map<Integer,AsyncQueryTask> mQueryTasksMap;

    private ExpensesDatabase mExpenseDb;

    public DBViewModel(@NonNull Application application) {
        super(application);
        mQueryTasksMap = new HashMap<>();
    }

    @NonNull
    public ExpensesDatabase getExpenseDatabase() {
        if (null == mExpenseDb) {
            mExpenseDb = ExpensesDatabase.getInstance(getApplication());
        }
        return mExpenseDb;
    }

    @NonNull
    public LiveData<AsyncQueryResult> getOrCreate(int code, Callable<Object> callback) {
        AsyncQueryTask task = getQueryTask(code);
        if (null != task) {
            return task.getLiveResult();
        }
        return execute(code,callback);
    }

    @NonNull
    public final LiveData<AsyncQueryResult> execute(int code, Callable<Object> callback) {
        Objects.requireNonNull(callback,"callback == null");
        AsyncQueryTask task = new AsyncQueryTask(code,callback);
        mQueryTasksMap.put(task.getOperationCode(),task);
        task.execute();
        return task.getLiveResult();
    }

    @Nullable
    public final AsyncQueryTask getQueryTask(int code) {
        return mQueryTasksMap.get(code);
    }

    @Nullable
    public final LiveData<AsyncQueryResult> getLiveResult(int code) {
        AsyncQueryTask task = getQueryTask(code);
        if (null == task) return null;
        return task.getLiveResult();
    }

    @Nullable
    public final AsyncQueryTask removeQueryTask(int code, boolean cancel) {
        AsyncQueryTask task = mQueryTasksMap.remove(code);
        if (task != null && !task.isFinished() && cancel) {
            try {
                task.cancel(true);
            }
            catch (Throwable ignore) {}
        }
        return task;
    }

    public static class AsyncQueryResult {

        private final int operationCode;

        @Nullable
        private Object result;

        @Nullable
        private Throwable error;

        private boolean canceled;

        public AsyncQueryResult(int operationCode) {
            this.operationCode = operationCode;
        }

        public int getOperationCode() {
            return operationCode;
        }

        public void setResult(@Nullable Object result) {
            this.result = result;
        }

        @Nullable
        public Object getResult() {
            return result;
        }

        public void setError(@Nullable Throwable error) {
            this.error = error;
        }

        @Nullable
        public Throwable getError() {
            return error;
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        public boolean isCanceled() {
            return canceled;
        }
    }

    public static final class AsyncQueryTask extends AsyncTask<Void,Void,AsyncQueryResult> {

        private final int mOperationCode;
        private final Callable<Object> mQueryCallback;

        private final MutableLiveData<AsyncQueryResult> mResult;

        public AsyncQueryTask(int operationCode, @NonNull Callable<Object> callback) {
            super();
            mOperationCode = operationCode;
            mQueryCallback = callback;
            mResult = new MutableLiveData<>();
        }

        public int getOperationCode() {
            return mOperationCode;
        }

        @Override
        protected AsyncQueryResult doInBackground(Void... voids) {
            AsyncQueryResult result = new AsyncQueryResult(mOperationCode);
            try {
                Object output = mQueryCallback.call();
                result.setResult(output);
            }
            catch (Throwable th) {
                result.setError(th);
            }
            return result;
        }

        @Override
        protected void onPostExecute(@NonNull AsyncQueryResult result) {
            mResult.postValue(result);
        }

        @Override
        protected void onCancelled(@NonNull AsyncQueryResult result) {
            super.onCancelled(result);
            result.setCanceled(true);
            mResult.postValue(result);
        }

        @NonNull
        public LiveData<AsyncQueryResult> getLiveResult() {
            return mResult;
        }

        public boolean isFinished() {
            return Status.FINISHED == getStatus();
        }
    }
}
