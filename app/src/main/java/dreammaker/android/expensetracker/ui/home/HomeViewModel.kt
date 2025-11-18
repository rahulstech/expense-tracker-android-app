package dreammaker.android.expensetracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group

class HomeViewModel (
    app: Application
): AndroidViewModel(app) {

    private val repos = ExpenseRepository.getInstance(app)
    private val accountRepo = repos.accountRepository
    private val groupRepo = repos.groupRepository

    private var liveRecentlyUsedAccounts: LiveData<List<Account>>? = null

    fun getRecentlyUsedThreeAccounts(): LiveData<List<Account>> {
        if (null==liveRecentlyUsedGroups) {
            liveRecentlyUsedAccounts = accountRepo.getLiveRecentlyUsedAccounts(3)
        }
        return liveRecentlyUsedAccounts!!
    }

    private var liveRecentlyUsedGroups: LiveData<List<Group>>? = null

    fun getRecentlyUsedThreeGroups(): LiveData<List<Group>> {
        if (null==liveRecentlyUsedGroups) {
            liveRecentlyUsedGroups = groupRepo.getLiveRecentlyUsedGroups(3)
        }
        return liveRecentlyUsedGroups!!
    }

    private var liveTotalBalance: LiveData<Double>? = null

    fun getTotalBalance(): LiveData<Double> {
        if (null == liveTotalBalance) {
            liveTotalBalance = accountRepo.getLiveTotalBalance()
        }
        return liveTotalBalance!!
    }
}