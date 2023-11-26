package dreammaker.android.expensetracker.database.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.util.DatabaseUtil;

import static dreammaker.android.expensetracker.database.util.DatabaseUtil.createAccount;
import static dreammaker.android.expensetracker.database.util.DatabaseUtil.createAccountModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AccountDaoTest {

    ExpensesDatabase db;

    AccountDao dao;

    @Before
    public void openDb(){
        db = DatabaseUtil.createInMemoryExpenseDatabase(7);
        dao = db.getAccountDao();
    }

    @After
    public void closeDb() {
        DatabaseUtil.closeDBSilently(db);
    }

    @Test
    public void addAccount() {
        Account account = createAccount(0L,"New Account", Currency.ZERO);
        long id = dao.addAccount(account);
        assertTrue(id>0);
    }

    @Test
    public void updateAccount() {
        Account account = createAccount(1L,"acc 1 updated",Currency.ZERO);
        int changes = dao.updateAccount(account);
        assertEquals(1,changes);
    }

    @Test
    public void getAllAccountsLive() throws Exception {
        LiveData<List<AccountModel>> liveData = dao.getAllAccountsLive();
        List<AccountModel> actual = DatabaseUtil.getValueFromLivedata(liveData,5000);

        AccountModel account1 = createAccountModel(1L,"acc 1",Currency.valueOf("-100"));
        AccountModel account2 = createAccountModel(2L,"acc 2",Currency.valueOf("3500"));
        AccountModel account3 = createAccountModel(3L,"acc 3",Currency.valueOf("25000"));
        List<AccountModel> expected = Arrays.asList(account1,account2,account3);

        assertEquals(expected,actual);
    }

    @Test
    public void getAllAccountsWithUsageCountLive() throws Exception {
        LiveData<List<AccountModel>> liveData = dao.getAllAccountWithUsageCountLive();
        List<AccountModel> actual = DatabaseUtil.getValueFromLivedata(liveData,5000);

        AccountModel account1 = createAccountModel(1L,"acc 1",Currency.valueOf("-100"),8);
        AccountModel account2 = createAccountModel(2L,"acc 2",Currency.valueOf("3500"),3);
        AccountModel account3 = createAccountModel(3L,"acc 3",Currency.valueOf("25000"),5);
        List<AccountModel> expected = Arrays.asList(account1,account2,account3);

        assertEquals(expected,actual);
    }

    @Test
    public void findAccountById() {
        Account expected = createAccount(1L,"acc 1",Currency.valueOf("-100"));
        Account actual = dao.findAccountById(1);
        assertEquals(expected,actual);
    }

    @Test
    public void removeAccounts() {
        int changes = dao.removeAccounts(new long[]{1,3});
        assertEquals(2,changes);
    }
}
