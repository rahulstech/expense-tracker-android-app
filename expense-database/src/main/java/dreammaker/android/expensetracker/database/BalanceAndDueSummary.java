package dreammaker.android.expensetracker.database;

import androidx.room.ColumnInfo;

public class BalanceAndDueSummary {

    @ColumnInfo(name = "total_balance", defaultValue = "0")
    private float totalBalance;
    @ColumnInfo(name = "count_accounts", defaultValue = "0")
    private int countTotalBalanceAccount;
    @ColumnInfo(name = "total_due", defaultValue = "0")
    private float totalDue;
    @ColumnInfo(name = "count_people", defaultValue = "0")
    private int countTotalDuePerson;

    public BalanceAndDueSummary(float totalBalance, int countTotalBalanceAccount, float totalDue, int countTotalDuePerson) {
        this.totalBalance = totalBalance;
        this.countTotalBalanceAccount = countTotalBalanceAccount;
        this.totalDue = totalDue;
        this.countTotalDuePerson = countTotalDuePerson;
    }

    public float getTotalBalance() {
        return totalBalance;
    }

    public int getCountTotalBalanceAccount() {
        return countTotalBalanceAccount;
    }

    public float getTotalDue() {
        return totalDue;
    }

    public int getCountTotalDuePerson() {
        return countTotalDuePerson;
    }
}
