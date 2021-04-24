package dreammaker.android.expensetracker.backup;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
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

import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_FILE_ACCESS;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_NON_EMPTY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_NO_FILE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_UNKNOWN;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_ACCOUNTS;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_ACCOUNT_ID;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_ACCOUNT_NAME;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_AMOUNT;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_BACKUP_FILE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_BALANCE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_DATE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_DESCRIPTION;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_DUE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_FAILURE_CODE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_ID;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_MESSAGE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PEOPLE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PERSON_ID;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PERSON_NAME;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PROGRESS_CURRENT;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PROGRESS_MAX;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_TRANSACTIONS;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_TYPE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_VERSION;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.VERSION_6;
import static dreammaker.android.expensetracker.backup.WorkActionService.EXTRA_WORK_ID;

public class LocalRestoreWork extends Worker {

    private static final String TAG = "LocalRestoreWork";

    private ExpensesBackupDao dao;
    private Uri backupFile;
    private String backupFileSimpleName;
    private int currentProgress = 0;
    private int maxProgress = 6;

    public LocalRestoreWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        dao = ExpensesDatabase.getInstance(getApplicationContext()).getBackupDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        Data.Builder output = new Data.Builder();
        Result result = null;
        try {
            // a restore requires the database no accounts and no people and no transactions;
            // if not then restore operation is terminated and user is asked to delete those
            // data manually and then perform restore
            if (!dao.isDatabaseEmpty()) {
                // database non empty; restore canceled
                Log.i(TAG,"database found non empty");
                output.putInt(KEY_FAILURE_CODE,FAIL_NON_EMPTY);
                result = Result.failure();
            }
            else {
                // database empty perform restore
                Log.i(TAG,"database found empty");
                Data input = getInputData();
                String vBackupFile = input.getString(KEY_BACKUP_FILE);
                Uri uri = Uri.parse(vBackupFile);
                if (!isStopped()) {
                    backupFile = uri;
                    backupFileSimpleName = uri.getLastPathSegment();
                    currentProgress = 0;
                    notifyRestoreStart();
                    restoreFromFile(uri);
                    result = Result.success();
                }
            }
        }
        catch (SecurityException e) {
            output.putInt(KEY_FAILURE_CODE,FAIL_FILE_ACCESS);
            result = Result.failure(output.build());
        }
        catch (FileNotFoundException e) {
            output.putInt(KEY_FAILURE_CODE, FAIL_NO_FILE);
            result = Result.failure(output.build());
        }
        catch (Exception e) {
            e.printStackTrace();
            output.putInt(KEY_FAILURE_CODE,FAIL_UNKNOWN)
                    .putAll(getInputData());
            result = Result.failure();
        }
        return result;
    }

    private void restoreFromFile(@NonNull Uri backupFile) throws Exception {
        InputStreamReader inReader = new InputStreamReader(getApplicationContext().getContentResolver()
                .openInputStream(backupFile));
        try (BufferedReader reader = new BufferedReader(inReader)) {
            restore(reader);
        }
    }

    private void restore(@NonNull Reader reader) throws IOException, SQLException {
        JsonReader r = new JsonReader(reader);
        List<Account> accounts = null;
        List<Person> people = null;
        List<Transaction> transactions = null;
        int version = -1;

        if (isStopped()) return;

        // read from json file first
        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            switch (name) {
                case KEY_ACCOUNTS: accounts = readAccounts(r);
                    break;
                case KEY_PEOPLE: people = readPeople(r);
                    break;
                case KEY_TRANSACTIONS: transactions = readTransactions(r);
                    break;
                case KEY_VERSION: version = r.nextInt();
            }
        }
        r.endObject();

        if (isStopped()) return;
        // insert values into database
        dao.insertAccounts(accounts);
        updateProgressDeterminant();
        dao.insertPeople(people);
        updateProgressDeterminant();
        dao.insertTransactions(transactions);
        updateProgressDeterminant();
        if (!transactions.isEmpty() && version < VERSION_6) {
            // backupNow files before version 5 do not contain account "balance" attribute
            // and person "due" attribute. So, we need to set those values now
            dao.setAccountsBalancesAndPeopleDues();
        }
    }

    @NonNull
    private List<Account> readAccounts(@NonNull JsonReader r) throws IOException {
        List<Account> accounts = new ArrayList<>();
        if (!isStopped()) {
            r.beginArray();
            while (r.hasNext()) {
                if (isStopped()) break;
                accounts.add(readAccount(r));
            }
            r.endArray();
            updateProgressDeterminant();
        }
        return accounts;
    }

    @NonNull
    private List<Person> readPeople(@NonNull JsonReader r) throws IOException {
        List<Person> people = new ArrayList<>();
        if (!isStopped()) {
            r.beginArray();
            while (r.hasNext()) {
                if (isStopped()) break;
                people.add(readPerson(r));
            }
            r.endArray();
            updateProgressDeterminant();
        }
        return people;
    }

    @NonNull
    private List<Transaction> readTransactions(@NonNull JsonReader r) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        if (!isStopped()) {
            r.beginArray();
            while (r.hasNext()) {
                if (isStopped()) break;
                transactions.add(readTransaction(r));
            }
            r.endArray();
            updateProgressDeterminant();
        }
        return transactions;
    }

    @NonNull
    private Account readAccount(@NonNull JsonReader r) throws IOException {
        Account account = new Account();
        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            if (KEY_ID.equals(name)) {
                account.setAccountId(r.nextLong());
            }
            else if (KEY_ACCOUNT_NAME.equals(name)) {
                account.setAccountName(r.nextString());
            }
            else if (KEY_BALANCE.equals(name)) {
                account.setBalance(Double.valueOf(r.nextDouble()).floatValue());
            }
        }
        r.endObject();
        return account;
    }

    @NonNull
    private Person readPerson(@NonNull JsonReader r) throws IOException {
        Person person = new Person();
        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            if (KEY_ID.equals(name)) {
                person.setPersonId(r.nextLong());
            }
            else if (KEY_PERSON_NAME.equals(name)) {
                person.setPersonName(r.nextString());
            }
            else if (KEY_DUE.equals(name)) {
                person.setDue(Double.valueOf(r.nextDouble()).floatValue());
            }
        }
        r.endObject();
        return person;
    }

    @NonNull
    private Transaction readTransaction(@NonNull JsonReader r) throws IOException {
        Transaction transaction = new Transaction();
        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            switch (name) {
                case KEY_ID: transaction.setTransactionId(r.nextLong());
                    break;
                case KEY_ACCOUNT_ID: transaction.setAccountId(r.nextLong());
                    break;
                case KEY_PERSON_ID: {
                    JsonToken token = r.peek();
                    if (JsonToken.NULL == token) {
                        r.nextNull();
                    }
                    else {
                        transaction.setPersonId(r.nextLong());
                    }
                }
                break;
                case KEY_AMOUNT: transaction.setAmount(Double.valueOf(r.nextDouble()).floatValue());
                    break;
                case KEY_TYPE: transaction.setType(r.nextInt());
                    break;
                case KEY_DATE: transaction.setDate(Date.valueOf(r.nextString()));
                break;
                case KEY_DESCRIPTION: transaction.setDescription(r.nextString());
            }
        }
        r.endObject();
        return transaction;
    }

    private void notifyRestoreStart() {
        AppExecutor.getMainThreadExecutor().execute(() -> {
            ContextCompat.startForegroundService(getApplicationContext(),
                    new Intent(getApplicationContext(),WorkActionService.class)
                            .setAction(WorkActionService.ACTION_LOCAL_RESTORE_START)
                            .putExtra(EXTRA_WORK_ID,getId()).putExtra(KEY_BACKUP_FILE, backupFile));
        });
        Data progress = new Data.Builder()
                .putString(KEY_MESSAGE,getResourceString(R.string.message_local_restore_start))
                .build();
        setProgressAsync(progress);
    }

    private void updateProgressDeterminant() {
        if (isStopped()) return;
        int current = ++this.currentProgress;
        int max = this.maxProgress;
        int percentage = current/max * 100;
        updateProgressDeterminant(max,current,
                getApplicationContext().getString(R.string.message_restore_progress,backupFileSimpleName,percentage));
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
