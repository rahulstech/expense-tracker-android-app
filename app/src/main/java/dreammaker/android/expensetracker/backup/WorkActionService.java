package dreammaker.android.expensetracker.backup;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.BackupRestoreActivity;
import dreammaker.android.expensetracker.util.AppExecutor;

import static dreammaker.android.expensetracker.activity.SettingsActivity.setNextAutoDeleteDate;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.BACKUP_NOTIFICATION_ID;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.BACKUP_WORK_TAG;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_BACKUP_FILE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_RETRY;
import static dreammaker.android.expensetracker.backup.NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE;

public class WorkActionService extends Service {

    private static final String TAG = "WorkActionService";

    public static final String ACTION_LOCAL_BACKUP_START = "action_local_backup_start";
    public static final String ACTION_RETRY_BACKUP = "action_retry_backup";
    public static final String ACTION_CANCEL_BACKUP = "action_cancel_backup";
    public static final String ACTION_LOCAL_RESTORE_START = "action_local_restore_start";
    public static final String ACTION_RETRY_RESTORE = "action_retry_restore";
    public static final String ACTION_CLEAR_APP_DATA = "action_clear_app_data";
    public static final String ACTION_AUTO_DELETE_START = "action_auto_delete_start";

    public static final String EXTRA_WORK_ID = "work_id";

    private NotificationHelper notificationHelper;
    private WorkManager workManager;
    private HashMap<UUID,Values> workIdValueMap = new HashMap<>();

    private ClearAppDataTask clearAppDataTask = null;
    private LocalRestoreTask localRestoreTask = null;
    private AutoDeleteWork autoDeleteWork = null;

