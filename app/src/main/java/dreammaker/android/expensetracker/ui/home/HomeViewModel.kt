package dreammaker.android.expensetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.GroupRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val groupRepo: GroupRepository
) : ViewModel() {

    val uiState: StateFlow<HomeScreenState> = combine(
        accountRepo.getTotalBalance(),
        accountRepo.getRecentlyUsedAccounts(3),
        groupRepo.getRecentlyUsedGroups(3)
    ) { totalBalance, accounts, groups ->
        HomeScreenState(
            totalBalance = totalBalance,
            recentAccounts = accounts,
            recentGroups = groups
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeScreenState()
    )
}
