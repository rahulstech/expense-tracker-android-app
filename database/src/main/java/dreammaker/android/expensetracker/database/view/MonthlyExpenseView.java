package dreammaker.android.expensetracker.database.view;

import java.time.LocalDate;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.DatabaseView;
import androidx.room.Ignore;
import dreammaker.android.expensetracker.database.type.Currency;

@DatabaseView(viewName = "monthly_expense_view",
        value = "SELECT DATE(date,'start of month') AS `monthYear`,SUM(`amount`) AS `amount` FROM `daily_expense_view` GROUP BY DATE_FORMAT(`when`,'%m-%Y') ORDER BY date")
@SuppressWarnings("unused")
public class MonthlyExpenseView {

    private LocalDate monthYear;

    private Currency amount;

    public MonthlyExpenseView(LocalDate monthYear, Currency amount) {
        this.monthYear = monthYear;
        this.amount = amount;
    }

    public LocalDate getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(LocalDate monthYear) {
        this.monthYear = monthYear;
    }

    public Currency getAmount() {
        return amount;
    }

    public void setAmount(Currency amount) {
        this.amount = amount;
    }

    /**
     * @return month value in integer in range January = 1, December = 12
     */
    @Ignore
    public int getMonth() {
        return monthYear.getMonthValue();
    }

    @Ignore
    public int getYear() {
        return monthYear.getYear();
    }

    @NonNull
    @Override
    public String toString() {
        return "DailyExpenseView{" +
                "when=" + monthYear +
                ", amount=" + amount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonthlyExpenseView)) return false;
        MonthlyExpenseView that = (MonthlyExpenseView) o;
        return Objects.equals(monthYear, that.monthYear) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monthYear, amount);
    }
}
