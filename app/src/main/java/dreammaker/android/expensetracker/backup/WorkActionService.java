package dreammaker.android.expensetracker.backup;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;

import java.util.HashMap;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.BackupRestoreActivity;
import dreammaker.android.expensetracker.activity.MainActivity;

import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.BACKUP_NOTIFICATION_ID;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.BACKUP_WORK_TAG;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_FILE_ACCESS;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_NON_EMPTY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_NO_DATA;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_BACKUP_FILE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_FAILURE_CODE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_MESSAGE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PROGRESS_CURRENT;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PROGRESS_MAX;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.RESTORE_NOTIFICATION_ID;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.RESTORE_WORK_TAG;
import static dreammaker.android.expensetracker.backup.NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_DETERMINATE_PROGRESS;
import static dreammaker.android.expensetracker.backup.NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_INDETERMINATE_PROGRESS;
import static dreammaker.android.expensetracker.backup.NotificationHelper.NotificationArgs.NOTIFICATION_TYPE_SIMPLE;

public class WorkActionService extends Service {

    public static final String ACTION_LOCAL_BACKUP_START = "action_local_backup_start";
    public static final String ACTION_LOCAL_RESTORE_START = "action_local_restore_start";
    public static final String ACTION_RETRY_BACKUP = "action_retry_backup";
    public static final String ACTION_RETRY_RESTORE = "action_retry_restore";
    public static final String ACTION_CANCEL_BACKUP = "action_cancel_backup";
    public static final String ACTION_CANCEL_RESTORE = "action_cancel_restore";

    public static final String EXTRA_WORK_ID = "work_id";

