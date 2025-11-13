package dreammaker.android.expensetracker.database;

import java.util.concurrent.Callable;

import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.dao.GroupDao;
import dreammaker.android.expensetracker.database.dao.HistoryDao;

public interface IExpenseDatabase {

    void runInTransaction(Runnable task);

    <V> V runInTransaction(Callable<V> task);

    HistoryDao getHistoryDao();

    AccountDao getAccountDao();

    GroupDao getGroupDao();
}
