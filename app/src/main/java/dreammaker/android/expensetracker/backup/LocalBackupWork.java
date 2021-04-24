package dreammaker.android.expensetracker.backup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.JsonWriter;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.database.ExpensesBackupDao;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Date;

import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_BACKUP_FILE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.CURRENT_BACKUP_FILE_SCHEMA_VERSION;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.DIRECTORY_BACKUP;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_FILE_ACCESS;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_NO_DATA;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_UNKNOWN;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_ACCOUNTS;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_ACCOUNT_ID;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_ACCOUNT_NAME;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_AMOUNT;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_FAILURE_CODE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_MESSAGE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_BALANCE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_DATE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_DESCRIPTION;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_DUE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_ID;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PEOPLE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PERSON_ID;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PERSON_NAME;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PROGRESS_CURRENT;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PROGRESS_MAX;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_TRANSACTIONS;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_TYPE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_VERSION;
import static dreammaker.android.expensetracker.backup.WorkActionService.EXTRA_WORK_ID;

public class LocalBackupWork extends Worker {

    private static final String TAG = "LocalBackupWork";

    private ExpensesBackupDao dao;

    public LocalBackupWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        dao = ExpensesDatabase.getInstance(getApplicationContext()).getBackupDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        Data.Builder output = new Data.Builder();
        Result result = null;
        try {
            // a backupNow operation requires at least some data in any of the tables
            // accounts, people or transaction. if none of the table contains data
            // then simply terminate the operation
            if (dao.isDatabaseEmpty()) {
                // database is empty, nothing to backup
                Log.i(TAG, "no data to backup");
                output.putInt(KEY_FAILURE_CODE,FAIL_NO_DATA);
                result = Result.failure(output.build());
            }
            else {
                // database is not empty, init backup operation
                Log.i(TAG,"data found to backup");
                if (!isStopped()) {
                    notifyBackupStart();
                    File backupFile = backup();
                    if (null != backupFile) {
                        output.putString(KEY_BACKUP_FILE, Uri.fromFile(backupFile).toString());
                        result = Result.success(output.build());
                    }
                    else {
                        output.putInt(KEY_FAILURE_CODE,FAIL_UNKNOWN);
                        result = Result.failure(output.build());
                    }
                }
            }
        }
        catch (SecurityException e) {
            output.putInt(KEY_FAILURE_CODE,FAIL_FILE_ACCESS);
            result = Result.failure(output.build());
        }
        catch (Exception e) {
            e.printStackTrace();
            output.putInt(KEY_FAILURE_CODE,FAIL_UNKNOWN);
            result = Result.failure(output.build());
        }
        return result;
    }

    private File backup() throws Exception {
        File backupFile = getBackupFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))) {
            backup(writer);
        }
        finally {
            if (isStopped()) {
                backupFile.delete();
                return null;
            }
        }
        return backupFile;
    }

    private void backup(@NonNull Writer _writer) throws IOException, SecurityException {

        try (JsonWriter writer = new JsonWriter(_writer)){
            List<Account> accountList = dao.getAccounts();
            List<Person> personList = dao.getPeople();
            List<Transaction> transactionList = dao.getTransactions();

            if (isStopped()) return;

            writer.beginObject();
            writer.name(KEY_VERSION).value(CURRENT_BACKUP_FILE_SCHEMA_VERSION);
            writeAccounts(writer,accountList);
            writePeople(writer,personList);
            writeTransactions(writer,transactionList);
            writer.endObject();
            writer.flush();
        }
    }

    private void writeAccounts(@NonNull JsonWriter writer, @Nullable List<Account> accounts) throws IOException {
        writer.name(KEY_ACCOUNTS);
        writer.beginArray();
        if (null != accounts) {
            int max = 3;
            int current = 1;
            if (isStopped()) return;
            updateProgressDeterminant(3,1,getProgressString(R.string.message_backup_progress_accounts,max,current));
            for (Account a : accounts) {
                if (isStopped()) return;
                writeAccount(writer, a);
            }
            if (!isStopped())
                updateProgressDeterminant(3,1,getProgressString(R.string.message_backup_progress_accounts,max,current));
        }
        writer.endArray();
    }

    private void writePeople(@NonNull JsonWriter writer,@Nullable List<Person> people) throws IOException {
        writer.name(KEY_PEOPLE);
        writer.beginArray();
        if (null != people) {
            int max = 3;
            int current = 2;
            if (isStopped()) return;
            for (Person p : people) {
                if (isStopped()) return;
                writePerson(writer,p);
            }
            if (!isStopped())
                updateProgressDeterminant(max,current,getProgressString(R.string.message_backup_progress_people,max,current));
        }
        writer.endArray();
    }

    private void writeTransactions(@NonNull JsonWriter writer, @Nullable List<Transaction> transactions) throws IOException {
        writer.name(KEY_TRANSACTIONS);
        writer.beginArray();
        if (null != transactions) {
            int max = 3;
            int current = 3;
            if (isStopped()) return;
            for (Transaction t : transactions) {
                if (isStopped()) return;
                writeTransaction(writer,t);
            }
            if (!isStopped())
                updateProgressDeterminant(max,current,getProgressString(R.string.message_backup_progress_transactions,max,current));
        }
        writer.endArray();
    }

    private void writeAccount(@NonNull JsonWriter w,@NonNull Account a) throws IOException {
        w.beginObject();
        w.name(KEY_ID).value(a.getAccountId());
        w.name(KEY_ACCOUNT_NAME).value(a.getAccountName());
        w.name(KEY_BALANCE).value(a.getBalance());
        w.endObject();
    }

    private void writePerson(@NonNull JsonWriter w,@NonNull Person p) throws IOException {
        w.beginObject();
        w.name(KEY_ID).value(p.getPersonId());
        w.name(KEY_PERSON_NAME).value(p.getPersonName());
        w.name(KEY_DUE).value(p.getDue());
        w.endObject();
    }

    private void writeTransaction(@NonNull JsonWriter writer, @NonNull Transaction t) throws IOException {
        writer.beginObject();
        writer.name(KEY_ID).value(t.getTransactionId());
        writer.name(KEY_ACCOUNT_ID).value(t.getAccountId());
        writer.name(KEY_PERSON_ID);
        if (null == t.getPersonId()) {
            writer.nullValue();
        }
        else {
            writer.value(t.getPersonId());
        }
        writer.name(KEY_AMOUNT).value(t.getAmount());
        writer.name(KEY_TYPE).value(t.getType());
        writer.name(KEY_DATE).value(t.getDate().format(Date.ISO_DATE_PATTERN));
        writer.name(KEY_DESCRIPTION);
        if (null == t.getDescription()) {
            writer.nullValue();
        }
        else {
            writer.value(t.getDescription());
        }
        writer.endObject();
    }

    private File getBackupFile() throws SecurityException {
        Date now = new Date();
        String filename = "et_backup_"+now.format("yyyy_MM_dd")+".json";
        return new File(getBackupDirectory(),filename);
    }

    private File getBackupDirectory() throws SecurityException {
        File dir = new File(Environment.getExternalStorageDirectory(), DIRECTORY_BACKUP);
        dir.mkdirs();
        return dir;
    }

    private String getProgressString(@StringRes  int resId, int max, int current) {
        return getApplicationContext().getString(resId, current, max);
    }

    private void notifyBackupStart() {
        AppExecutor.getMainThreadExecutor().execute(() -> {
            ContextCompat.startForegroundService(getApplicationContext(),
                    new Intent(getApplicationContext(),WorkActionService.class)
                            .setAction(WorkActionService.ACTION_LOCAL_BACKUP_START)
                            .putExtra(EXTRA_WORK_ID,getId()));
        });
        Data progress = new Data.Builder()
                .putString(KEY_MESSAGE,getResourceString(R.string.message_local_backup_start))
                .build();
        setProgressAsync(progress);
    }

    private void updateProgressDeterminant(int max, int current, String message) {
        Data progress = new Data.Builder()
                .putInt(KEY_PROGRESS_MAX,max)
                .putInt(KEY_PROGRESS_CURRENT,current)
                .putString(KEY_MESSAGE,message)
                .build();
        setProgressAsync(progress);
    }

    @NonNull
    private String getResourceString(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }
}
