package dreammaker.android.expensetracker.database.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dreammaker.android.expensetracker.database.FakeData;
import java.math.BigDecimal;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.model.Account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AccountDaoTest {

    ExpensesDatabase db;

    AccountDao dao;

    @Before
    public void createDb() {
        db = ExpensesDatabase.getTestInstance(ApplicationProvider.getApplicationContext(), new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                new FakeData().addFakeData_v7(db);
            }
        });
        dao = db.getAccountDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testAddBalance() {
        boolean org_result = dao.addBalance(1,BigDecimal.valueOf(500));
        assertTrue("addBalance",org_result);

        Account expected_account = new Account(1,"acc 1", BigDecimal.valueOf(500));
        Account original_account = dao.getAccountById(1);
        assertEquals("account balance not updated after addBalance", expected_account,original_account);

    }

    @Test
    public void testReduceBalance() {
        boolean org_result = dao.reduceBalance(2,BigDecimal.valueOf(500));
        assertTrue("reduceBalance",org_result);

        Account expected_account = new Account(2,"acc 2", BigDecimal.valueOf(3000));
        Account original_account = dao.getAccountById(2);
        assertEquals("account balance not updated after reduceBalance", expected_account,original_account);
    }

    @Test
    public void testDeleteMultiple() {
        int org_result = dao.deleteMultiple(Arrays.asList(1l));
        assertEquals("deleteMultiple",1,org_result);

        Account org_account = dao.getAccountById(1);
        assertNull("account found after delete",org_account);
    }
}
