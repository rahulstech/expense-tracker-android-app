package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.model.AccountModel;

@SuppressWarnings("unused")
public class AccountsListViewModel extends DBViewModel {

    private LiveData<List<AccountModel>> mAccounts;

    public AccountsListViewModel(@NonNull Application application) {
        super(application);
    }

    @NonNull
    public AccountDao getAccountDao() {
        return getExpenseDatabase().getAccountDao();
    }

    @NonNull
    public LiveData<List<AccountModel>> getAllAccounts() {
        if (null == mAccounts) {
            mAccounts = getAccountDao().getAllAccountsLive();
        }
        return mAccounts;
    }
}
