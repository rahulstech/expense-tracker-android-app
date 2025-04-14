package dreammaker.android.expensetracker.backup;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.database.ExpensesBackupDao;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.MoneyTransfer;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Date;

import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.CURRENT_BACKUP_FILE_SCHEMA_VERSION;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.DIRECTORY_BACKUP;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_BACKUP_FILE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_RETRY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.createNewGSONInstance;
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
        Result result;
        Data.Builder output = new Data.Builder();
        try {
            // a backupNow operation requires at least some data in any of the tables
            // accounts, people or transaction. if none of the table contains data
            // then simply terminate the operation
            if (dao.isDatabaseEmpty()) {
                // database is empty, nothing to backup
                Log.d(TAG, "no data to backup");
                result = Result.failure();
            }
            else {
                // database is not empty, init backup operation
                Log.d(TAG,"data found to backup");
                if (!isStopped()) {
                    notifyBackupStart();
                    File backupFile = backup();
                    if (null != backupFile) {
                        output.putString(KEY_BACKUP_FILE, backupFile.getAbsolutePath());
                        result = Result.success(output.build());
                    }
                    else {
                        result = Result.failure(output.putBoolean(KEY_RETRY,
                                getInputData().getBoolean(KEY_RETRY,false)).build());
                    }
                }
                else {
                    result = Result.success();
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG,e.getMessage());
            result = Result.failure(output.putBoolean(KEY_RETRY,
                    getInputData().getBoolean(KEY_RETRY,false)).build());
        }
        return result;
    }

    private File backup() throws Exception {
        File backupFile = getBackupFile();
        try (Writer writer = new FileWriter(backupFile)) {
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

    private void backup(@NonNull Writer _writer) {
        Gson gson = createNewGSONInstance();
        BackupData data = new BackupData();
        data.setVersion(CURRENT_BACKUP_FILE_SCHEMA_VERSION);
        if (isStopped()) return;
        backupAppData(data);
        if (isStopped()) return;
        backupSettings(data);
        gson.toJson(data, _writer);
    }

    private void backupAppData(BackupData data) {
        List<Account> accountList = dao.getAccounts();
        List<Person> personList = dao.getPeople();
        List<Transaction> transactionList = dao.getTransactions();
        List<MoneyTransfer> moneyTransferList = dao.getMoneyTransfers();

        data.setAccounts(accountList);
        data.setPeople(personList);
        data.setTransactions(transactionList);
        data.setMoneyTransfers(moneyTransferList);
    }

    private void backupSettings(BackupData data) {
        BackupData.SettingsData settings = BackupData.SettingsData.extract(getApplicationContext());
        data.setSettings(settings);
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

    private void notifyBackupStart() {
        AppExecutor.getMainThreadExecutor().execute(() ->
                getApplicationContext().startService(
                        new Intent(getApplicationContext(), WorkActionService.class)
                                .setAction(WorkActionService.ACTION_LOCAL_BACKUP_START)
                                .putExtra(EXTRA_WORK_ID, getId())));
    }
}
