package dreammaker.android.expensetracker.backup;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import dreammaker.android.expensetracker.activity.SettingsActivity;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.database.MoneyTransfer;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.database.Transaction;

public class BackupData {

    private int version;
    private List<Account> accounts;
    private List<Person> people;
    private List<Transaction> transactions;
    private List<MoneyTransfer> money_transfers;
    private SettingsData settings;

    public BackupData() {}

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<Account> getAccounts() {
        return accounts == null ? Collections.emptyList() : accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Person> getPeople() {
        return people == null ? Collections.emptyList() : people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }

    public List<Transaction> getTransactions() {
        return transactions  == null ? Collections.emptyList() : transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<MoneyTransfer> getMoneyTransfers() {
        return money_transfers == null ? Collections.emptyList() : money_transfers;
    }

    public void setMoneyTransfers(List<MoneyTransfer> moneyTransfers) {
        this.money_transfers = moneyTransfers;
    }

    public SettingsData getSettings() {
        return settings;
    }

    public void setSettings(SettingsData settings) {
        this.settings = settings;
    }

    public static class SettingsData {

        private int backupAutoScheduleDuration;
        private String autoDeleteDuration;

        public int getBackupAutoScheduleDuration() {
            return backupAutoScheduleDuration;
        }

        public void setBackupAutoScheduleDuration(int backupAutoScheduleDuration) {
            this.backupAutoScheduleDuration = backupAutoScheduleDuration;
        }

        public String getAutoDeleteDuration() {
            return autoDeleteDuration;
        }

        public void setAutoDeleteDuration(String autoDeleteDuration) {
            this.autoDeleteDuration = autoDeleteDuration;
        }

        public static SettingsData extract(Context context) {
            SettingsData data = new SettingsData();
            data.setBackupAutoScheduleDuration(BackupRestoreHelper.getBackupAutoScheduleDuration(context));
            data.setAutoDeleteDuration(SettingsActivity.getAutoDeleteDuration(context));
            return data;
        }
    }
}
