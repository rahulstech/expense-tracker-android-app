package dreammaker.android.expensetracker.ui.account.inputaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.model.Account
import javax.inject.Inject


data class AccountInputUIState(
    val isLoadingAccount: Boolean = false,
    val account: Account? = null,
    val accountLoadError: Throwable? = null,
    val isSaving: Boolean = false,
    val isSaveSuccessful: Boolean = false,
    val saveError: Throwable? = null
)



@HiltViewModel
class AccountInputViewModel @Inject constructor(
    private val accountRepo: AccountRepository
): ViewModel() {

    private var _uiState = MutableStateFlow(AccountInputUIState())

    private val currentUIState: AccountInputUIState get() = _uiState.value

    val uiState: StateFlow<AccountInputUIState> = _uiState.asStateFlow()

    private var lastFoundAccId = 0L

    fun findAccountById(id: Long) {
        if (id == lastFoundAccId) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadState(isLoadingAccount = true)
            try {
                val account = accountRepo.findAccountById(id)
                updateLoadState(isLoadingAccount = false, account = account)
                lastFoundAccId = id
            }
            catch (th: Throwable) {
                updateLoadState(isLoadingAccount = false, account = null, accountLoadError = th)
            }
        }
    }

    fun saveAccount(account: Account, isEdit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSaveSate(isSaving = true)
            try {
                if (isEdit) {
                    accountRepo.updateAccount(account)
                }
                else {
                    accountRepo.insertAccount(account)
                }
                updateSaveSate(isSaving = false, isSaveSuccessful = true)
            }
            catch (th: Throwable) {
                updateSaveSate(isSaving = false, isSaveSuccessful = false, saveError = th)
            }
        }
    }

    private fun updateLoadState(
        isLoadingAccount: Boolean = currentUIState.isLoadingAccount,
        account: Account? = currentUIState.account,
        accountLoadError: Throwable? = currentUIState.accountLoadError,
    ) {
        _uiState.value = currentUIState.copy(
            isLoadingAccount = isLoadingAccount,
            account = account,
            accountLoadError = accountLoadError
        )
    }

    private fun updateSaveSate(
        isSaving: Boolean = currentUIState.isSaving,
        isSaveSuccessful: Boolean = currentUIState.isSaveSuccessful,
        saveError: Throwable? = currentUIState.saveError
    ) {
        _uiState.value = currentUIState.copy(
            isSaving = isSaving,
            isSaveSuccessful = isSaveSuccessful,
            saveError = saveError
        )
    }


}