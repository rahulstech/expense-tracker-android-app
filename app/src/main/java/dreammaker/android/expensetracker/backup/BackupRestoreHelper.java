package dreammaker.android.expensetracker.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import dreammaker.android.expensetracker.database.ExpensesBackupDao;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.util.ResultCallback;

public class BackupRestoreHelper {

    private static final String TAG = "BackupRestoreHelper";

    public static final int VERSION_5 = 5;
    public static final int VERSION_6 = 6;

    public static final int CURRENT_BACKUP_FILE_SCHEMA_VERSION = VERSION_6;

    public static final String BACKUP_WORK_TAG = "backup";
    public static final String RESTORE_WORK_TAG = "restore";
    public static final int RESTORE_NOTIFICATION_ID = 1453;
    public static final int BACKUP_NOTIFICATION_ID = 2058;
    public static final String DIRECTORY_BACKUP = "ExpenseTracker/backup";

    public static final String KEY_BACKUP_FILE = "backup_file";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_FAILURE_CODE = "failure_code";
    public static final String KEY_PROGRESS_MAX = "progress_max";
    public static final String KEY_PROGRESS_CURRENT = "progress_current";

    public static final int FAIL_FILE_ACCESS = 1;
    public static final int FAIL_NO_DATA = 2;
    public static final int FAIL_NON_EMPTY = 3;
    public static final int FAIL_UNKNOWN = 4;
    public static final int FAIL_NO_FILE = 5;

    public static final String KEY_ID = "_id";
    public static final String KEY_ACCOUNT_NAME = "account_name";
    public static final String KEY_BALANCE = "balance";
    public static final String KEY_PERSON_NAME = "person_name";
    public static final String KEY_DUE = "due";
    public static final String KEY_ACCOUNT_ID = "account_id";
    public static final String KEY_PERSON_ID = "person_id";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DATE = "date";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_VERSION = "version";
    public static final String KEY_ACCOUNTS = "accounts";
    public static final String KEY_PEOPLE = "people";
    public static final String KEY_TRANSACTIONS = "transactions";

    public static final int SCHEDULE_NEVER = 0;
    public static final int SCHEDULE_DAILY = 1;
    public static final int SCHEDULE_WEEKLY = 2;
    public static final int SCHEDULE_MONTHLY = 3;

    private static final String KEY_FIRST_RESTORE_ASKED = "first_restore_asked";
    private static final String SHARED_PREFERENCE_NAME = "sp_backup_restore";
    private static final String KEY_LAST_LOCAL_BACKUP_DATE = "last_local_backup_date";
    private static final String KEY_BACKUP_AUTO_SCHEDULE_DURATION = "backup_auto_schedule_duration";


