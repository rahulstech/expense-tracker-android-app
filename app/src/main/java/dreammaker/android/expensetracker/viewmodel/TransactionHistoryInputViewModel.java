package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.dao.PersonDao;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.model.PersonModel;

public class TransactionHistoryInputViewModel extends DBViewModel {

    private LiveData<List<AccountModel>> mAccounts;

    private LiveData<List<PersonModel>> mPeople;

    public TransactionHistoryInputViewModel(@NonNull Application application) {
        super(application);
    }

    @NonNull
    private AccountDao getAccountDao() {
        return getExpenseDatabase().getAccountDao();
    }

    @NonNull
    private PersonDao getPersonDao() {
        return getExpenseDatabase().getPersonDao();
    }


    @NonNull
    private TransactionHistoryDao getTransactionHistoryDao() {
        return getTransactionHistoryDao();
    }

    @NonNull
    public LiveData<List<AccountModel>> getAllAccountsWithUsageCountLive() {
        if (null == mAccounts) {
            mAccounts = getAccountDao().getAllAccountWithUsageCountLive();
        }
        return mAccounts;
    }

    @NonNull
    public LiveData<List<PersonModel>> getAllPeopleWithUsageCountLive() {
        if (null == mPeople) {
            mPeople = getPersonDao().getAllPeopleWithUsageCountLive();
        }
        return mPeople;
    }
}
