package dreammaker.android.expensetracker.ui.group.viewgroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.ui.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import javax.inject.Inject

@HiltViewModel
class ViewGroupViewModel @Inject constructor(
    private val groupRepo: GroupRepository
): ViewModel() {

    private lateinit var groupLiveData: LiveData<Group?>

    fun getStoredGroup(): Group? {
        if (!::groupLiveData.isInitialized) {
            return null
        }
        return groupLiveData.value
    }

    fun findGroupById(id: Long): LiveData<Group?> {
        if (!::groupLiveData.isInitialized) {
            groupLiveData = groupRepo.getGroupById(id).asLiveData(viewModelScope.coroutineContext + Dispatchers.IO)
        }
        return groupLiveData
    }

    private val _deleteGroupState = MutableSharedFlow<UIState<Nothing>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val deleteGroupState: Flow<UIState<Nothing>> get() = _deleteGroupState.asSharedFlow()

    fun removeGroup(group: Group) {

        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _deleteGroupState.tryEmit(UIState.UILoading())
                groupRepo.removeGroup(group.id)
                emit(null)
            }
                .catch { error -> _deleteGroupState.tryEmit(UIState.UIError(error,group)) }
                .collect { _deleteGroupState.tryEmit(UIState.UISuccess()) }
        }
    }
}