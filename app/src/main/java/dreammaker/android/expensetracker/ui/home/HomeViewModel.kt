package dreammaker.android.expensetracker.ui.home

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import javax.inject.Inject

@Immutable
data class HomeUiState(
    val isTotalAccountBalanceLoading: Boolean = false,
    val totalAccountBalance: Double = 0.0,
    val isAccountsLoading: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val isGroupsLoading: Boolean = false,
    val groups: List<Group> = emptyList()
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val groupRepo: GroupRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTotalAccountBalance()
        loadRecentAccounts()
        loadRecentGroups()
    }

    private fun loadTotalAccountBalance() {
        viewModelScope.launch {
            accountRepo.getTotalBalance()
                .onStart {
                    _uiState.update {
                        it.copy(isTotalAccountBalanceLoading = true)
                    }
                }
                .catch { cause ->
                    Log.e(TAG, "total account balance loading error", cause)
                    _uiState.update {
                        it.copy(isTotalAccountBalanceLoading = false, totalAccountBalance = 0.0)
                    }
                }
                .onEach {
                    _uiState.update {
                        it.copy(isTotalAccountBalanceLoading = true)
                    }
                    delay(1000)
                }
                .collectLatest { totalAccountBalance ->
                    _uiState.update {
                        it.copy(isTotalAccountBalanceLoading = false, totalAccountBalance = totalAccountBalance)
                    }
                }
        }
    }

    private fun loadRecentAccounts() {
        viewModelScope.launch {
            accountRepo.getThreeFrequentlyUsedAccounts()
                .onStart {
                    _uiState.update {
                        it.copy(isAccountsLoading = true)
                    }
                }
                .catch { cause ->
                    Log.e(TAG, "recent accounts loading error", cause)
                    _uiState.update {
                        it.copy(isAccountsLoading = false, accounts = emptyList())
                    }
                }
                .onEach {
                    _uiState.update {
                        it.copy(isAccountsLoading = true)
                    }
                    delay(1000)
                }
                .collectLatest { accounts ->
                    _uiState.update {
                        it.copy(isAccountsLoading = false, accounts = accounts)
                    }
                }
        }

    }

    private fun loadRecentGroups() {
        viewModelScope.launch {
            groupRepo.getThreeFrequentlyUsedGroups()
                .onStart {
                    _uiState.update {
                        it.copy(isGroupsLoading = true)
                    }
                }
                .catch { cause ->
                    Log.e(TAG, "recent groups loading error", cause)
                    _uiState.update {
                        it.copy(isGroupsLoading = false, groups = emptyList())
                    }
                }
                .onEach {
                    _uiState.update {
                        it.copy(isGroupsLoading = true)
                    }
                    delay(1000)
                }
                .collectLatest { groups ->
                    _uiState.update {
                        it.copy(isGroupsLoading = false, groups = groups)
                    }
                }
        }
    }
}
