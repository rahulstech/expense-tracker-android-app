package dreammaker.android.expensetracker.ui.group.viewgroup

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

class ViewGroupViewModel(app: Application): AndroidViewModel(app) {

    private val groupDao: GroupDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        groupDao = db.groupDao
    }

    private lateinit var groupLiveData: LiveData<GroupModel?>

    fun getStoredGroup(): GroupModel? {
        if (!::groupLiveData.isInitialized) {
            return null
        }
        return groupLiveData.value
    }

    fun findGroupById(id: Long): LiveData<GroupModel?> {
        if (!::groupLiveData.isInitialized) {
            groupLiveData = groupDao.findGroupById(id)
        }
        return groupLiveData
    }

    private val _deleteGroupState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val deleteGroupState: Flow<UIState> get() = _deleteGroupState.asSharedFlow()

    fun removeGroup(group: GroupModel) {
        _deleteGroupState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                groupDao.deleteGroup(group.toGroup())
                emit(group)
            }
                .catch { error -> _deleteGroupState.tryEmit(UIState.UIError(error,group)) }
                .collect { _deleteGroupState.tryEmit(UIState.UISuccess(it)) }
        }
    }
}