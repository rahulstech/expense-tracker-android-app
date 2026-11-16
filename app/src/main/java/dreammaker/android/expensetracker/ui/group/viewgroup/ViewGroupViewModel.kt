package dreammaker.android.expensetracker.ui.group.viewgroup

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import javax.inject.Inject


sealed interface ViewGroupDialog {

    data object None: ViewGroupDialog

    data object ConfirmDelete: ViewGroupDialog

    data class DeleteError(val cause: Throwable): ViewGroupDialog

    data object PendingDelete: ViewGroupDialog
}
@Immutable
data class ViewGroupUiState(
    val isLoading: Boolean = false,
    val group: Group = Group.UNKNOWN,
    val isPendingGroupDelete: Boolean = false,
    val showDialog: ViewGroupDialog = ViewGroupDialog.None
)

sealed interface ViewGroupUiEvent {

    data object SetDefaultGroup: ViewGroupUiEvent

    data object DeleteGroup: ViewGroupUiEvent

    data object ConfirmDeleteGroup: ViewGroupUiEvent

    data object DismissDialog: ViewGroupUiEvent
}

sealed interface ViewGroupUiEffect {

    data object ShowGroupNotFoundAndExit: ViewGroupUiEffect

    data class ShowGroupLoadErrorAndExit(val cause: Throwable): ViewGroupUiEffect

    data class ShowDeleteGroupSuccessfulAndExit(val group: Group): ViewGroupUiEffect
}

@HiltViewModel
class ViewGroupViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val groupRepo: GroupRepository
): ViewModel() {

    companion object {
        val TAG = ViewGroupViewModel::class.simpleName
    }

    private val _uiState = MutableStateFlow(ViewGroupUiState())

    val uiState: StateFlow<ViewGroupUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ViewGroupUiEffect>()

    val uiEffect: Flow<ViewGroupUiEffect> = _uiEffect.receiveAsFlow()


    init {
        val route = savedState.toRoute<AppDestination.ViewGroup>()
        loadGroupById(route.groupId)
    }

    private fun loadGroupById(id: Long) {
        viewModelScope.launch {
            groupRepo.getGroupById(id)
                .catch { cause ->
                    _uiEffect.send(ViewGroupUiEffect.ShowGroupLoadErrorAndExit(cause))
                }
                .onEach {
                    _uiState.update {
                        it.copy(isLoading = true)
                    }
                    delay(1000)
                }
                .collectLatest { group ->
                    group?.let { foundGroup ->
                        _uiState.update {
                            it.copy(isLoading = false, group = foundGroup)
                        }
                    } ?: _uiEffect.send(ViewGroupUiEffect.ShowGroupNotFoundAndExit)
                }
        }
    }

    fun handleUiEvent(event: ViewGroupUiEvent) {
        when(event) {
            is ViewGroupUiEvent.SetDefaultGroup -> {}

            is ViewGroupUiEvent.DeleteGroup -> {
                _uiState.update {
                    it.copy(showDialog = ViewGroupDialog.ConfirmDelete)
                }
            }

            is ViewGroupUiEvent.ConfirmDeleteGroup -> {
                deleteGroup(uiState.value.group)
            }

            is ViewGroupUiEvent.DismissDialog -> {
                _uiState.update {
                    it.copy(showDialog = ViewGroupDialog.None)
                }
            }
        }
    }


    private fun deleteGroup(group: Group) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(isPendingGroupDelete = true, showDialog = ViewGroupDialog.PendingDelete)
            }

            try {
                groupRepo.removeGroup(group.id)
                _uiState.update {
                    it.copy(isPendingGroupDelete = false, showDialog = ViewGroupDialog.None)
                }
                _uiEffect.send(ViewGroupUiEffect.ShowDeleteGroupSuccessfulAndExit(group.copy()))
            }
            catch (cause: Throwable) {
                Log.e(TAG, "delete group error", cause)
                _uiState.update {
                    it.copy(isPendingGroupDelete = false, showDialog = ViewGroupDialog.DeleteError(cause))
                }
            }
        }
    }
}