    @Override
    public void onCreate() {
        super.onCreate();
        workManager = WorkManager.getInstance(this);
        notificationHelper = new NotificationHelper(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        if (!workIdValueMap.isEmpty()) {
            for (UUID workId : workIdValueMap.keySet()) {
                removeEntry(workId);
            }
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            String action = intent.getAction();
            if (ACTION_LOCAL_BACKUP_START.equals(action)) {
                if (null != intent)
                    onLocalBackupStart((UUID) intent.getSerializableExtra(EXTRA_WORK_ID));
            }
            else if (ACTION_CANCEL_BACKUP.equals(action)) {
                onCancelBackup();
            }
            else if (ACTION_RETRY_BACKUP.equals(action)) {
                onRetryBackup();
            }
            else if (ACTION_LOCAL_RESTORE_START.equals(action)) {
                if (null != intent)
                    onStartLocalRestore(intent.getData(),false);
            }
            else if (ACTION_RETRY_RESTORE.equals(action)) {
                if (null != intent)
                    onStartLocalRestore(intent.getData(),true);
            }
            else if (ACTION_CLEAR_APP_DATA.equals(action)) {
                onClearAll();
            }
            else if (ACTION_AUTO_DELETE_START.equals(action)) {
                onStartAutoDelete();
            }
        }
        return START_NOT_STICKY;
    }

    private void onStartLocalRestore(@NonNull Uri data, boolean isRetry) {
        if (null == this.localRestoreTask) {
            LocalRestoreTask task = new LocalRestoreTask(this, data, isRetry, success ->
                    WorkActionService.this.localRestoreTask = null);
            AppExecutor.getDiskOperationsExecutor().submit(task);
            this.localRestoreTask = task;
        }
    }

    private void onClearAll() {
        if (null == clearAppDataTask) {
            ClearAppDataTask task = new ClearAppDataTask(this, success -> WorkActionService.this.clearAppDataTask = null);
            AppExecutor.getDiskOperationsExecutor().submit(task);
            this.clearAppDataTask = task;
        }
    }

    private void onStartAutoDelete() {
        if (null != autoDeleteWork) return;
        AutoDeleteWork task = new AutoDeleteWork(this, successful -> {
            setNextAutoDeleteDate(this);
            this.autoDeleteWork = null;
        });
        AppExecutor.getDiskOperationsExecutor().execute(task);
        autoDeleteWork = task;
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    ///                                  Backup                                          ///
    ///////////////////////////////////////////////////////////////////////////////////////

    private PendingIntent createNotificationContentIntentForBackup() {
        return PendingIntent.getActivity(this,0,
                new Intent(this, BackupRestoreActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                ,0);
    }

    private Notification newNotificationForLocalBackup(String message, NotificationCompat.Action action, boolean autoCancel) {
        NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_SIMPLE)
                .setSmallIcon(R.drawable.backup_restore)
                .setContentIntent(createNotificationContentIntentForBackup())
                .setNotificationTitle(getString( R.string.notification_title_backup))
                .setNotificationMessage(message)
                .setAutoCancel(autoCancel);
        if (null != action) args.addAction(action);
        return notificationHelper.newNotification(NotificationHelper.BACKUP_RESTORE_CHANNEL_ID,args);
    }

    private void onCancelBackup() {
        workManager.cancelUniqueWork(BACKUP_WORK_TAG);
    }

    private void onLocalBackupStart(@NonNull UUID workId) {
        Log.d(TAG, "onLocalBackupStart(workId: "+workId+")");
        NotificationCompat.Action cancelAction = notificationHelper.createAction(R.string.cancel,
                PendingIntent.getService(this,0,
                        new Intent(this,WorkActionService.class)
                                .setAction(WorkActionService.ACTION_CANCEL_BACKUP),
                        0));
        notificationHelper.createNotificationChannel(NotificationHelper.BACKUP_RESTORE_CHANNEL_ID,
                NotificationHelper.BACKUP_RESTORE_CHANNEL_DESCRIPTION);
        startForeground(BACKUP_NOTIFICATION_ID,
                newNotificationForLocalBackup(getString(R.string.notification_start_local_backup),
                        cancelAction, false));
        Values values = new Values();
        workIdValueMap.put(workId,values);
        values.workInfoLiveData = workManager.getWorkInfoByIdLiveData(workId);
        values.workInfoObserver = info -> {
            if (null != info) onLocalBackupStateChange(info);
        };
        values.workInfoLiveData.observeForever(values.workInfoObserver);
    }

    private void onLocalBackupStateChange(@NonNull WorkInfo info) {
        WorkInfo.State state = info.getState();

        if (state.isFinished()) stopForeground(false);

        if (WorkInfo.State.SUCCEEDED == state) {
            Data data = info.getOutputData();
            String backupFile = data.getString(KEY_BACKUP_FILE);
            notificationHelper.updateNotification(BACKUP_NOTIFICATION_ID,
                    newNotificationForLocalBackup(getResources().getString(R.string.notification_end_local_backup, backupFile),
                            null,true));
            onLocalBackupSuccessful(new File(backupFile));
        }
        else if (WorkInfo.State.FAILED == state) {
            Data output = info.getOutputData();
            boolean isRetry = output.getBoolean(KEY_RETRY,false);
            NotificationCompat.Action retryAction = notificationHelper.createAction(R.string.retry,
                    PendingIntent.getService(this,0,
                            new Intent(this,WorkActionService.class)
                                    .setAction(ACTION_RETRY_BACKUP),0));
            notificationHelper.updateNotification(BACKUP_NOTIFICATION_ID,
                    newNotificationForLocalBackup(getString(R.string.notification_end_local_backup),
                    isRetry ? null : retryAction,true));
        }
        else if (WorkInfo.State.CANCELLED == state) {
            notificationHelper.removeNotification(BACKUP_NOTIFICATION_ID);
        }
        if (state.isFinished()) {
            removeEntry(info.getId());
        }
    }

    private void onRetryBackup() {
        BackupRestoreHelper.backupNow(this);
    }

    private void onLocalBackupSuccessful(@NonNull File file) {
        BackupRestoreHelper.onBackupSuccessful(getApplicationContext());
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    ///                               Miscellaneous                                      ///
    ///////////////////////////////////////////////////////////////////////////////////////

    private void removeEntry(@NonNull UUID workId) {
        Values value = workIdValueMap.get(workId);
        value.workInfoLiveData.removeObserver(value.workInfoObserver);
        workIdValueMap.remove(workId);
    }

    private static class Values {
        LiveData<WorkInfo> workInfoLiveData;
        Observer<WorkInfo> workInfoObserver;
    }
}
