package dreammaker.android.expensetracker.database.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.view.AssetLiabilitySummary;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.util.DatabaseUtil;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AnalyticsDaoTest {

    ExpensesDatabase db;

    AnalyticsDao dao;

    @Before
    public void openDb() {
        db = DatabaseUtil.createInMemoryExpenseDatabase(7);
        dao = db.getAnalyticsDao();
    }

    @After
    public void closeDb() {
        DatabaseUtil.closeDBSilently(db);
    }

    @Test
    public void getTotalAssetLiability() throws Exception {
        LiveData<AssetLiabilitySummary> liveData = dao.getTotalAssetLiability();
        AssetLiabilitySummary actual = DatabaseUtil.getValueFromLivedata(liveData,5000);

        AssetLiabilitySummary expected = new AssetLiabilitySummary();
        expected.setTotalPositiveBalance(Currency.valueOf("28500"));
        expected.setTotalPositiveAccounts(2);
        expected.setTotalNegativeBalance(Currency.valueOf("-100"));
        expected.setTotalNegativeAccounts(1);
        expected.setTotalPositiveDue(Currency.valueOf("200"));
        expected.setTotalPositiveDuePeople(1);
        expected.setTotalNegativeDue(Currency.valueOf("-500"));
        expected.setTotalNegativeDuePeople(1);
        expected.setTotalPositiveBorrow(Currency.valueOf("1000"));
        expected.setTotalPositiveBorrowPeople(1);
        expected.setTotalNegativeBorrow(Currency.valueOf("-100"));
        expected.setTotalNegativeBorrowPeople(1);
        expected.setTotalAccounts(3);
        expected.setTotalPeople(3);

        assertEquals(expected,actual);
    }
}
