package dreammaker.android.expensetracker.concurrent;

import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.util.AppExecutor;

public class TaskMaster {

    private HashMap<String,Task> mTasks = new HashMap<>();

    private HashMap<String,TaskResult> mResults = new HashMap<>();

    public boolean hasTask(String key) {
        return mTasks.containsKey(key);
    }

    @Nullable
    public TaskResult getResult(String key) {
        return mResults.get(key);
    }

    public void removeResult(String key) {
        mResults.remove(key);
    }

    @Nullable
    private Task removeTask(String key) {
        Task task = mTasks.remove(key);
        return task;
    }

    public void addTaskCallback(String key, @NonNull TaskCallback callback) throws IllegalStateException {
        Task task = mTasks.get(key);
        if (null == task) {
            throw new IllegalStateException("no task with key \""+key+"\" exists " +
                    "- either the task already finished or it is not created");
        }
        task.addCallback(callback);
    }

    public void execute(@NonNull String key, @NonNull Task task) throws IllegalStateException {
        Objects.requireNonNull(key,"key == null");
        Objects.requireNonNull(task,"task == null");
        if (mTasks.containsKey(key)) {
            throw new IllegalStateException("another task with key \""+key+"\" exists");
        }
        task.addCallback(result -> {
            removeTask(key);
            mResults.put(key,result);
        });
        mTasks.put(key,task);
        AppExecutor.getDiskOperationsExecutor().execute(task);
    }
}
