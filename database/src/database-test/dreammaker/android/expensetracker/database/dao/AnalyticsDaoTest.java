package dreammaker.android.expensetracker.database.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.math.RoundingMode;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.FakeData;
import dreammaker.android.expensetracker.database.model.AssetLiabilitySummary;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AnalyticsDaoTest {

    ExpensesDatabase db;
    AnalyticsDao dao;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void createDB() {
        db = ExpensesDatabase.getTestInstance(ApplicationProvider.getApplicationContext(), new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                new FakeData().addFakeData_v7(db);
            }
        });
        dao = db.getAnalyticsDao();
    }

    @After
    public void closeDB() {
        db.close();
    }

    @Test
    public void testGetTotalAssetLiability() {
        LiveData<AssetLiabilitySummary> liveData = dao.getTotalAssetLiability();
        liveData.observeForever(summery ->{
            AssetLiabilitySummary original = liveData.getValue();
            AssetLiabilitySummary expected = new AssetLiabilitySummary();
            expected.setTotalPositiveBalance(BigDecimal.valueOf(28500).setScale(2, RoundingMode.HALF_DOWN));
            expected.setTotalNegativeBalance(BigDecimal.valueOf(2600).setScale(2, RoundingMode.HALF_DOWN));
            expected.setTotalPositiveDue(BigDecimal.valueOf(200).setScale(2, RoundingMode.HALF_DOWN));
            expected.setTotalNegativeDue(BigDecimal.valueOf(9000).setScale(2, RoundingMode.HALF_DOWN));
            expected.setTotalPositiveBorrow(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_DOWN));
            expected.setTotalNegativeBorrow(BigDecimal.valueOf(150).setScale(2, RoundingMode.HALF_DOWN));

            assertEquals(expected,original);
        });

    }
}
