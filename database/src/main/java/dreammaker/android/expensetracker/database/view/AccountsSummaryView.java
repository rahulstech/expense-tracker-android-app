package dreammaker.android.expensetracker.database.view;

import androidx.room.DatabaseView;
import dreammaker.android.expensetracker.database.type.Currency;

@DatabaseView(value = "SELECT " +
        "SUM(CASE WHEN CAST(`balance` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveAccounts`, " +
        "SUM(CASE WHEN CAST(`balance` AS REAL) > 0 THEN `balance` ELSE 0 END) AS `totalPositiveBalance`,  " +
        "SUM(CASE WHEN CAST(`balance` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeAccounts`, " +
        "SUM(CASE WHEN CAST(`balance` AS REAL) < 0 THEN `balance` ELSE 0 END) AS `totalNegativeBalance` " +
        "FROM `accounts`", viewName = "accounts_summary_view")
@SuppressWarnings("unused")
public class AccountsSummaryView {

    private Currency totalPositiveBalance;

    private int totalPositiveAccounts;

    private Currency totalNegativeBalance;

    private int totalNegativeAccounts;

    public AccountsSummaryView() {}

    public Currency getTotalPositiveBalance() {
        return totalPositiveBalance;
    }

    public void setTotalPositiveBalance(Currency totalPositiveBalance) {
        this.totalPositiveBalance = totalPositiveBalance;
    }

    public int getTotalPositiveAccounts() {
        return totalPositiveAccounts;
    }

    public void setTotalPositiveAccounts(int totalPositiveAccounts) {
        this.totalPositiveAccounts = totalPositiveAccounts;
    }

    public Currency getTotalNegativeBalance() {
        return totalNegativeBalance;
    }

    public void setTotalNegativeBalance(Currency totalNegativeBalance) {
        this.totalNegativeBalance = totalNegativeBalance;
    }

    public int getTotalNegativeAccounts() {
        return totalNegativeAccounts;
    }

    public void setTotalNegativeAccounts(int totalNegativeAccounts) {
        this.totalNegativeAccounts = totalNegativeAccounts;
    }
}
