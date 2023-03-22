package dreammaker.android.expensetracker.concurrent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.util.AppExecutor;

public abstract class Task implements Runnable {
    private static final WeakReference<TaskCallback> NO_CALLBACK = new WeakReference<>(null);

    public static final int CREATE = 0;

    public static final int RUNNING = 1;

    public static final int FINISH = 3;

    private final Object mLock = new Object();

    private int mState = CREATE;

    private TaskResult mResult = null;

    private List<WeakReference<TaskCallback>> mCallbackRefList
            = Collections.synchronizedList(new ArrayList<>());

    private void setState(int newState) {
        synchronized (mLock) {
            mState = newState;
        }
    }

    private void setResult(TaskResult result) {
        synchronized (mLock) {
            this.mResult = result;
        }
    }

    public void addCallback(@NonNull TaskCallback callback) {
        Objects.requireNonNull(callback,"callback == null");
        mCallbackRefList.add(new WeakReference<>(callback));
    }

    public void removeCallback(@NonNull TaskCallback callback) {
        Objects.requireNonNull(callback,"callback == null");
        mCallbackRefList.remove(callback);
    }

    public boolean isRunning() {
        int state;
        synchronized (mLock) {
            state = mState;
        }
        return state == RUNNING;
    }

    public boolean isFinished() {
        int state;
        synchronized (mLock) {
            state = mState;
        }
        return state == FINISH;
    }

    @Nullable
    public TaskResult getResult() {
        if (!isFinished()) return null;
        TaskResult result;
        synchronized (mLock) {
            result = mResult;
        }
        return result;
    }

    public final void notifyCallback() {
        final TaskResult result = getResult();
        for (WeakReference<TaskCallback> ref : mCallbackRefList) {
            AppExecutor.getMainThreadExecutor().execute(()->{
                TaskCallback callback = ref.get();
                if (null != callback) {
                    callback.onResult(result);
                }
            });
        }
    }

    @Override
    public void run() {
        setState(RUNNING);
        TaskResult result = execute();
        if (null == result) {
            throw new NullPointerException("execute() must result non null TaskResult");
        }
        setResult(result);
        setState(FINISH);
        onPostExecute(result);
        notifyCallback();
    }

    @NonNull
    protected abstract TaskResult execute();

    protected void onPostExecute(@NonNull TaskResult result){}
}
