package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.model.AccountModel;

@SuppressWarnings("unused")
public class AccountViewModel extends DBViewModel {

    public static final int SAVE_ACCOUNT = 2;

    public static final int DELETE_ACCOUNTS = 3;

    private LiveData<List<AccountModel>> mAccounts;

    private LiveData<AccountModel> mAccount;

    public AccountViewModel(@NonNull Application application) {
        super(application);
    }

    private AccountDao getAccountDao() {
        return getExpenseDatabase().getAccountDao();
    }

    @NonNull
    public LiveData<List<AccountModel>> getAllAccounts() {
        if (null == mAccounts) {
            mAccounts = getAccountDao().getAllAccountsLive();
        }
        return mAccounts;
    }

    @NonNull
    public LiveData<AccountModel> getAccountById(long id) {
        if (null == mAccount) {
            mAccount = getAccountDao().getAccountByIdLive(id);
        }
        return mAccount;
    }

    @NonNull
    public LiveData<List<AccountModel>> getAllAccountsWithUsageCount() {
        if (null == mAccounts) {
            mAccounts = getAccountDao().getAllAccountWithUsageCountLive();
        }
        return mAccounts;
    }

    @NonNull
    public LiveData<AsyncQueryResult> saveAccount(@NonNull Account account) {
        return execute(SAVE_ACCOUNT,()->{
            boolean existing = account.getId() > 0;
            if (existing) {
                if (getAccountDao().updateAccount(account) != 1) {
                    return null;
                }
            }
            else {
                long id = getAccountDao().addAccount(account);
                if (id <= 0) {
                    return null;
                }
                account.setId(id);
            }
            return account;
        });
    }

    @NonNull
    public LiveData<AsyncQueryResult> removeAccounts(final long[] ids) {
        Objects.requireNonNull(ids,"array of account ids is null");
        return execute(DELETE_ACCOUNTS,()->{
            if (ids.length == 0) {
                return 0;
            }
            return getAccountDao().removeAccounts(ids);
        });
    }
}
