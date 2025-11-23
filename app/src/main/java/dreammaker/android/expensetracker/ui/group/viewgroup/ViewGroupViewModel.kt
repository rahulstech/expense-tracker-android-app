package dreammaker.android.expensetracker.ui.group.viewgroup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Group

class ViewGroupViewModel (
    app: Application
): AndroidViewModel(app) {

    private val groupRepo = ExpenseRepository.getInstance(app).groupRepository

    private lateinit var groupLiveData: LiveData<Group?>

    fun getStoredGroup(): Group? {
        if (!::groupLiveData.isInitialized) {
            return null
        }
        return groupLiveData.value
    }

    fun findGroupById(id: Long): LiveData<Group?> {
        if (!::groupLiveData.isInitialized) {
            groupLiveData = groupRepo.getLiveGroupById(id)
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
                groupRepo.deleteGroup(group.id)
                emit(null)
            }
                .catch { error -> _deleteGroupState.tryEmit(UIState.UIError(error,group)) }
                .collect { _deleteGroupState.tryEmit(UIState.UISuccess()) }
        }
    }
}