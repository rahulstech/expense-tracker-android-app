package dreammaker.android.expensetracker.database.view;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.DatabaseView;
import dreammaker.android.expensetracker.database.type.Currency;

@DatabaseView(value = "SELECT * FROM (SELECT * FROM `accounts_summary_view`), (SELECT * FROM `people_summary_view`), " +
        "(SELECT COUNT(`id`) AS `totalAccounts` FROM `accounts`), (SELECT COUNT(`id`) AS `totalPeople` FROM `people`)",
        viewName = "asset_liability_summary_view")
@SuppressWarnings("unused")
public class AssetLiabilitySummary {

    private Currency totalPositiveBalance;

    private int totalPositiveAccounts;

    private Currency totalNegativeBalance;

    private int totalNegativeAccounts;

    private int totalPositiveDuePeople;

    private int totalPositiveBorrowPeople;

    private int totalNegativeDuePeople;

    private int totalNegativeBorrowPeople;

    private Currency totalPositiveDue;

    private Currency totalPositiveBorrow;

    private Currency totalNegativeDue;

    private Currency totalNegativeBorrow;

    private int totalAccounts;

    private int totalPeople;

    public AssetLiabilitySummary() {}

    public Currency getTotalPositiveBalance() {
        return null == totalPositiveBalance ? Currency.ZERO : totalPositiveBalance;
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
        return null == totalNegativeBalance ? Currency.ZERO : totalNegativeBalance;
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

    public int getTotalPositiveDuePeople() {
        return totalPositiveDuePeople;
    }

    public void setTotalPositiveDuePeople(int totalPositiveDuePeople) {
        this.totalPositiveDuePeople = totalPositiveDuePeople;
    }

    public int getTotalPositiveBorrowPeople() {
        return totalPositiveBorrowPeople;
    }

    public void setTotalPositiveBorrowPeople(int totalPositiveBorrowPeople) {
        this.totalPositiveBorrowPeople = totalPositiveBorrowPeople;
    }

    public int getTotalNegativeDuePeople() {
        return totalNegativeDuePeople;
    }

    public void setTotalNegativeDuePeople(int totalNegativeDuePeople) {
        this.totalNegativeDuePeople = totalNegativeDuePeople;
    }

    public int getTotalNegativeBorrowPeople() {
        return totalNegativeBorrowPeople;
    }

    public void setTotalNegativeBorrowPeople(int totalNegativeBorrowPeople) {
        this.totalNegativeBorrowPeople = totalNegativeBorrowPeople;
    }

    public Currency getTotalPositiveDue() {
        return null == totalPositiveDue ? Currency.ZERO : totalPositiveDue;
    }

    public void setTotalPositiveDue(Currency totalPositiveDue) {
        this.totalPositiveDue = totalPositiveDue;
    }

    public Currency getTotalPositiveBorrow() {
        return null == totalPositiveBorrow ? Currency.ZERO : totalPositiveBorrow;
    }

    public void setTotalPositiveBorrow(Currency totalPositiveBorrow) {
        this.totalPositiveBorrow = totalPositiveBorrow;
    }

    public Currency getTotalNegativeDue() {
        return null == totalNegativeDue ? Currency.ZERO : totalNegativeDue;
    }

    public void setTotalNegativeDue(Currency totalNegativeDue) {
        this.totalNegativeDue = totalNegativeDue;
    }

    public Currency getTotalNegativeBorrow() {
        return null == totalNegativeBorrow ? Currency.ZERO : totalNegativeBorrow;
    }

    public void setTotalNegativeBorrow(Currency totalNegativeBorrow) {
        this.totalNegativeBorrow = totalNegativeBorrow;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(int totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public int getTotalPeople() {
        return totalPeople;
    }

    public void setTotalPeople(int totalPeople) {
        this.totalPeople = totalPeople;
    }

    @NonNull
    public Currency getTotalAsset() {
        return Currency.ZERO.add(getTotalPositiveBalance())
                .add(getTotalPositiveDue()).add(getTotalNegativeBorrow());
    }

    @NonNull
    public Currency getTotalLiability() {
        return Currency.ZERO.add(getTotalNegativeBalance())
                .add(getTotalNegativeDue()).add(getTotalPositiveBorrow());
    }

    @NonNull
    @Override
    public String toString() {
        return "AssetLiabilitySummary{" +
                "totalPositiveBalance=" + totalPositiveBalance +
                ", totalPositiveAccounts=" + totalPositiveAccounts +
                ", totalNegativeBalance=" + totalNegativeBalance +
                ", totalNegativeAccounts=" + totalNegativeAccounts +
                ", totalPositiveDuePeople=" + totalPositiveDuePeople +
                ", totalPositiveBorrowPeople=" + totalPositiveBorrowPeople +
                ", totalNegativeDuePeople=" + totalNegativeDuePeople +
                ", totalNegativeBorrowPeople=" + totalNegativeBorrowPeople +
                ", totalPositiveDue=" + totalPositiveDue +
                ", totalPositiveBorrow=" + totalPositiveBorrow +
                ", totalNegativeDue=" + totalNegativeDue +
                ", totalNegativeBorrow=" + totalNegativeBorrow +
                ", totalAccounts=" + totalAccounts +
                ", totalPeople=" + totalPeople +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetLiabilitySummary)) return false;
        AssetLiabilitySummary that = (AssetLiabilitySummary) o;
        return totalPositiveAccounts == that.totalPositiveAccounts && totalNegativeAccounts == that.totalNegativeAccounts && totalPositiveDuePeople == that.totalPositiveDuePeople && totalPositiveBorrowPeople == that.totalPositiveBorrowPeople && totalNegativeDuePeople == that.totalNegativeDuePeople && totalNegativeBorrowPeople == that.totalNegativeBorrowPeople && totalAccounts == that.totalAccounts && totalPeople == that.totalPeople && Objects.equals(totalPositiveBalance, that.totalPositiveBalance) && Objects.equals(totalNegativeBalance, that.totalNegativeBalance) && Objects.equals(totalPositiveDue, that.totalPositiveDue) && Objects.equals(totalPositiveBorrow, that.totalPositiveBorrow) && Objects.equals(totalNegativeDue, that.totalNegativeDue) && Objects.equals(totalNegativeBorrow, that.totalNegativeBorrow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalPositiveBalance, totalPositiveAccounts, totalNegativeBalance, totalNegativeAccounts, totalPositiveDuePeople, totalPositiveBorrowPeople, totalNegativeDuePeople, totalNegativeBorrowPeople, totalPositiveDue, totalPositiveBorrow, totalNegativeDue, totalNegativeBorrow, totalAccounts, totalPeople);
    }
}
