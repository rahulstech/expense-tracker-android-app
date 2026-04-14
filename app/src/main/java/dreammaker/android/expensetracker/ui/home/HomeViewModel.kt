package dreammaker.android.expensetracker.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val groupRepo: GroupRepository
): ViewModel() {

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