package dreammaker.android.expensetracker.database.view;

import androidx.room.DatabaseView;
import dreammaker.android.expensetracker.database.type.Currency;

@DatabaseView(value = "SELECT "+
        " SUM(CASE WHEN CAST(`due` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveDuePeople`,"+
        " SUM(CASE WHEN CAST(`due` AS REAL) > 0 THEN `due` ELSE 0 END) AS `totalPositiveDue`,"+
        " SUM(CASE WHEN CAST(`due` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeDuePeople`,"+
        " SUM(CASE WHEN CAST(`due` AS REAL) < 0 THEN `due` ELSE 0 END) AS `totalNegativeDue`,"+
        " SUM(CASE WHEN CAST(`borrow` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveBorrowPeople`,"+
        " SUM(CASE WHEN CAST(`borrow` AS REAL) > 0 THEN `borrow` ELSE 0 END) AS `totalPositiveBorrow`, "+
        " SUM(CASE WHEN CAST(`borrow` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeBorrowPeople`, "+
        " SUM(CASE WHEN CAST(`borrow` AS REAL) < 0 THEN `borrow` ELSE 0 END) AS `totalNegativeBorrow`"+
        " FROM `people`", viewName = "people_summary_view")
@SuppressWarnings("unused")
public class PeopleSummaryView {

    private int totalPositiveDuePeople;

    private int totalPositiveBorrowPeople;

    private int totalNegativeDuePeople;

    private int totalNegativeBorrowPeople;

    private Currency totalPositiveDue;

    private Currency totalPositiveBorrow;

    private Currency totalNegativeDue;

    private Currency totalNegativeBorrow;

    public PeopleSummaryView() {}

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
        return totalPositiveDue;
    }

    public void setTotalPositiveDue(Currency totalPositiveDue) {
        this.totalPositiveDue = totalPositiveDue;
    }

    public Currency getTotalPositiveBorrow() {
        return totalPositiveBorrow;
    }

    public void setTotalPositiveBorrow(Currency totalPositiveBorrow) {
        this.totalPositiveBorrow = totalPositiveBorrow;
    }

    public Currency getTotalNegativeDue() {
        return totalNegativeDue;
    }

    public void setTotalNegativeDue(Currency totalNegativeDue) {
        this.totalNegativeDue = totalNegativeDue;
    }

    public Currency getTotalNegativeBorrow() {
        return totalNegativeBorrow;
    }

    public void setTotalNegativeBorrow(Currency totalNegativeBorrow) {
        this.totalNegativeBorrow = totalNegativeBorrow;
    }
}