    public static Date getLastLocalBackDate(Context context) {
        String date = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LAST_LOCAL_BACKUP_DATE, null);
        return Check.isNull(date) ? null : Date.valueOf(date);
    }

    public static void setLastLocalBackupDate(Context context, Date date) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        if (null == date)
            editor.remove(KEY_LAST_LOCAL_BACKUP_DATE);
        else
            editor.putString(KEY_LAST_LOCAL_BACKUP_DATE,date.toString());
        editor.apply();
    }

    public static boolean isFirstRestoreAsked(@NonNull Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_FIRST_RESTORE_ASKED,false);
    }

    public static void setFirstRestoreAsked(@NonNull Context context) {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_FIRST_RESTORE_ASKED,true).apply();
    }

    public static int getBackupAutoScheduleDuration(@NonNull Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE)
                .getInt(KEY_BACKUP_AUTO_SCHEDULE_DURATION,SCHEDULE_NEVER);
    }

    public static void setBackupAutoScheduleDuration(@NonNull Context context, int value) {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE)
                .edit().putInt(KEY_BACKUP_AUTO_SCHEDULE_DURATION,value)
                .apply();
    }

    public static void checkAppFirstRestoreRequired(@NonNull Context context, @NonNull ResultCallback<Boolean> callback) {
        Context appContext = context.getApplicationContext();
        if (!isFirstRestoreAsked(appContext)) {
            AppExecutor.getDiskOperationsExecutor().execute(() -> {
                ExpensesBackupDao dao = ExpensesDatabase.getInstance(appContext).getBackupDao();
                final boolean isRequired = dao.isDatabaseEmpty();
                AppExecutor.getMainThreadExecutor().execute(() -> {
                    if (null != callback) callback.onResult(isRequired);
                    setFirstRestoreAsked(appContext);
                });
            });
        }
    }

    public static void backupNow(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        WorkManager workManager = WorkManager.getInstance(appContext);
        backup(appContext,workManager,false);
    }

    public static void backupNext(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        WorkManager workManager = WorkManager.getInstance(appContext);
        int code = getBackupAutoScheduleDuration(context);
        if (SCHEDULE_NEVER != code)
            backup(appContext,workManager,true);
        else
            workManager.cancelUniqueWork(BACKUP_WORK_TAG);
    }

    private static void backup(@NonNull Context appContext,
                               @NonNull WorkManager workManager,
                               boolean scheduled) {
        ListenableFuture<List<WorkInfo>> future = workManager.getWorkInfos(
                WorkQuery.Builder.fromTags(Arrays.asList(BACKUP_WORK_TAG))
                .addStates(Arrays.asList(WorkInfo.State.ENQUEUED)).build());
        future.addListener(() -> {
            try {
                List<WorkInfo> infos = future.get();
                if (null != infos && !infos.isEmpty()) {
                    workManager.cancelWorkById(infos.get(0).getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            enqueueNewBackupRequest(appContext,workManager,scheduled);
        },AppExecutor.getMainThreadExecutor());
    }

    private static void enqueueNewBackupRequest(@NonNull Context appContext,
                                                @NonNull WorkManager workManager,
                                                final boolean scheduled
                                                ) {
            OneTimeWorkRequest.Builder request = new OneTimeWorkRequest.Builder(LocalBackupWork.class)
                    .addTag(BACKUP_WORK_TAG);
            if (scheduled) {
                long initialDelay = calculateInitialDelayForNextScheduledBackup(appContext);
                request.setInitialDelay(initialDelay, TimeUnit.MILLISECONDS);
            }
            Operation operation = workManager.enqueueUniqueWork(BACKUP_WORK_TAG, ExistingWorkPolicy.KEEP,request.build());
            ListenableFuture<Operation.State.SUCCESS> resultFuture = operation.getResult();
            resultFuture.addListener(() -> onBackupSuccessful(appContext),
                    AppExecutor.getMainThreadExecutor());
    }

    private static void onBackupSuccessful(@NonNull Context appContext) {
        setLastLocalBackupDate(appContext, new Date());
    }

    public static void restore(@NonNull Context context, @NonNull Uri from) {
        final Context appContext = context.getApplicationContext();
        WorkManager workManager = WorkManager.getInstance(appContext);
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LocalRestoreWork.class)
                .addTag(RESTORE_WORK_TAG)
                .setInputData(new Data.Builder().putString(KEY_BACKUP_FILE,from.toString()).build())
                .build();
        workManager.enqueueUniqueWork(RESTORE_WORK_TAG, ExistingWorkPolicy.KEEP,request);
    }

    private static long calculateInitialDelayForNextScheduledBackup(@NonNull Context context) {
        int code = getBackupAutoScheduleDuration(context);

        if (SCHEDULE_NEVER == code) return 0;

        Calendar todayAtBeginningOfScheduledBackupStart = Calendar.getInstance();
        todayAtBeginningOfScheduledBackupStart.set(Calendar.HOUR_OF_DAY,2);
        todayAtBeginningOfScheduledBackupStart.set(Calendar.MINUTE,0);
        todayAtBeginningOfScheduledBackupStart.set(Calendar.SECOND,0);

        long todayScheduledBackupStartMillis = todayAtBeginningOfScheduledBackupStart.getTimeInMillis();
        long difference;

        if (SCHEDULE_DAILY == code) {
            difference = TimeUnit.MILLISECONDS.convert(1,TimeUnit.DAYS);
        }
        else if (SCHEDULE_WEEKLY == code) {
            Calendar atBeginningOfNextWeek = Calendar.getInstance();
            atBeginningOfNextWeek.add(Calendar.WEEK_OF_YEAR,1);
            atBeginningOfNextWeek.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
            difference = atBeginningOfNextWeek.getTimeInMillis()-System.currentTimeMillis();
        }
        else if (SCHEDULE_MONTHLY == code) {
            Calendar atBeginningOfNextMonth = Calendar.getInstance();
            atBeginningOfNextMonth.add(Calendar.MONTH,1);
            atBeginningOfNextMonth.set(Calendar.DAY_OF_MONTH,1);
            difference = atBeginningOfNextMonth.getTimeInMillis()-System.currentTimeMillis();
        }
        else {
            throw new IllegalArgumentException("unknown code="+code);
        }

        return difference+todayScheduledBackupStartMillis-System.currentTimeMillis();
    }
}
