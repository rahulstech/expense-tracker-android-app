package dreammaker.android.expensetracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.GroupDao
import dreammaker.android.expensetracker.database.GroupModel

class HomeViewModel(app: Application): AndroidViewModel(app) {

    private val accountDao: AccountDao
    private val groupDao: GroupDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        accountDao = db.accountDao
        groupDao = db.groupDao
    }

    private lateinit var recentlyUsedAccountsLiveData: LiveData<List<AccountModel>>
    private lateinit var recentlyUsedGroupsLiveData: LiveData<List<GroupModel>>
    private lateinit var totalBalance: LiveData<Double?>

    fun getRecentlyUsedThreeAccounts(): LiveData<List<AccountModel>> {
        if (!::recentlyUsedAccountsLiveData.isInitialized) {
            recentlyUsedAccountsLiveData = accountDao.getLatestUsedThreeAccounts()
        }
        return recentlyUsedAccountsLiveData
    }

    fun getRecentlyUsedThreeGroups(): LiveData<List<GroupModel>> {
        if (!::recentlyUsedGroupsLiveData.isInitialized) {
            recentlyUsedGroupsLiveData = groupDao.getLatestUsedThreeGroups()
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