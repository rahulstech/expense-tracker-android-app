package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;
import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import dreammaker.android.expensetracker.database.AboutAccount;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;

import static dreammaker.android.expensetracker.util.Helper.ACTION_DELETE;
import static dreammaker.android.expensetracker.util.Helper.ACTION_EDIT;
import static dreammaker.android.expensetracker.util.Helper.ACTION_INSERT;

public class AccountsViewModel extends BaseViewModel {

    private static final String TAG = "AccountsViewModel";

    private LiveData<List<AboutAccount>> accounts;
    private MutableLiveData<Account> selectedAccountLiveData;

    public AccountsViewModel(@NonNull Application application) {
        super(application);
        accounts = getDao().getAllAccountsForListOfAccounts();
        selectedAccountLiveData = new MutableLiveData<>();
    }

    public void setSelectedAccount(Account account) {
        Check.isNonNull(account, "selected account must be non null");
        selectedAccountLiveData.setValue(account.clone());
    }

    public LiveData<Account> getSelectedAccountLiveData() {
        return selectedAccountLiveData;
    }

    public Account getSelectedAccount() { return selectedAccountLiveData.getValue(); }

    public void insertAccount(final Account account){
        AppExecutor.getDiskOperationsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try{
                    success = getDao().insertAccount(account) > 0;
                }
                catch (Exception e){
                    Log.e(TAG, "error on inserting new account with message: "+e.getMessage());
                }
                finally {
                    notifyOperationCallback(ACTION_INSERT, success);
                }
            }
        });
    }

    public void updateAccount(final Account account){
        AppExecutor.getDiskOperationsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    success = getDao().updateAccount(account) > 0;
                }
                catch (Exception e){
                    Log.e(TAG, "error on updating a account with message: "+e.getMessage());
                }
                finally {
                    notifyOperationCallback(ACTION_EDIT, success);
                }
            }
        });
    }

    public void deleteAccount(final Account... accounts){
        if (Check.isNonNull(accounts)) {
            AppExecutor.getDiskOperationsExecutor().execute(() -> {
                int changes = 0;
                try {
                    changes = getDao().deleteAccounts(accounts);
                } catch (Exception e) {
                    Log.e(TAG, "error on deleting account(s) with message: "+e.getMessage());
                } finally {
                    notifyOperationCallback(ACTION_DELETE, changes > 0, changes);
                }
            });
        }
    }

    public LiveData<List<AboutAccount>> getAccounts() {
        return accounts;
    }
}
