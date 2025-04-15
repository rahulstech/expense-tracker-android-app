package dreammaker.android.expensetracker.database;

import androidx.room.ColumnInfo;

import static androidx.room.ColumnInfo.INTEGER;
import static androidx.room.ColumnInfo.REAL;
import static dreammaker.android.expensetracker.database.ExpensesContract.BalanceAndDueSummaryColumns.COUNT_ACCOUNTS;
import static dreammaker.android.expensetracker.database.ExpensesContract.BalanceAndDueSummaryColumns.COUNT_PEOPLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.BalanceAndDueSummaryColumns.TOTAL_BALANCE;
import static dreammaker.android.expensetracker.database.ExpensesContract.BalanceAndDueSummaryColumns.TOTAL_DUE;

public class BalanceAndDueSummary {

    @ColumnInfo(name = TOTAL_BALANCE, typeAffinity = REAL, defaultValue = "0")
    private float totalBalance;
    @ColumnInfo(name = COUNT_ACCOUNTS, typeAffinity = INTEGER, defaultValue = "0")
    private int countTotalBalanceAccount;
    @ColumnInfo(name = TOTAL_DUE, typeAffinity = REAL, defaultValue = "0")
    private float totalDue;
    @ColumnInfo(name = COUNT_PEOPLE, typeAffinity = INTEGER, defaultValue = "0")
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
