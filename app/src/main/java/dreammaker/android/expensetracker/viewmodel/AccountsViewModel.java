package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import dreammaker.android.expensetracker.concurrent.Task;
import dreammaker.android.expensetracker.concurrent.TaskCallback;
import dreammaker.android.expensetracker.concurrent.TaskMaster;
import dreammaker.android.expensetracker.concurrent.TaskResult;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.model.Account;
import dreammaker.android.expensetracker.database.model.AccountDisplayModel;
import dreammaker.android.expensetracker.util.Constants;

public class AccountsViewModel extends AndroidViewModel {

    private static final String TAG = "AccountsViewModel";

    private AccountDao accountDao;
    private MutableLiveData<Account> selectedAccountLiveData;
    private LiveData<List<AccountDisplayModel>> accountDisplayLiveData = null;

    private MutableLiveData<Long> accountIdLiveData = new MutableLiveData<>();
    private LiveData<Account> accountLiveData = null;

    private TaskMaster taskMaster = new TaskMaster();

    public AccountsViewModel(@NonNull Application application) {
        super(application);
        ExpensesDatabase db = ExpensesDatabase.getInstance(application);
        accountDao = db.getAccountDao();
        selectedAccountLiveData = new MutableLiveData<>();
        accountLiveData = Transformations.switchMap(accountIdLiveData,id -> accountDao.findAccountById(id));
    }

    public TaskMaster getTaskMaster() {
        return taskMaster;
    }

    public LiveData<Account> getSelectedAccountLiveData() {
        return selectedAccountLiveData;
    }

    public Account getSelectedAccount() { return selectedAccountLiveData.getValue(); }

    public void saveAccount(@NonNull String key, @NonNull Account account, @Nullable TaskCallback callback) {
        Objects.requireNonNull(account,"account == null");
        if (account.getId() > 0) {
            editAccount(key, account, callback);
        }
        else {
            createAccount(key,account,callback);
        }
    }

    public void createAccount(@NonNull String key, @NonNull Account account, @NonNull TaskCallback callback) {
        Task task = new Task() {
            @NonNull
            @Override
            protected TaskResult execute() {
                TaskResult result = new TaskResult(Constants.DB_INSERT);
                result.parameter = account;
                try {
                    long id = accountDao.insert(account);
                    result.result = id;
                    result.successful = id > 0;
                }
                catch (Exception ex) {
                    result.error = ex;
                    result.successful = false;
                }
                return result;
            }
        };
        task.addCallback(callback);
        taskMaster.execute(key,task);
    }

    public void editAccount(@NonNull String key, @NonNull Account account, @NonNull TaskCallback callback) {
        Task task = new Task() {
            @NonNull
            @Override
            protected TaskResult execute() {
                TaskResult result = new TaskResult(Constants.DB_UPDATE);
                result.parameter = account;
                try {
                    int changes = accountDao.update(account);
                    result.result = changes;
                    result.successful = changes == 1;
                }
                catch (Exception ex) {
                    result.error = ex;
                    result.successful = false;
                }
                return result;
            }
        };
        task.addCallback(callback);
        taskMaster.execute(key,task);
    }

    public void findAccountById(@NonNull String key, long id, @NonNull TaskCallback callback) {
        Task task = new Task() {
            @NonNull
            @Override
            protected TaskResult execute() {
                TaskResult result = new TaskResult(Constants.DB_QUERY);
                result.parameter = id;
                try {
                    Account account = accountDao.getAccountById(id);
                    result.result = account;
                    result.successful = true;
                }
                catch (Exception ex) {
                    result.error = ex;
                    result.successful = false;
                }
                return result;
            }
        };
        task.addCallback(callback);
        taskMaster.execute(key,task);
    }

    public LiveData<List<AccountDisplayModel>> getAllAccountsForDisplay() {
        if (null == accountDisplayLiveData) {
            accountDisplayLiveData = accountDao.getAllAccountsForDisplay();
        }
        return accountDisplayLiveData;
    }

    public LiveData<Account> getAccountById(long id) {
        accountIdLiveData.postValue(id);
        return accountLiveData;
    }

    public void removeMultipleAccounts(@NonNull String key, @NonNull List<Long> ids, @NonNull TaskCallback callback) {
        Task task = new Task() {
            @NonNull
            @Override
            protected TaskResult execute() {
                TaskResult result = new TaskResult(Constants.DB_DELETE);
                result.parameter = ids;
                try {
                    int changes = accountDao.deleteMultiple(ids);
                    result.result = changes;
                    result.successful = changes == ids.size();
                }
                catch (Exception ex) {
                    result.error = ex;
                    result.successful = false;
                }
                return result;
            }
        };
        task.addCallback(callback);
        taskMaster.execute(key,task);
    }
}
