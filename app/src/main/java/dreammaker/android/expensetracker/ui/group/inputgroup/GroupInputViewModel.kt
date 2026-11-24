package dreammaker.android.expensetracker.ui.group.inputgroup

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import java.math.BigDecimal
import javax.inject.Inject

sealed interface GroupInputDialogType {

    data object None: GroupInputDialogType

    data object PendingSave: GroupInputDialogType

    data class SaveError(val cause: Throwable): GroupInputDialogType

    data object AddMoreGroup: GroupInputDialogType
}

@Immutable
data class GroupInputUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val id: Long = 0,
    val name: String = "",
    val balance: String = "",
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val balanceError: Int? = null,
    val isSaving: Boolean = false,
    val showDialog: GroupInputDialogType = GroupInputDialogType.None
) {

    fun toGroup(): Group {
        val balance = balance.toBigDecimalOrNull() ?: BigDecimal.ZERO
        return Group(id=id,name=name,balance=balance)
    }
}

private data class ValidationErrorResult(
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val balanceError: Int? = null
)


sealed interface GroupInputUiEvent {

    data class NameChange(val name: String): GroupInputUiEvent

    data class BalanceChange(val balance: String): GroupInputUiEvent

    data object SaveGroup: GroupInputUiEvent

    data object AddMoreGroup: GroupInputUiEvent

    data object DismissDialog: GroupInputUiEvent
}

sealed interface GroupInputUiEffect {

    data object ShowGroupNotFoundAndExit: GroupInputUiEffect

    data class ShowGroupLoadErrorAndExit(val cause: Throwable): GroupInputUiEffect

    data class ShowGroupSaveSuccessful(val exit: Boolean): GroupInputUiEffect
}

@HiltViewModel
class GroupInputViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val groupRepo: GroupRepository
): ViewModel() {

    companion object {
        private const val TAG = "GroupInputViewModel"
    }

    private val _uiState = MutableStateFlow(GroupInputUiState())

    val uiState: StateFlow<GroupInputUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<GroupInputUiEffect>()

    val uiEffect: Flow<GroupInputUiEffect> = _uiEffect.receiveAsFlow()

    init {
        runCatching {
            val route = savedState.toRoute<AppDestination.EditGroup>()
            loadGroup(route.groupId)
        }
    }

    fun handleUiEvent(event: GroupInputUiEvent) {
        when(event) {

            is GroupInputUiEvent.NameChange -> {
                _uiState.update {
                    it.copy(name = event.name)
                }
            }
            is GroupInputUiEvent.BalanceChange -> {
                _uiState.update {
                    it.copy(balance = event.balance)
                }
            }
            is GroupInputUiEvent.SaveGroup -> {
                saveGroup()
            }
            is GroupInputUiEvent.AddMoreGroup -> {
                _uiState.update {
                    it.copy(
                        id = 0, name = "", balance = "",
                        nameError = null, balanceError = null,
                        showDialog = GroupInputDialogType.None
                    )
                }
            }
            is GroupInputUiEvent.DismissDialog -> {
                _uiState.update {
                    it.copy(showDialog = GroupInputDialogType.None)
                }
            }
        }
    }

    private fun loadGroup(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEditing = true, isLoading = true, id = id) }

            groupRepo.getGroupById(id)
                .catch { cause ->
                    _uiEffect.send(GroupInputUiEffect.ShowGroupLoadErrorAndExit(cause))
                }
                .collectLatest {
                    it?.let { group ->
                        _uiState.update { state ->
                            state.copy(
                                name = group.name,
                                balance = group.balance.toString(),
                                isLoading = false
                            )
                        }
                    } ?: _uiEffect.send(GroupInputUiEffect.ShowGroupNotFoundAndExit)
                }
        }
    }

    private fun saveGroup() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true, showDialog = GroupInputDialogType.PendingSave) }
            val state = _uiState.value
            val result = validateInput(state)
            if (result == null) {
                val group = state.toGroup()
                runCatching {
                    if (state.isEditing) {
                        groupRepo.editGroup(group)
                    }
                    else {
                        groupRepo.createGroup(group)
                    }
                }
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                showDialog = if (it.isEditing) GroupInputDialogType.None else GroupInputDialogType.AddMoreGroup)
                        }
                        _uiEffect.send(GroupInputUiEffect.ShowGroupSaveSuccessful(state.isEditing))
                    }
                    .onFailure { cause ->
                        Log.e(TAG, "save group error ", cause)
                        _uiState.update {
                            it.copy(isSaving = false, showDialog = GroupInputDialogType.SaveError(cause))
                        }
                    }
            }
            else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        nameError = result.nameError,
                        balanceError = result.balanceError,
                        showDialog = GroupInputDialogType.None
                    )
                }
            }
        }
    }

    private fun validateInput(state: GroupInputUiState): ValidationErrorResult? {
        val balance = state.balance.toBigDecimalOrNull()
        val nameError: Int? = if (state.name.isBlank()) R.string.error_empty_input else null
        val balanceError: Int? = if (state.balance.isNotBlank() && balance == null) R.string.error_non_numeric_input else null
        if (nameError == null && balanceError == null) {
            return null
        }
        return ValidationErrorResult(nameError,balanceError)
    }
}
