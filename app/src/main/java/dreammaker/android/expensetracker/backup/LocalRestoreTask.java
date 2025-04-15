package dreammaker.android.expensetracker.backup;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.Reader;

import androidx.annotation.NonNull;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.activity.RestoreActivity;
import dreammaker.android.expensetracker.activity.SettingsActivity;
import dreammaker.android.expensetracker.database.ExpensesBackupDao;
import dreammaker.android.expensetracker.database.ExpensesDao;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.ResultCallback;

import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.VERSION_6;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.createNewGSONInstance;

public class LocalRestoreTask implements Runnable {

    private static final String TAG = "LocalRestoreTask";
    private static final int LOCAL_RESTORE_NOTIFICATION_ID = 1849;

    private WorkActionService service;
    private Uri uri;
    private boolean isRetry;
    private ExpensesDao dao;
    private ExpensesBackupDao bkpDao;
    private ResultCallback<Boolean> callback;
    private NotificationHelper nh;

    public LocalRestoreTask(WorkActionService service, Uri uri, boolean isRetry, ResultCallback<Boolean> callback) {
        Check.isNonNull(service,"service == null");
        Check.isNonNull(uri,"uri == null");
        Check.isNonNull(callback, "callback == null");
        this.service = service;
        this.uri = uri;
        this.isRetry = isRetry;
        this.callback = callback;
        ExpensesDatabase db = ExpensesDatabase.getInstance(service.getApplicationContext());
        this.dao = db.getDao();
        this.bkpDao = db.getBackupDao();
        this.nh = new NotificationHelper(service.getApplicationContext());
        this.nh.createNotificationChannel(NotificationHelper.BACKUP_RESTORE_CHANNEL_ID,
                NotificationHelper.BACKUP_RESTORE_CHANNEL_DESCRIPTION);
    }

    public Uri getUri() {
        return uri;
    }

    @Override
    public void run() {
        notifyStart();
        try {
            // a restore requires the database no accounts and no people and no transactions;
            // if not then restore operation is terminated and user is asked to delete those
            // data manually and then perform restore
            restore(uri);
            notifyEnd();
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
            notifyError(e);
        }
    }

    private void restore(@NonNull Uri backupFile) throws Exception {
        try (Reader reader =  new InputStreamReader(service.getApplicationContext().getContentResolver()
                .openInputStream(backupFile))) {
            Gson gson = createNewGSONInstance();
            BackupData data = gson.fromJson(reader,BackupData.class);
            restoreAppData(data);
            restoreSettings(data);
        }
    }

    private void restoreAppData(BackupData data) {
        bkpDao.insertAccounts(data.getAccounts());
        bkpDao.insertPeople(data.getPeople());
        bkpDao.insertTransactions(data.getTransactions());
        if (!data.getTransactions().isEmpty() && data.getVersion() < VERSION_6) {
            bkpDao.setAccountsBalancesAndPeopleDues();
        }
        if (data.getVersion() > VERSION_6) {
            bkpDao.insertMoneyTransfers(data.getMoneyTransfers());
        }
    }

    private void restoreSettings(BackupData data) {
        if (data.getVersion() <= VERSION_6) return;
        BackupData.SettingsData settings = data.getSettings();
        BackupRestoreHelper.setBackupAutoScheduleDuration(service,settings.getBackupAutoScheduleDuration());
        SettingsActivity.setAutoDeleteDuration(service,settings.getAutoDeleteDuration());
    }

    private void notifyStart() {
        AppExecutor.getMainThreadExecutor().execute(() -> {
            WorkActionService service = LocalRestoreTask.this.service;
           NotificationHelper.NotificationArgs args =
                    new NotificationHelper.NotificationArgs(NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE)
                    .setSmallIcon(R.drawable.backup_restore)
                            .setNotificationTitle(service.getString(R.string.restore))
                    .setNotificationMessage(service.getString(R.string.notification_start_restore))
                   .setContentIntent(PendingIntent.getActivity(service,0,
                           new Intent(service, RestoreActivity.class)
                           .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
                           PendingIntent.FLAG_IMMUTABLE));
            showNotification(args,true);
        });
    }

    private void notifyEnd() {
        AppExecutor.getMainThreadExecutor().execute(() -> {
            WorkActionService service = LocalRestoreTask.this.service;
            NotificationHelper.NotificationArgs args =
                    new NotificationHelper.NotificationArgs(NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE)
                    .setSmallIcon(R.drawable.backup_restore)
                    .setNotificationTitle(service.getString(R.string.restore))
                            .setAutoCancel(true)
                    .setNotificationMessage(service.getString(R.string.notification_end_restore))
                            .setContentIntent(PendingIntent.getActivity(service.getApplicationContext(),0,
                                    new Intent(service.getApplicationContext(), MainActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),PendingIntent.FLAG_IMMUTABLE));
            showNotification(args,false);
            LocalRestoreTask.this.callback.onResult(true);
        });
    }

    private void notifyError(final Exception e) {
        try {
            dao.clearAll();
        }
        catch (Exception ex) {
            Log.e(TAG,"error during clear all: "+ex.getMessage());
        }
        AppExecutor.getMainThreadExecutor().execute(() -> {
            WorkActionService service = LocalRestoreTask.this.service;
            Uri data = LocalRestoreTask.this.uri;
            NotificationHelper nh = LocalRestoreTask.this.nh;
            boolean isRetry = LocalRestoreTask.this.isRetry;
            NotificationHelper.NotificationArgs args =
                    new NotificationHelper.NotificationArgs(NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE)
                            .setSmallIcon(R.drawable.backup_restore)
                            .setNotificationTitle(service.getString(R.string.restore))
                            .setNotificationMessage(service.getString(R.string.notification_error_restore))
                            .setAutoCancel(true);
            if (!isRetry) {
                PendingIntent pi = PendingIntent.getService(service,0,
                        new Intent(service.getApplicationContext(),WorkActionService.class)
                                .setAction(WorkActionService.ACTION_LOCAL_RESTORE_START).setData(data),PendingIntent.FLAG_IMMUTABLE);
                args.addAction(nh.createAction(R.string.retry,pi));
            }
            showNotification(args,false);
            LocalRestoreTask.this.callback.onResult(false);
        });

    }

    private void showNotification(@NonNull NotificationHelper.NotificationArgs args, boolean sticky) {
        Notification n = nh.newNotification(NotificationHelper.BACKUP_RESTORE_CHANNEL_ID,args);
        if (sticky)
            service.startForeground(LOCAL_RESTORE_NOTIFICATION_ID,n);
        else {
            service.stopForeground(false);
            nh.updateNotification(LOCAL_RESTORE_NOTIFICATION_ID,n);
        }
    }
}
