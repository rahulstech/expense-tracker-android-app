package dreammaker.android.expensetracker.database.view;

import java.time.LocalDate;
import java.util.Objects;

import androidx.room.DatabaseView;
import dreammaker.android.expensetracker.database.type.Currency;

@DatabaseView(viewName = "daily_expense_view",
        value = "SELECT DATE(`when`) AS `date`,SUM(`amount`) AS `amount` FROM `transaction_histories` GROUP BY DATE(`when`) ORDER BY `when`")
@SuppressWarnings("unused")
public class DailyExpenseView {

    private LocalDate date;

    private Currency amount;

    public DailyExpenseView(LocalDate date, Currency amount) {
        this.date = date;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Currency getAmount() {
        return amount;
    }

    public void setAmount(Currency amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "DailyExpenseView{" +
                "when=" + date +
                ", amount=" + amount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DailyExpenseView)) return false;
        DailyExpenseView that = (DailyExpenseView) o;
        return Objects.equals(date, that.date) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, amount);
    }
}
