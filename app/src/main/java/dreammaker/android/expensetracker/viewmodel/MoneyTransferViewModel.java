package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;
import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.database.MoneyTransfer;
import dreammaker.android.expensetracker.database.MoneyTransferDetails;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.ResultCallback;

import static dreammaker.android.expensetracker.util.Helper.ACTION_DELETE;
import static dreammaker.android.expensetracker.util.Helper.ACTION_EDIT;
import static dreammaker.android.expensetracker.util.Helper.ACTION_INSERT;

public class MoneyTransferViewModel extends BaseViewModel {

    private static final String TAG = "MoneyTransferViewModel";

    private LiveData<List<MoneyTransferDetails>> moneyTransferHistories;
    private MutableLiveData<MoneyTransfer> selectedMoneyTransfer;
    private LiveData<List<Account>> accounts;

    public MoneyTransferViewModel(@NonNull Application application) {
        super(application);
        moneyTransferHistories = getDao().getMoneyTransferHistory();
        selectedMoneyTransfer = new MutableLiveData<>();
        accounts = getDao().getAllAccounts();
        setSelectedMoneyTransfer(null);
    }

    public MutableLiveData<MoneyTransfer> getSelectedMoneyTransferLiveData() {
        return selectedMoneyTransfer;
    }

    public MoneyTransfer getSelectedMoneyTransfer() {
        return selectedMoneyTransfer.getValue();
    }

    public void setSelectedMoneyTransfer(MoneyTransfer mt) {
        selectedMoneyTransfer.setValue(null == mt ? new MoneyTransfer() : mt.clone());
    }

    public LiveData<List<Account>> getAccounts() {
        return accounts;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    ///                                       CRUD Methods                                        ///
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void hasAdequateAccounts(@NonNull ResultCallback<Boolean> callback) {
        AppExecutor.getDiskOperationsExecutor().execute(() -> {
            boolean hasAdequateAccounts = false;
            try {
                hasAdequateAccounts = getDao().countAccounts() > 1;
            }
            catch (Exception e) {
                Log.e(TAG, "Error occurred during countAccounts operation: "+e.getMessage());
            }
            finally {
                final boolean result = hasAdequateAccounts;
                AppExecutor.getMainThreadExecutor().execute(() -> callback.onResult(result));
            }
        });
    }

    public void insertMoneyTransfer(final MoneyTransfer mt){
        AppExecutor.getDiskOperationsExecutor().execute(() -> {
            boolean success = false;
            try{
                success = getDao().insertMoneyTransfer(mt) > 0;
            }
            catch (Exception e){
                Log.e(TAG, "error on inserting money transfer with message: "+e.getMessage());
            }
            finally {
                notifyOperationCallback(ACTION_INSERT, success);
            }
        });
    }

    public LiveData<List<MoneyTransferDetails>> getMoneyTransferHistories() {
        return moneyTransferHistories;
    }

    public void updateMoneyTransfer(final MoneyTransfer mt){
        AppExecutor.getDiskOperationsExecutor().execute(() -> {
            boolean success = false;
            try {
                success = getDao().updateMoneyTransfer(mt) > 0;
            }
            catch (Exception e){
                Log.e(TAG, "error on updating money transfer with message: "+e.getMessage());
            }
            finally {
                notifyOperationCallback(ACTION_EDIT, success);
            }
        });
    }

    public void deleteMoneyTransfer(final MoneyTransfer mt){
        if (Check.isNonNull(mt)) {
            AppExecutor.getDiskOperationsExecutor().execute(() -> {
                int changes = 0;
                try {
                    changes = getDao().deleteMoneyTransfer(mt);
                } catch (Exception e) {
                    Log.e(TAG, "error on delete money transfer(s) with message: "+e.getMessage());
                } finally {
                    notifyOperationCallback(ACTION_DELETE, changes > 0, changes);
                }
            });
        }
    }
}
