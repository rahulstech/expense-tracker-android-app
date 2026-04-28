package dreammaker.android.expensetracker.ui.group.inputgroup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import javax.inject.Inject

data class GroupInputUIState(
    val isLoadingGroup: Boolean = false,
    val group: Group? = null,
    val isSaving: Boolean = false,
)

sealed interface GroupInputUIEvent {

    data class SaveSuccessful(val group: Group): GroupInputUIEvent

    data class SaveError(val cause: Throwable): GroupInputUIEvent

    data class LoadingError(val cause: Throwable): GroupInputUIEvent
}

@HiltViewModel
class GroupInputViewModel @Inject constructor(
    private val groupRepo: GroupRepository
): ViewModel() {

    companion object {
        private const val TAG = "GroupInputViewModel"
    }

    private val _uiState = MutableStateFlow(GroupInputUIState())
    val uiState: StateFlow<GroupInputUIState> = _uiState.asStateFlow()

    private val currentUIState: GroupInputUIState get() = _uiState.value

    private val _uiEvent = MutableSharedFlow<GroupInputUIEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val uiEvent: SharedFlow<GroupInputUIEvent> = _uiEvent.asSharedFlow()

    private var lastFoundGroupId = 0L

    fun findGroupById(id: Long) {
        if (id == lastFoundGroupId) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadState(isLoadingGroup = true)
            try {
                val group = groupRepo.findGroupById(id)
                updateLoadState(isLoadingGroup = false, group = group)
                lastFoundGroupId = id
            } catch (th: Throwable) {
                Log.e(TAG, "can not find group by id $id", th)
                updateLoadState(isLoadingGroup = false)
                _uiEvent.emit(GroupInputUIEvent.LoadingError(th))
            }
        }
    }

    fun saveGroup(group: Group, isEdit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSaveState(isSaving = true)
            try {
                val savedGroup = if (isEdit) {
                    groupRepo.updateGroup(group)
                    group
                } else {
                    groupRepo.insertGroup(group)
                }
                _uiEvent.emit(GroupInputUIEvent.SaveSuccessful(savedGroup))
            } catch (th: Throwable) {
                Log.e(TAG, "save group failed with error", th)
                _uiEvent.emit(GroupInputUIEvent.SaveError(th))
            } finally {
                updateSaveState(isSaving = false)
            }
        }
    }

    private fun updateLoadState(
        isLoadingGroup: Boolean = currentUIState.isLoadingGroup,
        group: Group? = currentUIState.group
    ) {
        _uiState.update {
            it.copy(
                isLoadingGroup = isLoadingGroup,
                group = group,
            )
        }
    }

    private fun updateSaveState(
        isSaving: Boolean = currentUIState.isSaving,
    ) {
        _uiState.update {
            it.copy(isSaving = isSaving)
        }
    }
}
