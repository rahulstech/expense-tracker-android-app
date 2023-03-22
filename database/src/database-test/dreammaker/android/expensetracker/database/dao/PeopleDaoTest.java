package dreammaker.android.expensetracker.database.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.FakeData;
import dreammaker.android.expensetracker.database.model.Person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PeopleDaoTest {

    ExpensesDatabase db;
    PeopleDao dao;

    @Before
    public void createDB() {
        db = ExpensesDatabase.getTestInstance(ApplicationProvider.getApplicationContext(), new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                new FakeData().addFakeData_v7(db);
            }
        });
        dao = db.getPeopleDao();
    }

    @After
    public void closeDB() {
        db.close();
    }

    @Test
    public void testAddDue() {
        boolean org_result = dao.addDue(1, BigDecimal.ONE);
        assertTrue("addDue",org_result);

        Person expected_person = new Person(1,"person 1",null,BigDecimal.valueOf(201),BigDecimal.ZERO);
        Person original_person = dao.getPersonById(1);
        assertEquals("person due not updated after addDue",expected_person,original_person);
    }

    @Test
    public void testReduceDue() {
        boolean org_result = dao.reduceDue(1, BigDecimal.ONE);
        assertTrue("reduceDue",org_result);

        Person expected_person = new Person(1,"person 1",null,BigDecimal.valueOf(199),BigDecimal.ZERO);
        Person original_person = dao.getPersonById(1);
        assertEquals("person due not updated after reduceDue",expected_person,original_person);
    }

    @Test
    public void testAddBorrow() {
        boolean org_result = dao.addBorrow(2, BigDecimal.ONE);
        assertTrue("addBorrow",org_result);

        Person expected_person = new Person(2,"person 2",null,BigDecimal.ZERO,BigDecimal.valueOf(1001));
        Person original_person = dao.getPersonById(2);
        assertEquals("person borrow not updated after addBorrow",expected_person,original_person);
    }

    @Test
    public void testReduceBorrow() {
        boolean org_result = dao.reduceBorrow(2, BigDecimal.ONE);
        assertTrue("reduceBorrow",org_result);

        Person expected_person = new Person(2,"person 2",null,BigDecimal.ZERO,BigDecimal.valueOf(999));
        Person original_person = dao.getPersonById(2);
        assertEquals("person borrow not updated after reduceBorrow",expected_person,original_person);
    }

    @Test
    public void testDeleteMultiple() {
        int org_result = dao.deleteMultiple(Arrays.asList(1l));
        assertEquals("deleteMultiple)",1,org_result);

        Person org_person = dao.getPersonById(1);
        assertNull("person found after delete",org_person);
    }
}
