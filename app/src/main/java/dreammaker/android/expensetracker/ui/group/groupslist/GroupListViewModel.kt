package dreammaker.android.expensetracker.ui.group.groupslist

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import javax.inject.Inject

sealed interface GroupListDialog {

    data object None: GroupListDialog

    data object ConfirmDelete: GroupListDialog

    data class DeleteError(val cause: Throwable): GroupListDialog

    data object PendingDelete: GroupListDialog
}

@Immutable
data class GroupListUiState(
    val isLoading: Boolean = false,
    val allGroups: List<Group> = emptyList(),
    val isSelectingGroups: Boolean = false,
    val selectedGroupIds: Set<Long> = emptySet(),
    val queryText: String = "",
    val showDialog: GroupListDialog = GroupListDialog.None
) {

    val groupSelectionCount: Int
        get() = if (isSelectingGroups) selectedGroupIds.size else 0

    fun toggleGroupSelection(group: Group): GroupListUiState {
        val newSelection = if (group.id in selectedGroupIds) {
            selectedGroupIds - group.id
        } else {
            selectedGroupIds + group.id
        }

        return copy(selectedGroupIds = newSelection)
    }

    fun isGroupSelected(group: Group): Boolean =
        group.id in selectedGroupIds

    fun clearGroupSelection(): GroupListUiState =
        copy(selectedGroupIds = emptySet())
}

sealed interface GroupListUiEvent {

    data class GroupSearchTextChange(val queryText: String): GroupListUiEvent

    data class ToggleGroupSelection(val group: Group): GroupListUiEvent

    data object DeleteSelectedGroups: GroupListUiEvent

    data object ConfirmDeleteSelectedGroup: GroupListUiEvent

    data object ExitGroupSelection: GroupListUiEvent

    data object DismissDialog: GroupListUiEvent
}

sealed interface GroupListUiEffect {

    data object ShowDeleteGroupsSuccessful: GroupListUiEffect
}

@OptIn(FlowPreview::class)
@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepo: GroupRepository
): ViewModel() {

    companion object {
        val TAG = GroupListViewModel::class.simpleName
    }

    private val _uiState = MutableStateFlow(GroupListUiState())

    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()

    private val _queryText = MutableStateFlow("")

    private val _uiEffect = Channel<GroupListUiEffect>()

    val uiEffect: Flow<GroupListUiEffect> = _uiEffect.receiveAsFlow()


    init {
        viewModelScope.launch {
            combine(
                groupRepo.getAllGroups(),
                _queryText.debounce(200)
            ) { allGroups, queryText ->
                allGroups.fastFilter { group ->
                    group.name.contains(queryText, ignoreCase = true)
                }
            }
                .flowOn(Dispatchers.IO)
                .collectLatest { allGroups ->
                _uiState.update {
                    it.copy(allGroups = allGroups, queryText = _queryText.value)
                }
            }
        }
    }

    fun handleUiEvent(event: GroupListUiEvent) {
        when(event) {
            is GroupListUiEvent.GroupSearchTextChange -> {
                _queryText.update { event.queryText }
            }
            is GroupListUiEvent.ToggleGroupSelection -> {
                _uiState.update {
                    it.copy(isSelectingGroups = true).toggleGroupSelection(event.group)
                }
            }
            is GroupListUiEvent.DeleteSelectedGroups -> {
                _uiState.update {
                    it.copy(showDialog = GroupListDialog.ConfirmDelete)
                }
            }
            is GroupListUiEvent.ConfirmDeleteSelectedGroup -> {
                deleteGroups(_uiState.value.selectedGroupIds.toList())
            }
            is GroupListUiEvent.ExitGroupSelection -> {
                _uiState.update {
                    it.copy(
                        isSelectingGroups = false,
                        showDialog = GroupListDialog.None
                    ).clearGroupSelection()
                }
            }
            is GroupListUiEvent.DismissDialog -> {
                _uiState.update {
                    it.copy(showDialog = GroupListDialog.None)
                }
            }
        }
    }

    private fun deleteGroups(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(showDialog = GroupListDialog.PendingDelete)
            }
            try {
                groupRepo.removeMultipleGroups(ids)
                _uiState.update {
                    it.copy(
                        isSelectingGroups = false,
                        showDialog = GroupListDialog.None
                    ).clearGroupSelection()
                }
                _uiEffect.send(GroupListUiEffect.ShowDeleteGroupsSuccessful)
            }
            catch (cause: Throwable) {
                Log.e(TAG, "delete groups error", cause)
                _uiState.update {
                    it.copy(showDialog = GroupListDialog.DeleteError(cause))
                }
            }
        }
    }
}