    private NotificationHelper notificationHelper;
    private WorkManager workManager;
    private NotificationManagerCompat notificationManager;
    private HashMap<UUID,Values> workIdValueMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        workManager = WorkManager.getInstance(this);
        notificationHelper = new NotificationHelper(getApplicationContext());
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
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
                if (null != intent) onLocalBackupStart((UUID) intent.getSerializableExtra(EXTRA_WORK_ID));
            }
            else if (ACTION_LOCAL_RESTORE_START.equals(action)) {
                if (null != intent)
                    onLocalRestoreStart(
                            (UUID) intent.getSerializableExtra(EXTRA_WORK_ID),
                            intent.getParcelableExtra(KEY_BACKUP_FILE));
            }
            else if (ACTION_RETRY_BACKUP.equals(action)) {
                onRetryBackup();
            }
            else if (ACTION_CANCEL_BACKUP.equals(action)) {
                onCancelBackup();
            }
            else if (ACTION_CANCEL_RESTORE.equals(action)) {
                onCancelRestore();
            }
        }
        return START_NOT_STICKY;
    }

    private void onCancelRestore() {
        workManager.cancelUniqueWork(RESTORE_WORK_TAG);
    }

    private void onCancelBackup() {
        workManager.cancelUniqueWork(BACKUP_WORK_TAG);
    }

    private void onLocalBackupStart(@NonNull UUID workId) {
        startForeground(BACKUP_NOTIFICATION_ID,createNotificationForLocalBackupStart(workId));
        Values values = new Values();
        workIdValueMap.put(workId,values);
        values.workInfoLiveData = workManager.getWorkInfoByIdLiveData(workId);
        values.workInfoObserver = info -> {
            if (null != info) onLocalBackupStateChange(info);
        };
        values.workInfoLiveData.observeForever(values.workInfoObserver);
    }

    private Notification createNotificationForLocalBackupStart(@NonNull UUID workId) {
        Intent intent = new Intent(this, BackupRestoreActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,
                intent,0);
        NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_INDETERMINATE_PROGRESS)
                .addAction(notificationHelper.createAction(R.string.cancel,
                        PendingIntent.getService(this,0,
                                new Intent(this,WorkActionService.class)
                                        .setAction(ACTION_CANCEL_BACKUP),0)))
                .setContentIntent(pi)
                .setNotificationTitle(getString( R.string.notification_title_backup))
                .setNotificationMessage(getString(R.string.message_local_backup_start));
        return notificationHelper.newNotification(args);
    }

    private void onLocalBackupStateChange(@NonNull WorkInfo info) {
        WorkInfo.State state = info.getState();
        if (WorkInfo.State.RUNNING == state) {
            Data progress = info.getProgress();
            int max = progress.getInt(KEY_PROGRESS_MAX,100);
            int current = progress.getInt(KEY_PROGRESS_CURRENT,0);
            String message = progress.getString(KEY_MESSAGE);

            Intent intent = new Intent(getApplicationContext(), BackupRestoreActivity.class);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,
                    intent,0);
            NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_DETERMINATE_PROGRESS)
                    .addAction(notificationHelper.createAction(R.string.cancel,workManager.createCancelPendingIntent(info.getId())))
                    .setProgressMax(max)
                    .setProgressCurrent(current)
                    .setContentIntent(pi)
                    .setNotificationTitle(getString( R.string.notification_title_backup))
                    .setNotificationMessage(message);
            notificationManager.notify(BACKUP_NOTIFICATION_ID,notificationHelper.newNotification(args));
        }

        if (state.isFinished()) stopForeground(false);

        if (WorkInfo.State.SUCCEEDED == state) {
            Intent intent = new Intent(getApplicationContext(), BackupRestoreActivity.class);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,
                    intent,0);
            NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_SIMPLE)
                    .setContentIntent(pi)
                    .setNotificationTitle(getString( R.string.notification_title_backup))
                    .setNotificationMessage(getString(R.string.message_local_backup_successful));
            notificationManager.notify(BACKUP_NOTIFICATION_ID,notificationHelper.newNotification(args));
        }
        else if (WorkInfo.State.FAILED == state) {
            Data output = info.getOutputData();
            int failureCode = output.getInt(KEY_FAILURE_CODE,-1);

            NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_SIMPLE)
                    .setNotificationTitle(getString( R.string.notification_title_backup))
                    .setAutoCancel(true);
            if (FAIL_FILE_ACCESS == failureCode) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", getPackageName(), null))
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
                args.setContentIntent(pi);
                args.setNotificationMessage(getString(R.string.message_error_file_access));
            }
            else if (FAIL_NO_DATA == failureCode) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,
                        intent,0);
                args.setContentIntent(pi);
                args.setNotificationMessage(getString(R.string.message_fail_no_data));
            }
            else {
                Intent intent = new Intent(getApplicationContext(), BackupRestoreActivity.class);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,
                        intent,0);
                args.setContentIntent(pi);
                args.addAction(notificationHelper.createAction(R.string.retry,
                        PendingIntent.getService(this,0,
                                new Intent(this,WorkActionService.class).setAction(ACTION_RETRY_BACKUP),0)));
                args.setNotificationMessage(getString(R.string.message_backup_fail_unknown));
            }
            notificationManager.notify(BACKUP_NOTIFICATION_ID,notificationHelper.newNotification(args));
        }
        else if (WorkInfo.State.CANCELLED == state) {
            Intent intent = new Intent(getApplicationContext(), BackupRestoreActivity.class);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,
                    intent,0);
            NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_SIMPLE)
                    .setContentIntent(pi)
                    .setNotificationTitle(getString( R.string.notification_title_backup))
                    .setNotificationMessage(getString(R.string.message_backup_canceled))
                    .setAutoCancel(true);
            notificationManager.notify(BACKUP_NOTIFICATION_ID,notificationHelper.newNotification(args));
        }
        if (state.isFinished()) {
            removeEntry(info.getId());
            BackupRestoreHelper.backupNext(this);
        }
    }

    private void onLocalRestoreStart(@NonNull UUID workId, @NonNull Uri backupFile) {
        startForeground(RESTORE_NOTIFICATION_ID,createNotificationForLocalRestoreStart(workId));
        Values values = new Values();
        workIdValueMap.put(workId,values);
        values.workInfoLiveData = workManager.getWorkInfoByIdLiveData(workId);
        values.workInfoObserver = info -> {
            if (null != info) onLocalRestoreStateChange(info, backupFile);
        };
        values.workInfoLiveData.observeForever(values.workInfoObserver);
    }

    private Notification createNotificationForLocalRestoreStart(@NonNull UUID workId) {
        Intent intent = new Intent(this, BackupRestoreActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,
                intent,0);
        NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_INDETERMINATE_PROGRESS)
                .addAction(notificationHelper.createAction(R.string.cancel,PendingIntent.getService(this,0,
                        new Intent(this,WorkActionService.class)
                                .setAction(ACTION_CANCEL_RESTORE),
                        0)))
                .setContentIntent(pi)
                .setNotificationTitle(getString( R.string.notification_title_restore))
                .setNotificationMessage(getString(R.string.message_local_restore_start));
        return notificationHelper.newNotification(args);
    }

    private void onLocalRestoreStateChange(@NonNull WorkInfo info, @NonNull Uri backupFile) {
        WorkInfo.State state = info.getState();
        if (WorkInfo.State.RUNNING == state) {
            Data progress = info.getProgress();
            int max = progress.getInt(KEY_PROGRESS_MAX,100);
            int current = progress.getInt(KEY_PROGRESS_CURRENT,0);
            String message = progress.getString(KEY_MESSAGE);

            Intent intent = new Intent(getApplicationContext(), BackupRestoreActivity.class);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,
                    intent,0);
            NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_DETERMINATE_PROGRESS)
                    .addAction(notificationHelper.createAction(R.string.cancel,workManager.createCancelPendingIntent(info.getId())))
                    .setProgressMax(max)
                    .setProgressCurrent(current)
                    .setContentIntent(pi)
                    .setNotificationTitle(getString( R.string.notification_title_restore))
                    .setNotificationMessage(message);
            notificationManager.notify(RESTORE_NOTIFICATION_ID,notificationHelper.newNotification(args));
        }

        if (state.isFinished()) stopForeground(false);

        if (WorkInfo.State.SUCCEEDED == state) {
            Intent intent = new Intent(getApplicationContext(), BackupRestoreActivity.class);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,
                    intent,0);
            NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_SIMPLE)
                    .setContentIntent(pi)
                    .setNotificationTitle(getString( R.string.notification_title_restore))
                    .setNotificationMessage(getString(R.string.message_local_restore_successful));
            notificationManager.notify(RESTORE_NOTIFICATION_ID,notificationHelper.newNotification(args));
        }
        else if (WorkInfo.State.FAILED == state) {
            Data output = info.getOutputData();
            int failureCode = output.getInt(KEY_FAILURE_CODE,-1);

            NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_SIMPLE)
                    .setNotificationTitle(getString( R.string.notification_title_restore))
                    .setAutoCancel(true);
            if (FAIL_FILE_ACCESS == failureCode) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", getPackageName(), null))
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
                args.setContentIntent(pi);
                args.setNotificationMessage(getString(R.string.message_error_file_access));
            }
            else if (FAIL_NON_EMPTY == failureCode) {
                // TODO: navigate to clear data activity
                args.setNotificationMessage(getString(R.string.message_restore_non_empty_database));
            }
            else {
                Intent intent = new Intent(getApplicationContext(), BackupRestoreActivity.class);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,
                        intent,0);
                args.setContentIntent(pi);
                args.addAction(notificationHelper.createAction(R.string.retry,
                        PendingIntent.getService(this,0,
                                new Intent(this,WorkActionService.class)
                                        .setAction(ACTION_RETRY_RESTORE)
                                        .putExtra(KEY_BACKUP_FILE,backupFile),0)));
                args.setNotificationMessage(getString(R.string.message_restore_fail_unknown));
            }
            notificationManager.notify(RESTORE_NOTIFICATION_ID,notificationHelper.newNotification(args));
        }
        else if (WorkInfo.State.CANCELLED == state) {
            Intent intent = new Intent(getApplicationContext(), BackupRestoreActivity.class);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,
                    intent,0);
            NotificationHelper.NotificationArgs args = new NotificationHelper.NotificationArgs(NOTIFICATION_TYPE_SIMPLE)
                    .setContentIntent(pi)
                    .setNotificationTitle(getString( R.string.notification_title_restore))
                    .setNotificationMessage(getString(R.string.message_restore_canceled))
                    .setAutoCancel(true);
            notificationManager.notify(RESTORE_NOTIFICATION_ID,notificationHelper.newNotification(args));
        }
        if (state.isFinished()) {
            removeEntry(info.getId());
        }
    }

    private void onRetryBackup() {
        BackupRestoreHelper.backupNow(this);
    }

    private void onRetryRestore(@NonNull Uri data) {
        if (null == data) return;
        BackupRestoreHelper.restore(this,data);
    }

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
