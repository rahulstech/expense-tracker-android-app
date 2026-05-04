package dreammaker.android.expensetracker.ui.group.inputgroup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import javax.inject.Inject

data class GroupInputUIState(
    val id: Long = 0,
    val name: String = "",
    val balance: String = "",

    val nameError: Int? = null,
    val balanceError: Int? = null,

    val isLoadingGroup: Boolean = false,
    val loadingError: Throwable? = null,

    val isSaving: Boolean = false,
    val savingError: Throwable? = null,
    val savingSuccess: Boolean = false,

    val showAddMoreDialog: Boolean = false
)

@HiltViewModel
class GroupInputViewModel @Inject constructor(
    private val groupRepo: GroupRepository
): ViewModel() {

    companion object {
        private const val TAG = "GroupInputViewModel"
    }

    private val _uiState = MutableStateFlow(GroupInputUIState())
    val uiState: StateFlow<GroupInputUIState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onBalanceChange(balance: String) {
        _uiState.update { it.copy(balance = balance, balanceError = null) }
    }

    fun findGroupById(id: Long) {
        if (id == _uiState.value.id && id != 0L) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingGroup = true, loadingError = null) }
            try {
                val group = groupRepo.getGroup(id)
                if (group != null) {
                    _uiState.update {
                        it.copy(
                            id = group.id,
                            name = group.name,
                            balance = group.balance.toString(),
                            isLoadingGroup = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingGroup = false,
                            loadingError = Exception("Group not found")
                        )
                    }
                }
            } catch (th: Throwable) {
                Log.e(TAG, "can not find group by id $id", th)
                _uiState.update { it.copy(isLoadingGroup = false, loadingError = th) }
            }
        }
    }

    fun saveGroup() {
        val state = _uiState.value
        val nameBlank = state.name.isBlank()
        val balanceValue = state.balance.toDoubleOrNull()
        val balanceBlank = state.balance.isBlank()

        if (nameBlank || balanceBlank || balanceValue == null) {
            _uiState.update {
                it.copy(
                    nameError = if (nameBlank) R.string.error_empty_group_name else null,
                    balanceError = when {
                        balanceBlank -> R.string.error_empty_balance_input
                        balanceValue == null -> R.string.error_invalid_balance_input
                        else -> null
                    }
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true, savingError = null, savingSuccess = false) }
            try {
                val groupToSave = Group(
                    id = state.id,
                    name = state.name,
                    balance = state.balance.toDouble()
                )
                if (groupToSave.id == 0L) {
                    groupRepo.createGroup(groupToSave)
                } else {
                    groupRepo.editGroup(groupToSave)
                }
                _uiState.update { it.copy(isSaving = false, savingSuccess = true) }
            } catch (th: Throwable) {
                Log.e(TAG, "save group failed with error", th)
                _uiState.update { it.copy(isSaving = false, savingError = th) }
            }
        }
    }

    fun onShowAddMoreDialog(show: Boolean) {
        _uiState.update { it.copy(showAddMoreDialog = show) }
    }

    fun resetInput() {
        _uiState.update {
            it.copy(
                name = "",
                balance = "",
                savingSuccess = false,
                showAddMoreDialog = false
            )
        }
    }

    fun clearSavingSuccess() {
        _uiState.update { it.copy(savingSuccess = false) }
    }
}
