package dreammaker.android.expensetracker.database;

import android.content.Context;
import android.database.Cursor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.test.core.app.ApplicationProvider;
import dreammaker.android.expensetracker.util.Date;

import static org.junit.Assert.*;

public class TransactionsDaoTest {

    ExpensesDatabase db;
    TransactionsDao dao;

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        db = FakeValueProvider.createExpenseDbAndFakeDate(context);
        dao = db.getTransactionDao();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void getAccountTransactionsBetween() {
        Date start = Date.of(2021,0,3);
        Date end = Date.of(2021,0,6);

        List<Transaction> actual = dao.getAccountTransactionsBetween(1,start,end);

        assertNotNull(actual);
        assertEquals(4,actual.size());
    }

    @Test
    public void getPersonTransactionsBetween() {
        Date start = Date.of(2021,0,3);
        Date end = Date.of(2021,0,6);

        List<Transaction> actual = dao.getPersonTransactionsBetween(1,start,end);

        assertNotNull(actual);
        assertEquals(5,actual.size());
    }
}