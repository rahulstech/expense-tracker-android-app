package dreammaker.android.expensetracker.ui.history.historyinput

import android.util.Log
import dreammaker.android.expensetracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.HistoryType
import java.math.BigDecimal

class UiComposition(
    initialType: HistoryType,
    private val scope: CoroutineScope,
    private val onSaveHistory: suspend (History) -> History
) {

    companion object {
        private val TAG = UiComposition::class.simpleName
    }

    private val _uiState = MutableStateFlow(HistoryInputUiState(initialType))

    val uiState: StateFlow<HistoryInputUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<HistoryInputUiEffect>(Channel.BUFFERED)

    val uiEffect: Flow<HistoryInputUiEffect> = _uiEffect.receiveAsFlow()

    fun onHistoryLoadingComplete(history: History?) {
        history?.let { foundHistory ->
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    id = foundHistory.id,
                    date = foundHistory.date,
                    amount = foundHistory.amount.toPlainString(),
                    type = foundHistory.type,
                    sourceAccount = foundHistory.primaryAccount,
                    destinationAccount = foundHistory.secondaryAccount,
                    group = foundHistory.group,
                    note = foundHistory.note ?: ""
                )
            }
        } ?: sendUiEffect(HistoryInputUiEffect.ShowNotFoundAndExit)
    }

    fun updateAccountState(state: AccountState) {
        _uiState.update {
            it.copy(
                isAccountLoading = state.isLoading,
                allAccounts = state.allAccounts,
                recentAccounts = state.recentAccounts
            )
        }
    }

    fun updateGroupState(state: GroupState) {
        _uiState.update {
            it.copy(
                isGroupLoading = state.isLoading,
                allGroups = state.allGroups,
                recentGroups = state.recentGroups
            )
        }
    }

    fun handleUiEvent(event: HistoryInputUiEvent) {
        when(event) {
            is HistoryInputUiEvent.DateChange -> {
                onUpdateUiState {
                    it.copy(date = event.date)
                }
            }

            is HistoryInputUiEvent.AmountChange -> {
                onUpdateUiState {
                    it.copy(
                        amount = event.amount,
                        amountError = null
                    )
                }
            }

            is HistoryInputUiEvent.HistoryTypeChange -> {
                onUpdateUiState {
                    it.copy(type = event.type)
                }
            }

            is HistoryInputUiEvent.NoteChange -> {
                onUpdateUiState {
                    it.copy(note = event.note)
                }
            }

            is HistoryInputUiEvent.SourceAccountChange -> {
                onUpdateUiState {
                    it.copy(
                        sourceAccount = event.account,
                        sourceAccountError = null
                    )
                }
            }

            is HistoryInputUiEvent.DestinationAccountChange -> {
                onUpdateUiState {
                    it.copy(
                        destinationAccount = event.account,
                        destinationAccountError = null
                    )
                }
            }

            is HistoryInputUiEvent.GroupChange -> {
                onUpdateUiState {
                    it.copy(group = event.group)
                }
            }

            is HistoryInputUiEvent.Save -> {
                saveHistory()
            }

            is HistoryInputUiEvent.AddMore -> {
                onUpdateUiState {
                    it.reset()
                }
            }

            is HistoryInputUiEvent.DismissDialog -> {
                onUpdateUiState {
                    it.copy(dialog = HistoryInputDialog.None)
                }
            }
        }
    }

    private fun saveHistory() {
        scope.launch(Dispatchers.IO) {
            _uiState.update {
                it.resetInputErrors()
                    .copy(
                        isSaving = true,
                        dialog = HistoryInputDialog.PendingSave
                )
            }
            val state = _uiState.value
            val result = validateInput(state)
            if (result == null) {
                val history = state.toHistory()
                runCatching { onSaveHistory(history) }
                    .onFailure { cause ->
                        Log.e(TAG, "history save error", cause)
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                dialog = HistoryInputDialog.SaveError(cause)
                            )
                        }
                    }
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                dialog = if (!it.isEditing) HistoryInputDialog.AddMore else HistoryInputDialog.None
                            )
                        }
                        _uiEffect.send(HistoryInputUiEffect.ShowSaveSuccessful(_uiState.value.isEditing))
                    }
            }
            else {
                _uiState.update {
                    it.resetInputErrors()
                        .copy(
                            isSaving = false,
                            amountError = result.amountError,
                            sourceAccountError = result.sourceAccountError,
                            destinationAccountError = result.destinationAccountError,
                            dialog = HistoryInputDialog.None
                        )
                }
            }
        }
    }

    private fun validateInput(state: HistoryInputUiState): ValidationErrorResult? {
        val amountParsed = state.amount.toBigDecimalOrNull()
        val destAccount = state.destinationAccount
        val amountError = when {
            state.amount.isBlank() -> R.string.error_empty_input
            amountParsed == null -> R.string.error_non_numeric_input
            amountParsed <= BigDecimal.ZERO -> R.string.error_negative_numeric_input
            else -> null
        }
        val sourceAccountError = if (state.sourceAccount == null) R.string.error_empty_input else null
        val destinationAccountError = if (state.type == HistoryType.TRANSFER) {
            when {
                destAccount == null -> R.string.error_empty_input
                state.sourceAccount?.id == destAccount.id ->  R.string.error_same_account_money_transfer
                else -> null
            }
        }
        else null

        if (amountError == null && sourceAccountError == null && destinationAccountError == null) {
            return null
        }
        return ValidationErrorResult(amountError,sourceAccountError,destinationAccountError)
    }

    private fun onUpdateUiState(updater: (HistoryInputUiState)-> HistoryInputUiState) {
        _uiState.update(updater)
    }

    private fun sendUiEffect(effect: HistoryInputUiEffect) {
        scope.launch {
            _uiEffect.send(effect)
        }
    }
}