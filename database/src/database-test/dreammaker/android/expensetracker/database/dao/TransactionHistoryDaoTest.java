package dreammaker.android.expensetracker.database.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.database.util.DatabaseUtil;

import static dreammaker.android.expensetracker.database.util.DatabaseUtil.createTransactionHistory;
import static dreammaker.android.expensetracker.database.util.DatabaseUtil.createTransactionHistoryMode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TransactionHistoryDaoTest {

    ExpensesDatabase db;

    TransactionHistoryDao dao;

    @Before
    public void openDb() {
        db = DatabaseUtil.createInMemoryExpenseDatabase(7);
        dao = db.getTransactionHistoryDao();
    }

    @After
    public void closeDb() {
        DatabaseUtil.closeDBSilently(db);
    }

    @Test
    public void addTransactionHistory() {
        TransactionHistory history = createTransactionHistory(0L, TransactionType.DUE, Currency.valueOf("100"), LocalDate.now(),null,3L,1L,null,null);
        long id = dao.addTransactionHistory(history);
        assertTrue(id>0);
    }

    @Test
    public void updateTransactionHistory() {
        // (id,payeeAccountId,amount,type,`when`) => (8,3,\"200\",\"INCOME\",\"2021-08-05\")
        TransactionHistory history = createTransactionHistory(8L, TransactionType.INCOME, Currency.valueOf("150"), LocalDate.of(2021,8,5),3L,null,null,null,null);
        int changes = dao.updateTransactionHistory(history);
        assertEquals(1,changes);
    }

    @Test
    public void findTransactionById() {
        // (id,payerAccountId,payeePersonId,amount,type,`when`) => (1,1,1,\"200\",\"DUE\",\"2021-01-03\")
        TransactionHistory expected = createTransactionHistory(1L, TransactionType.DUE, Currency.valueOf("200"), LocalDate.of(2021,1,3),null,1L,1L,null,null);
        TransactionHistory actual = dao.findTransactionHistoryById(1);
        assertEquals(expected,actual);
    }

    @Test
    public void getAllTransactionHistoriesForDateLive() throws Exception {
        /*
         (id,payeeAccountId,payerPersonId,amount,type,`when`) => (3,2,1,\"355\",\"PAY_DUE\",\"2021-01-06\")

         (id,payerAccountId,payeePersonId,amount,type,`when`) => (6,1,2,\"156\",\"DUE\",\"2021-01-06\")
         */
        LiveData<List<TransactionHistoryModel>> liveData = dao.getAllTransactionHistoriesForDateLive(LocalDate.of(2021,1,6));
        List<TransactionHistoryModel> actual = DatabaseUtil.getValueFromLivedata(liveData,5000);

        TransactionHistoryModel history1 = createTransactionHistoryMode(3L,TransactionType.PAY_DUE,Currency.valueOf("355"),LocalDate.of(2021,1,6),2L,null,null,1L,null);
        TransactionHistoryModel history2 = createTransactionHistoryMode(6L,TransactionType.DUE,Currency.valueOf("156"),LocalDate.of(2021,1,6),null,1L,2L,null,null);
        List<TransactionHistoryModel> expected = Arrays.asList(history1,history2);

        assertEquals("expected list of transaction-histories not fetched",expected,actual);
        assertNotNull("account-model not fetched",actual.get(0).getPayeeAccount());
        assertNotNull("person-model not fetched",actual.get(0).getPayerPerson());
    }

    @Test
    public void getAllTransactionHistoriesForAccountBetweenLive() throws Exception {
        /*
        (id,payerAccountId,payeePersonId,amount,type,`when`) => (5,3,1,\"700\",\"DUE\",\"2021-01-05\");")

        (id,payerAccountId,payeeAccountId,amount,type,`when`) => (13,3,2,\"500\",\"MONEY_TRANSFER\",\"2021-07-15\");")
         */
        LiveData<List<TransactionHistoryModel>> liveData = dao.getAllTransactionHistoriesForAccountsBetweenLive(3,LocalDate.of(2021,1,5),LocalDate.of(2021,7,30));
        List<TransactionHistoryModel> actual = DatabaseUtil.getValueFromLivedata(liveData,5000);

        TransactionHistoryModel history1 = createTransactionHistoryMode(13L,TransactionType.MONEY_TRANSFER,Currency.valueOf("500"),LocalDate.of(2021,7,15),2L,3L,null,null,null);
        TransactionHistoryModel history2 = createTransactionHistoryMode(5L,TransactionType.DUE,Currency.valueOf("700"),LocalDate.of(2021,1,5),null,3L,1L,null,null);
        List<TransactionHistoryModel> expected = Arrays.asList(history1,history2);

        assertEquals(expected,actual);
    }

    @Test
    public void getAllTransactionHistoriesForPersonBetweenLive() throws Exception {
        /*
        (id,payerAccountId,payeePersonId,amount,type,`when`) => (6,1,2,\"156\",\"DUE\",\"2021-01-06\")

        (id,payeeAccountId,payerPersonId,amount,type,`when`) => (9,1,2,\"800.5\",\"PAY_DUE\",\"2021-01-03\")
         */
        LiveData<List<TransactionHistoryModel>> liveData = dao.getAllTransactionHistoriesForPeopleBetweenLive(2,LocalDate.of(2021,1,1),LocalDate.of(2021,1,31));
        List<TransactionHistoryModel> actual = DatabaseUtil.getValueFromLivedata(liveData,5000);

        TransactionHistoryModel history1 = createTransactionHistoryMode(6L,TransactionType.DUE,Currency.valueOf("156"),LocalDate.of(2021,1,6),null,1L,2L,null,null);
        TransactionHistoryModel history2 = createTransactionHistoryMode(9L,TransactionType.PAY_DUE,Currency.valueOf("800.5"),LocalDate.of(2021,1,3),1L,null,null,2L,null);
        List<TransactionHistoryModel> expected = Arrays.asList(history1,history2);

        assertEquals(expected,actual);
    }

    @Test
    public void removeTransactionHistory() {
        TransactionHistory history = createTransactionHistory(1L, TransactionType.DUE, Currency.valueOf("200"), LocalDate.of(2021,1,3),null,1L,1L,null,null);
        int changes = dao.removeTransactionHistory(history);
        assertEquals(1,changes);
    }
}
