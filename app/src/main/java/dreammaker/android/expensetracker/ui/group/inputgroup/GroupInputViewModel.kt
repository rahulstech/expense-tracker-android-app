package dreammaker.android.expensetracker.ui.group.inputgroup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
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

class GroupInputViewModel(
    app: Application
): AndroidViewModel(app) {

    private val groupRepo = ExpenseRepository.getInstance(app).groupRepository

    lateinit var groupsLiveData: LiveData<Group?>

    fun getStoredGroup(): Group? {
        if (!::groupsLiveData.isInitialized) {
            return null
        }
        return groupsLiveData.value
    }

    fun findGroupById(id: Long): LiveData<Group?> {
        if (!::groupsLiveData.isInitialized) {
            groupsLiveData = groupRepo.getLiveGroupById(id)
        }
        return groupsLiveData
    }

    private val _saveGroupState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val saveGroupState: Flow<UIState> get() = _saveGroupState.asSharedFlow()

    fun addGroup(group: Group) {
        viewModelScope.launch(Dispatchers.IO){
            flow {
                _saveGroupState.tryEmit(UIState.UILoading())
                val savedGroup = groupRepo.insertGroup(group)
                emit(savedGroup)
            }
                .catch { error -> _saveGroupState.tryEmit(UIState.UIError(error)) }
                .collect { _saveGroupState.tryEmit(UIState.UISuccess(it)) }
        }
    }

    fun setGroup(group: Group) {
        viewModelScope.launch(Dispatchers.IO){
            flow {
                _saveGroupState.tryEmit(UIState.UILoading())
                groupRepo.updateGroup(group)
                emit(group)
            }
                .catch { error -> _saveGroupState.tryEmit(UIState.UIError(error)) }
                .collect { _saveGroupState.tryEmit(UIState.UISuccess(it)) }
        }
    }
}