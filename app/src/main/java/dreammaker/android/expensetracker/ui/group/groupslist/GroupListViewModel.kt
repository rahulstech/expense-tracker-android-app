package dreammaker.android.expensetracker.ui.group.groupslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Group

class GroupListViewModel(
    app: Application
): AndroidViewModel(app) {

    private val groupRepo = ExpenseRepository.getInstance(app).groupRepository

    private lateinit var groups: LiveData<List<Group>>

    fun getAllGroups(): LiveData<List<Group>> {
        if (!::groups.isInitialized) {
            groups = groupRepo.getLiveAllGroups()
        }
        return groups
    }

    private val _deleteGroupsState = MutableSharedFlow<UIState<Nothing>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val deleteGroupsState: Flow<UIState<Nothing>?> get() = _deleteGroupsState


    fun deleteGroups(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _deleteGroupsState.tryEmit(UIState.UILoading())
                groupRepo.deleteMultipleGroups(ids)
                emit(null)
            }
                .catch { error -> _deleteGroupsState.tryEmit(UIState.UIError(error)) }
                .collect { _deleteGroupsState.tryEmit(UIState.UISuccess()) }
        }
    }
}