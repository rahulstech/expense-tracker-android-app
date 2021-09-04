package dreammaker.android.expensetracker.backup;

import android.app.Notification;
import android.util.Log;

import androidx.annotation.NonNull;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.ExpensesDao;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.ResultCallback;

public class ClearAppDataTask implements Runnable {

    private static final String TAG = "ClearAppDataTask";
    private static final int CLEAR_TASK_NOTIFICATION_ID = 656;

    private WorkActionService service;
    private ExpensesDao dao;
    private NotificationHelper nh;
    private ResultCallback<Boolean> callback;

    public ClearAppDataTask(@NonNull WorkActionService service, @NonNull ResultCallback<Boolean> callback) {
        Check.isNonNull(service,"service == null");
        Check.isNonNull(callback,"callback == null");
        this.service = service;
        this.callback = callback;
        this.dao = ExpensesDatabase.getInstance(service.getApplicationContext()).getDao();
        this.nh = new NotificationHelper(service);
        nh.createNotificationChannel(NotificationHelper.MISCELLANEOUS_CHANNEL_ID,
                NotificationHelper.MISCELLANEOUS_CHANNEL_DESCRIPTION);

    }

    @Override
    public void run() {
        notifyStart();
        try {
            dao.clearAll();
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
            notifyError(e);
        }
        finally {
            notifyEnd();
        }
    }

    private void notifyStart() {
        AppExecutor.getMainThreadExecutor().execute(() -> {
            NotificationHelper.NotificationArgs args =
                    new NotificationHelper.NotificationArgs(NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE)
                            .setSmallIcon(R.drawable.ic_baseline_clear_all)
                            .setNotificationTitle(service.getString(R.string.title_clear_all))
                            .setNotificationMessage(service.getString(R.string.notification_start_clear_all));
            showNotification(args,true);
        });
    }

    private void notifyError(Exception e) {
        AppExecutor.getMainThreadExecutor().execute(() -> {
            NotificationHelper.NotificationArgs args =
                    new NotificationHelper.NotificationArgs(NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE)
                            .setSmallIcon(R.drawable.ic_baseline_clear_all)
                            .setNotificationTitle(service.getString(R.string.title_clear_all))
                            .setNotificationMessage(service.getString(R.string.notification_error_clear_all))
                            .setAutoCancel(true);
            showNotification(args,false);
            ClearAppDataTask.this.callback.onResult(false);
        });
    }

    private void notifyEnd() {
        AppExecutor.getMainThreadExecutor().execute(() -> {
            NotificationHelper.NotificationArgs args =
                    new NotificationHelper.NotificationArgs(NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE)
                            .setSmallIcon(R.drawable.ic_baseline_clear_all)
                            .setNotificationTitle(service.getString(R.string.title_clear_all))
                            .setNotificationMessage(service.getString(R.string.notification_end_clear_all))
                            .setAutoCancel(true);
            showNotification(args,false);
            ClearAppDataTask.this.callback.onResult(true);
        });
    }

    private void showNotification(@NonNull NotificationHelper.NotificationArgs args, boolean sticky) {
        Notification n = nh.newNotification(NotificationHelper.MISCELLANEOUS_CHANNEL_ID,args);
        if (sticky)
            service.startForeground(CLEAR_TASK_NOTIFICATION_ID,n);
        else {
            service.stopForeground(false);
            nh.updateNotification(CLEAR_TASK_NOTIFICATION_ID,n);
        }
    }
}
