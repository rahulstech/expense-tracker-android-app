package dreammaker.android.expensetracker.backup;

import android.util.Log;

import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.SettingsActivity;
import dreammaker.android.expensetracker.database.ExpensesDao;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.database.Date;
import dreammaker.android.expensetracker.util.ResultCallback;

public class AutoDeleteWork implements Runnable {

    public static final String TAG = "AutoDeleteWork";

    private static final int AUTO_DELETE_NOTIFICATION_ID = 854;

    private WorkActionService service;
    private ResultCallback<Boolean> callback;
    private NotificationHelper nh;
    private ExpensesDao dao;

    public AutoDeleteWork(WorkActionService service, ResultCallback<Boolean> callback) {
        Check.isNonNull(service,"service == null");
        Check.isNonNull(callback,"callback == null");
        this.service = service;
        this.callback = callback;
        this.dao = ExpensesDatabase.getInstance(service.getApplicationContext())
                .getDao();
        this.nh = new NotificationHelper(service);
        this.nh.createNotificationChannel(NotificationHelper.MISCELLANEOUS_CHANNEL_ID,
                NotificationHelper.MISCELLANEOUS_CHANNEL_DESCRIPTION);
    }

    @Override
    public void run() {
        notifyStart();
        try {
            delete();
        }
        catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
        finally {
            notifyEnd();
        }
    }

    private void delete() {
        String autoDeleteDuration = SettingsActivity.getAutoDeleteDuration(service);
        Date now = new Date();
        Date date;
        switch (autoDeleteDuration) {
            case "one_month": {
                date = now.firstDateOfNPreviousMonths(1);
            }
            break;
            case "three_month": {
                date = now.firstDateOfNPreviousMonths(3);
            }
            break;
            case "six_month": {
                date = now.firstDateOfNPreviousMonths(6);
            }
            break;
            case "one_year": {
                date = now.firstDateOfNPreviousMonths(12);
            }
            break;
            default: {
                // this must never reach
                throw new IllegalArgumentException("unknown auto delete duration: '"+autoDeleteDuration+"'");
            }
        }
        dao.markTransactionDeletedOlderThan(date);
        dao.deleteMoneyTransfersOlderThan(date);
    }

    private void notifyStart() {
        AppExecutor.getMainThreadExecutor().execute(() -> {
            NotificationHelper.NotificationArgs args =
                    new NotificationHelper.NotificationArgs(NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE)
                            .setSmallIcon(R.drawable.ic_baseline_auto_delete)
                            .setNotificationTitle(service.getString(R.string.title_auto_delete))
                            .setNotificationMessage(service.getString(R.string.notification_start_auto_delete));
            service.startForeground(AUTO_DELETE_NOTIFICATION_ID,
                    nh.newNotification(NotificationHelper.MISCELLANEOUS_CHANNEL_ID,args));
        });
    }

    private void notifyEnd() {
        AppExecutor.getMainThreadExecutor().execute(() -> {
            NotificationHelper.NotificationArgs args =
                    new NotificationHelper.NotificationArgs(NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE)
                            .setSmallIcon(R.drawable.ic_baseline_auto_delete)
                            .setNotificationTitle(service.getString(R.string.title_auto_delete))
                            .setNotificationMessage(service.getString(R.string.notification_end_auto_delete))
                            .setAutoCancel(true);
            AutoDeleteWork.this.callback.onResult(true);
            service.stopForeground(false);
            service.startForeground(AUTO_DELETE_NOTIFICATION_ID,
                    nh.newNotification(NotificationHelper.MISCELLANEOUS_CHANNEL_ID,args));

        });
    }
}
