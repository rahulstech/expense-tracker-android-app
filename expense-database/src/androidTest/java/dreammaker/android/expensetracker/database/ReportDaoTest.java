package dreammaker.android.expensetracker.database;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dreammaker.android.expensetracker.util.Date;

import static dreammaker.android.expensetracker.database.EntityType.ACCOUNTS;
import static dreammaker.android.expensetracker.database.EntityType.PERSONS;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ReportDaoTest {

    ExpensesDatabase db;
    ReportDao dao;

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        db = FakeValueProvider.createExpenseDbAndFakeDate(context);
        dao = db.getReportDao();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void getDailyReport() {

        Date start = Date.of(2021,0,3);
        Date end = Date.of(2021,0,6);

        List<Report> actual_dailyReportPerson = dao.getDailyReport(start,end,1,PERSONS);

        List<Report> expected_dailyReportPerson = Arrays.asList(
                new Report(Date.of(2021,0,6),Date.of(2021,0,6),2,-700,0,0,1,PERSONS),
                new Report(Date.of(2021,0,5),Date.of(2021,0,5),1,-700,1,500,1,PERSONS),
                new Report(Date.of(2021,0,3),Date.of(2021,0,3),0,0,1,355,1,PERSONS)
        );

        assertEquals("dailyReportPerson",expected_dailyReportPerson,actual_dailyReportPerson);

        List<Report> actual_dailyReportAccount = dao.getDailyReport(start,end,1,ACCOUNTS);

        List<Report> expected_dailyReportAccount = Arrays.asList(
                new Report(Date.of(2021,0,6),Date.of(2021,0,6),1,-156,0,0,1,ACCOUNTS),
                new Report(Date.of(2021,0,5),Date.of(2021,0,5),1,-700,1,500,1,ACCOUNTS),
                new Report(Date.of(2021,0,3),Date.of(2021,0,3),0,0,1,799,1,ACCOUNTS)

        );

        assertEquals("dailyReportAccount",expected_dailyReportAccount,actual_dailyReportAccount);
    }
}