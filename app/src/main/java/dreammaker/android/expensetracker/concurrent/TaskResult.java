package dreammaker.android.expensetracker.concurrent;

public class TaskResult {
    public int taskCode;
    public Object parameter;
    public Object result;
    public Throwable error;
    public int errorCode;
    public boolean successful;

    public TaskResult(int taskCode) {
        this.taskCode = taskCode;
    }
}
