package dreammaker.android.expensetracker.backup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import dreammaker.android.expensetracker.database.ExpensesBackupDao;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.database.Date;
import dreammaker.android.expensetracker.util.ResultCallback;

public class BackupRestoreHelper {

    public static final int VERSION_5 = 5;
    public static final int VERSION_6 = 6;
    public static final int VERSION_7 = 7;

    public static final int CURRENT_BACKUP_FILE_SCHEMA_VERSION = VERSION_7;

    public static final String BACKUP_WORK_TAG = "backup";
    public static final String RESTORE_WORK_TAG = "restore";
    public static final int BACKUP_NOTIFICATION_ID = 2058;
    public static final String DIRECTORY_BACKUP = "ExpenseTracker/backup";

    public static final String KEY_BACKUP_FILE = "backup_file";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_FAILURE_CODE = "failure_code";
    public static final String KEY_PROGRESS_MAX = "progress_max";
    public static final String KEY_PROGRESS_CURRENT = "progress_current";
    public static final String KEY_RETRY = "retry";

    public static final int FAIL_FILE_ACCESS = 1;
    public static final int FAIL_NO_DATA = 2;
    public static final int FAIL_NON_EMPTY = 3;
    public static final int FAIL_UNKNOWN = 4;
    public static final int FAIL_NO_FILE = 5;

    public static final int SCHEDULE_NEVER = 0;
    public static final int SCHEDULE_DAILY = 1;
    public static final int SCHEDULE_WEEKLY = 2;
    public static final int SCHEDULE_MONTHLY = 3;

    public static final String KEY_FIRST_RESTORE_ASKED = "first_restore_asked";
    public static final String KEY_LAST_LOCAL_BACKUP_DATE = "last_local_backup_date";
    public static final String KEY_BACKUP_AUTO_SCHEDULE_DURATION = "backup_auto_schedule_duration";

    public static Gson createNewGSONInstance() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeHierarchyAdapter(Date.class,new TypeAdapter<Date>() {
                    @Override
                    public void write(JsonWriter out, Date value) throws IOException {
                        out.value(value.format(Date.ISO_DATE_PATTERN));
                    }

                    @Override
                    public Date read(JsonReader in) throws IOException {
                        return Date.valueOf(in.nextString(),Date.ISO_DATE_PATTERN);
                    }
                })
                .create();
    }

    public static Date getLastLocalBackDate(@NonNull Context context) {
        String date = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LAST_LOCAL_BACKUP_DATE,null);
        return Check.isNull(date) ? null : Date.valueOf(date);
    }

    public static void setLastLocalBackupDate(@NonNull Context context, Date date) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (null == date)
            editor.remove(KEY_LAST_LOCAL_BACKUP_DATE);
        else
            editor.putString(KEY_LAST_LOCAL_BACKUP_DATE,date.format(Date.ISO_DATE_PATTERN));
        editor.apply();
    }

    public static boolean isFirstRestoreAsked(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_FIRST_RESTORE_ASKED,false);
    }

    public static void setFirstRestoreAsked(@NonNull Context context, boolean asked) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KEY_FIRST_RESTORE_ASKED,asked).apply();
    }

    public static int getBackupAutoScheduleDuration(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_BACKUP_AUTO_SCHEDULE_DURATION,SCHEDULE_NEVER);
    }

    public static void setBackupAutoScheduleDuration(@NonNull Context context, int value) {
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putInt(KEY_BACKUP_AUTO_SCHEDULE_DURATION,value)
                .commit()) {
            backupNext(context);
        }
    }

    public static void checkAppFirstRestoreRequired(@NonNull Context context, @NonNull ResultCallback<Boolean> callback) {
        Context appContext = context.getApplicationContext();
        if (!isFirstRestoreAsked(appContext)) {
            AppExecutor.getDiskOperationsExecutor().execute(() -> {
                ExpensesBackupDao dao = ExpensesDatabase.getInstance(appContext).getBackupDao();
                final boolean isRequired = dao.isDatabaseEmpty();
                AppExecutor.getMainThreadExecutor().execute(() -> {
                    callback.onResult(isRequired);
                    setFirstRestoreAsked(appContext, true);
                });
            });
        }
    }

    public static void backupNow(@NonNull Context context) {
        backupNow(context,false);
    }

    public static void backupNow(@NonNull Context context, boolean isRetry) {
        Context appContext = context.getApplicationContext();
        WorkManager workManager = WorkManager.getInstance(appContext);
        backup(appContext,workManager,false, isRetry);
    }

    public static void backupNext(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        WorkManager workManager = WorkManager.getInstance(appContext);
        int code = getBackupAutoScheduleDuration(context);
        if (SCHEDULE_NEVER != code)
            backup(appContext,workManager,true, false);
        else
            workManager.cancelUniqueWork(BACKUP_WORK_TAG);
    }

    private static void backup(@NonNull Context appContext,
                               @NonNull WorkManager workManager,
                               boolean scheduled, boolean isRetry) {
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
            enqueueNewBackupRequest(appContext,workManager,scheduled,isRetry);
        },AppExecutor.getMainThreadExecutor());
    }

    private static void enqueueNewBackupRequest(@NonNull Context appContext,
                                                @NonNull WorkManager workManager,
                                                boolean scheduled,
                                                boolean isRetry
                                                ) {
        Data input = new Data.Builder().putBoolean(KEY_RETRY, isRetry).build();
            OneTimeWorkRequest.Builder request = new OneTimeWorkRequest.Builder(LocalBackupWork.class)
                    .addTag(BACKUP_WORK_TAG).setInputData(input);
            if (scheduled) {
                long initialDelay = calculateInitialDelayForNextScheduledBackup(appContext);
                request.setInitialDelay(initialDelay, TimeUnit.MILLISECONDS);
            }
        workManager.enqueueUniqueWork(BACKUP_WORK_TAG, ExistingWorkPolicy.KEEP,request.build());
    }

    public static void onBackupSuccessful(@NonNull Context appContext) {
        setLastLocalBackupDate(appContext, new Date());
        backupNext(appContext);
    }

    public static void restore(@NonNull Context context, @NonNull Uri from) {
        context.startService(new Intent(context,WorkActionService.class)
                .setAction(WorkActionService.ACTION_LOCAL_RESTORE_START).setData(from));
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
