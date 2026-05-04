package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate
import javax.inject.Inject


data class HistoryInputUIState(
    val id: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val amount: String = "",
    val isCredit: Boolean = false,
    val isTransfer: Boolean = false,
    val account: Account? = null,
    val destinationAccount: Account? = null,
    val group: Group? = null,
    val note: String = "",

    val amountError: Int? = null,
    val accountError: Int? = null,
    val destinationAccountError: Int? = null,

    val isLoadingHistory: Boolean = false,
    val historyLoadingError: Throwable? = null,

    val isSaving: Boolean = false,
    val savingError: Throwable? = null,
    val savingSuccess: Boolean = false,

    val showAddMoreDialog: Boolean = false
)


@HiltViewModel
class HistoryInputViewModel @Inject constructor(
    private val historyRepo: HistoryRepository,
    private val accountRepo: AccountRepository,
    private val groupRepo: GroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryInputUIState())
    val uiState: StateFlow<HistoryInputUIState> = _uiState.asStateFlow()

    var history: History? = null
        private set

    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onDateChange(date: LocalDate) = setDate(date)

    fun setAccountSelection(account: Account?) {
        _uiState.update { it.copy(account = account, accountError = null) }
    }

    fun onAccountSelected(account: Account?) = setAccountSelection(account)

    fun setDestinationAccountSelection(account: Account?) {
        _uiState.update { it.copy(destinationAccount = account, destinationAccountError = null) }
    }

    fun onDestinationAccountSelected(account: Account?) = setDestinationAccountSelection(account)

    fun setIsTransfer(isTransfer: Boolean) {
        _uiState.update { it.copy(isTransfer = isTransfer) }
    }

    fun setGroup(group: Group?) {
        _uiState.update { it.copy(group = group) }
    }

    fun onGroupSelected(group: Group?) = setGroup(group)

    fun onAmountChange(amount: String) {
        if (amount.startsWith("-")) return
        _uiState.update { it.copy(amount = amount, amountError = null) }
    }

    fun onTypeChange(isCredit: Boolean) {
        _uiState.update { it.copy(isCredit = isCredit) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun findHistory(id: Long) {
        if (_uiState.value.id == id) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingHistory = true, historyLoadingError = null) }
            try {
                val history = historyRepo.getHistory(id)

                history?.let { foundHistory ->
                    _uiState.update {
                        it.copy(
                            isLoadingHistory = false,
                            id = foundHistory.id,
                            date = foundHistory.date,
                            amount = if (foundHistory.amount == 0.0) "" else foundHistory.amount.toString(),
                            isCredit = foundHistory is History.CreditHistory,
                            isTransfer = foundHistory is History.TransferHistory,
                            account = foundHistory.primaryAccount,
                            destinationAccount = foundHistory.secondaryAccount,
                            group = foundHistory.group,
                            note = foundHistory.note ?: ""
                        )
                    }
                } ?: _uiState.update {
                    it.copy(isLoadingHistory = false, historyLoadingError = Exception("History not found"))
                }
            } catch (error: Throwable) {
                _uiState.update { it.copy(isLoadingHistory = false, historyLoadingError = error) }
            }
        }
    }

    fun saveHistory() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull()
        val amountBlank = state.amount.isBlank()
        val amountNegative = amountValue != null && amountValue < 0
        val accountNull = state.account == null
        val destinationAccountNull = state.isTransfer && state.destinationAccount == null
        val accountsSame = state.isTransfer && state.account != null && state.account == state.destinationAccount

        if (amountBlank || amountNegative || accountNull || destinationAccountNull || accountsSame) {
            _uiState.update {
                it.copy(
                    amountError = when {
                        amountBlank -> R.string.error_invalid_amount
                        amountNegative -> R.string.error_negative_amount
                        else -> null
                    },
                    accountError = if (accountNull) R.string.error_no_selection else null,
                    destinationAccountError = when {
                        destinationAccountNull -> R.string.error_no_selection
                        accountsSame -> R.string.error_same_account_money_transfer
                        else -> null
                    }
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            val historyToSave = if (currentState.isTransfer) {
                History.TransferHistory(
                    id = currentState.id,
                    amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                    date = currentState.date,
                    primaryAccountId = currentState.account?.id ?: 0,
                    secondaryAccountId = currentState.destinationAccount?.id ?: 0,
                    note = currentState.note
                )
            } else if (currentState.isCredit) {
                History.CreditHistory(
                    id = currentState.id,
                    amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                    date = currentState.date,
                    primaryAccountId = currentState.account?.id ?: 0,
                    groupId = currentState.group?.id,
                    note = currentState.note
                )
            } else {
                History.DebitHistory(
                    id = currentState.id,
                    amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                    date = currentState.date,
                    primaryAccountId = currentState.account?.id ?: 0,
                    groupId = currentState.group?.id,
                    note = currentState.note
                )
            }

            _uiState.update { it.copy(isSaving = true, savingError = null, savingSuccess = false) }
            try {
                if (historyToSave.id == 0L) {
                    historyRepo.createHistory(historyToSave)
                } else {
                    historyRepo.editHistory(historyToSave)
                }
                _uiState.update { it.copy(isSaving = false, savingSuccess = true) }
            } catch (error: Throwable) {
                _uiState.update { it.copy(isSaving = false, savingError = error) }
            }
        }
    }

    fun onShowAddMoreDialog(show: Boolean) {
        _uiState.update { it.copy(showAddMoreDialog = show) }
    }

    fun resetInput() {
        _uiState.update {
            it.copy(
                amount = "",
                note = "",
                savingSuccess = false,
                showAddMoreDialog = false
            )
        }
    }

    fun clearSavingSuccess() {
        _uiState.update { it.copy(savingSuccess = false) }
    }

    // default account

    fun setDefaultAccount(account: Account) = accountRepo.setDefaultAccount(account)

    fun getDefaultAccount(): Flow<Account?> = accountRepo.getDefaultAccount()

    val defaultAccount: StateFlow<Account?> = accountRepo.getDefaultAccount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allAccounts: Flow<List<Account>> by lazy {
        accountRepo.getAllAccounts()
    }

    val lastUsedAccounts: Flow<List<Account>> by lazy {
        accountRepo.getRecentlyUsedAccounts(3)
    }

    val allGroups: Flow<List<Group>> by lazy {
        groupRepo.getAllGroups()
    }

    val lastUsedGroups: Flow<List<Group>> by lazy {
        groupRepo.getRecentlyUsedGroups(3)
    }
}
