package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.HistoryType
import javax.inject.Inject

@HiltViewModel
class CreditDebitViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val repoComposition: RepositoryComposition
): ViewModel() {

    private val _uiComposition = UiComposition(
        initialType = HistoryType.DEBIT,
        scope = viewModelScope,
        onSaveHistory = { repoComposition.saveHistory(it, mode is HistoryInputMode.Edit) }
    )

    val uiState: StateFlow<HistoryInputUiState> = _uiComposition.uiState

    val uiEffect: Flow<HistoryInputUiEffect> = _uiComposition.uiEffect

    private val route = runCatching { savedState.toRoute<AppDestination.AddCreditDebit>() }
        .getOrElse { savedState.toRoute<AppDestination.EditCreditDebit>() }

    private val mode = HistoryInputMode.fromRoute(route)

    init {
        when(mode) {
            is HistoryInputMode.Add -> {
                viewModelScope.launch {
                    repoComposition.loadAccounts { accounts ->
                        mode.accountId?.let { id ->
                            val selectedAccount = accounts.find { it.id == id }
                        }
                    }.collectLatest(_uiComposition::updateAccountState)
                }

                viewModelScope.launch {
                    repoComposition.loadGroups()
                        .collectLatest(_uiComposition::updateGroupState)
                }
            }

            is HistoryInputMode.Edit -> {
                viewModelScope.launch {
                    repoComposition.findHistoryById(mode.historyId)
                        .collectLatest(_uiComposition::onHistoryLoadingComplete)
                }
            }
        }
    }

    fun handleUiEvent(event: HistoryInputUiEvent) {
        _uiComposition.handleUiEvent(event)
    }
}