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
import dreammaker.android.expensetracker.database.model.Account;
import dreammaker.android.expensetracker.database.model.Person;
import dreammaker.android.expensetracker.database.model.TransactionHistory;
import dreammaker.android.expensetracker.database.type.Date;
import dreammaker.android.expensetracker.database.type.TransactionType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TransactionHistoryDaoTest {

    ExpensesDatabase db;
    AccountDao accountDao;
    PeopleDao peopleDao;
    TransactionHistoryDao transactionDao;

    @Before
    public void createDB() {
        db = ExpensesDatabase.getTestInstance(ApplicationProvider.getApplicationContext(), new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                new FakeData().addFakeData_v7(db);
            }
        });
        accountDao = db.getAccountDao();
        peopleDao = db.getPeopleDao();
        transactionDao = db.getTransactionHistoryDao();
    }

    @After
    public void closeDB() {
        db.close();
    }

    @Test
    public void testAddDue() {
        TransactionHistory transaction = new TransactionHistory(0,1l,null,2l,null, BigDecimal.valueOf(500), TransactionType.DUE, Date.today(),null);
        long tid = transactionDao.insert(transaction);
        assertTrue("insert DUE",tid > 0);

        Account expected_account = new Account(2,"acc 2", BigDecimal.valueOf(3000));
        Person expected_person = new Person(1,"person 1",null,BigDecimal.valueOf(700),BigDecimal.ZERO);
        Account original_account = accountDao.getAccountById(2);
        Person original_person = peopleDao.getPersonById(1);
        assertEquals("insert did not updated account balance properly",expected_account,original_account);
        assertEquals("insert did not updated person due properly",expected_person,original_person);
    }

    @Test
    public void testPayBorrow() {
        TransactionHistory transaction = new TransactionHistory(0,2l,null,2l,null, BigDecimal.valueOf(500), TransactionType.PAY_BORROW, Date.today(),null);
        long tid = transactionDao.insert(transaction);
        assertTrue("insert PAY_BORROW",tid > 0);

        Account expected_account = new Account(2,"acc 2", BigDecimal.valueOf(3000));
        Person expected_person = new Person(2,"person 2",null,BigDecimal.ZERO,BigDecimal.valueOf(500));
        Account original_account = accountDao.getAccountById(2);
        Person original_person = peopleDao.getPersonById(2);
        assertEquals("insert did not updated account balance properly",expected_account,original_account);
        assertEquals("insert did not updated person due properly",expected_person,original_person);
    }

    @Test
    public void testDeleteBorrow() {
        TransactionHistory transaction = new TransactionHistory(11,null,3l,null,1l,BigDecimal.valueOf(500),TransactionType.BORROW,Date.valueOf("2021-09-06"),null);
        boolean org_result = transactionDao.delete(transaction);
        assertTrue("delete BORROW",org_result);

        Account expected_account = new Account(1,"acc 1",BigDecimal.valueOf(-500));
        Person expected_person = new Person(3,"person 3 FN","person 3 LN",BigDecimal.ZERO,BigDecimal.valueOf(-500));
        Account org_account = accountDao.getAccountById(1);
        Person org_person = peopleDao.getPersonById(3);
        assertEquals("account balance not updated after deletion of BORROW transaction",expected_account,org_account);
        assertEquals("person borrow not updated after deletion of BORROW transaction",expected_person,org_person);
    }

    @Test
    public void testDeleteMoneyTransfer() {
        TransactionHistory transaction = new TransactionHistory(13,null,null,3l,2l,BigDecimal.valueOf(500),TransactionType.MONEY_TRANSFER,Date.valueOf("2021-07-15"),null);
        boolean org_result = transactionDao.delete(transaction);
        assertTrue("delete MONEY_TRANSFER",org_result);

        Account expected_payer_account = new Account(3,"acc 3",BigDecimal.valueOf(25500));
        Account expected_payee_account = new Account(2,"acc 2",BigDecimal.valueOf(3000));
        Account org_payer_account = accountDao.getAccountById(3);
        Account org_payee_account = accountDao.getAccountById(2);
        assertEquals("payer account balance not updated after deletion of MONEY_TRANSFER transaction",expected_payer_account,org_payer_account);
        assertEquals("payee account balance not updated after deletion of MONEY_TRANSFER transaction",expected_payee_account,org_payee_account);
    }

    @Test
    public void testDeleteMultiple() {
        int org_result = transactionDao.deleteMultiple(Arrays.asList(1l));
        assertEquals("deleteMultiple",1,org_result);

        TransactionHistory org_transaction = transactionDao.getTransactionHistoryById(1);
        assertNull("transaction found after delete",org_transaction);
    }
}
