package dreammaker.android.expensetracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryDao

class HomeViewModel(app: Application): AndroidViewModel(app) {

    private val historyDao: HistoryDao
    private val accountDao: AccountDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
        accountDao = db.accountDao
    }

    private lateinit var recentlyUsedAccountsLiveData: LiveData<List<AccountModel>>
    private lateinit var recentlyUsedGroupsLiveData: LiveData<List<GroupModel>>
    private lateinit var totalBalance: LiveData<Double?>

    fun getRecentlyUsedThreeAccounts(): LiveData<List<AccountModel>> {
        if (!::recentlyUsedAccountsLiveData.isInitialized) {
            recentlyUsedAccountsLiveData = historyDao.getLatestUsedThreeAccounts()
        }
        return recentlyUsedAccountsLiveData
    }

    fun getRecentlyUsedThreeGroups(): LiveData<List<GroupModel>> {
        if (!::recentlyUsedGroupsLiveData.isInitialized) {
            recentlyUsedGroupsLiveData = historyDao.getLatestUsedThreeGroups()
        }
        return recentlyUsedGroupsLiveData
    }

    fun getTotalBalance(): LiveData<Double?> {
        if (!::totalBalance.isInitialized) {
            totalBalance = accountDao.getTotalBalance()
        }
        return totalBalance
    }
}