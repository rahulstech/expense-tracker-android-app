package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.concurrent.Task;
import dreammaker.android.expensetracker.concurrent.TaskCallback;
import dreammaker.android.expensetracker.concurrent.TaskMaster;
import dreammaker.android.expensetracker.concurrent.TaskResult;
import dreammaker.android.expensetracker.database.ExpensesDao;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.dao.PeopleDao;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao;
import dreammaker.android.expensetracker.database.model.AccountDisplayModel;
import dreammaker.android.expensetracker.database.model.PersonDisplayModel;
import dreammaker.android.expensetracker.database.model.TransactionHistory;
import dreammaker.android.expensetracker.util.Constants;

public class TransactionInputViewModel extends AndroidViewModel {

    private AccountDao accountDao;

    private PeopleDao peopleDao;

    private ExpensesDao expensesDao;

    private TransactionHistoryDao transactionHistoryDao;

    private TaskMaster taskMaster = new TaskMaster();

    private LiveData<List<AccountDisplayModel>> accountsDisplayLiveData;
    private LiveData<List<PersonDisplayModel>> personDisplayLiveData;

    public TransactionInputViewModel(@NonNull Application application) {
        super(application);
        /*ExpensesDatabase db = ExpensesDatabase.getInstance(application);
        accountDao = db.getAccountDao();
        peopleDao = db.getPeopleDao();
        expensesDao = db.getDao();
        transactionHistoryDao = db.getTransactionHistoryDao();*/
    }

    @NonNull
    public TaskMaster getTaskMaster() {
        return taskMaster;
    }

    @NonNull
    public LiveData<List<AccountDisplayModel>> getAccountsDisplayLiveData() {
        if (null == accountsDisplayLiveData) {
            //accountsDisplayLiveData = accountDao.getAllAccountsForDisplay();
        }
        return accountsDisplayLiveData;
    }

    @NonNull
    public LiveData<List<PersonDisplayModel>> getPeopleDisplayLiveData() {
        if (null == personDisplayLiveData) {
            //personDisplayLiveData = peopleDao.getAllPeopleForDisplay();
        }
        return personDisplayLiveData;
    }

    public void saveTransactionHistory(@NonNull String key, @NonNull TransactionHistory transaction, @Nullable TaskCallback callback) {
        Objects.requireNonNull(transaction,"transaction history == null");
        Task task = new Task() {
            @NonNull
            @Override
            protected TaskResult execute() {
                TaskResult result = new TaskResult(Constants.DB_INSERT);
                result.parameter = transaction;
                try {
                    //long tid = transactionHistoryDao.insert(transaction);
                    //result.result = tid;
                    //result.successful = tid > 0;
                }
                catch (Exception ex) {
                    result.successful = false;
                    result.error = ex;
                }
                return result;
            }
        };
        if (null != callback) {
            task.addCallback(callback);
        }
        taskMaster.execute(key, task);
    }
}
