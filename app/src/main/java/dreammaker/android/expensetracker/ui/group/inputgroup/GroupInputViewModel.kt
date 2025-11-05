package dreammaker.android.expensetracker.ui.group.inputgroup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.GroupDao
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class GroupInputViewModel(app: Application): AndroidViewModel(app) {

    private val groupDao: GroupDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        groupDao = db.groupDao
    }

    lateinit var groupsLiveData: LiveData<GroupModel?>

    fun getStoredGroup(): GroupModel? {
        if (!::groupsLiveData.isInitialized) {
            return null
        }
        return groupsLiveData.value
    }

    fun findGroupById(id: Long): LiveData<GroupModel?> {
        if (!::groupsLiveData.isInitialized) {
            groupsLiveData = groupDao.findGroupById(id)
        }
        return groupsLiveData
    }

    private val _saveGroupState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val saveGroupState: Flow<UIState> get() = _saveGroupState.asSharedFlow()

    fun addGroup(group: GroupModel) {
        _saveGroupState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO){
            flow {
                val id = groupDao.insertGroup(group.toGroup())
                emit(group.copy(id=id))
            }
                .catch { error -> _saveGroupState.tryEmit(UIState.UIError(error)) }
                .collect { _saveGroupState.tryEmit(UIState.UISuccess(it)) }
        }
    }

    fun setGroup(group: GroupModel) {
        _saveGroupState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO){
            flow {
                groupDao.updateGroup(group.toGroup())
                emit(group)
            }
                .catch { error -> _saveGroupState.tryEmit(UIState.UIError(error)) }
                .collect { _saveGroupState.tryEmit(UIState.UISuccess(it)) }
        }
    }
}