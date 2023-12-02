package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.model.AccountModel;

@SuppressWarnings("unused")
public class InputAccountViewModel extends DBViewModel {

    public static final int OPERATION_SAVE_ACCOUNT = 2;

    private LiveData<AccountModel> mAccount;

    public InputAccountViewModel(@NonNull Application application) {
        super(application);
    }

    @NonNull
    private AccountDao getAccountDao() {
        return getExpenseDatabase().getAccountDao();
    }

    @NonNull
    public LiveData<AsyncQueryResult> saveAccount(@NonNull Account account) {
        return getOrCreate(OPERATION_SAVE_ACCOUNT,()->{
            boolean existing = account.getId() > 0;
            if (existing) {
                return getAccountDao().updateAccount(account);
            }
            else {
                long id = getAccountDao().addAccount(account);
                account.setId(id);
                return account;
            }
        });
    }

    @NonNull
    public LiveData<AccountModel> getAccountById(long id) {
        if (null == mAccount) {
            mAccount = getAccountDao().getAccountByIdLive(id);
        }
        return mAccount;
    }
}
