package dreammaker.android.expensetracker.ui.group.groupslist

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

class GroupListViewModel(app: Application): AndroidViewModel(app) {

    private val groupDao: GroupDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        groupDao = db.groupDao
    }

    private lateinit var groups: LiveData<List<GroupModel>>

    fun getAllGroups(): LiveData<List<GroupModel>> {
        if (!::groups.isInitialized) {
            groups = groupDao.getAllGroups()
        }
        return groups
    }

    private val _deleteGroupsState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val deleteGroupsState: Flow<UIState?> get() = _deleteGroupsState.asSharedFlow()


    fun deleteGroups(ids: List<Long>) {
        _deleteGroupsState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                groupDao.deleteMultipleGroups(ids)
                emit(null)
            }
                .catch { error -> _deleteGroupsState.tryEmit(UIState.UIError(error)) }
                .collect { _deleteGroupsState.tryEmit(UIState.UISuccess()) }
        }
    }
}