package dreammaker.android.expensetracker.database;

public class AboutAccount extends Account {

    public AboutAccount(long accountId, String accountName, float balance) {
        super(accountId,accountName,balance);
    }

    public float getBalance() {
        return Math.max(0,super.getBalance());
    }
}
