package dreammaker.android.expensetracker.ui.group.inputgroup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.GroupDao
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.util.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    private val _resultState: MutableStateFlow<OperationResult<GroupModel>?> = MutableStateFlow(null)
    val resultState: Flow<OperationResult<GroupModel>?> = _resultState

    fun emptyState() {
        viewModelScope.launch { _resultState.emit(null) }
    }

    fun addGroup(group: GroupModel) {
        viewModelScope.launch {
            flow {
                try {
                    val id = groupDao.insertGroup(group.toGroup())
                    val copy = group.copy(id=id)
                    emit(OperationResult(copy,null))
                }
                catch (ex: Throwable) {
                    emit(OperationResult(null,ex))
                }
            }
                .flowOn(Dispatchers.IO)
                .collect { _resultState.emit(it) }
        }
    }

    fun setGroup(group: GroupModel) {
        viewModelScope.launch {
            flow {
                try {
                    val copy = group.copy()
                    groupDao.updateGroup(group.toGroup())
                    emit(OperationResult(copy,null))
                }
                catch (ex: Throwable) {
                    emit(OperationResult(null,ex))
                }
            }
                .flowOn(Dispatchers.IO)
                .collect { _resultState.emit(it) }
        }
    }
}