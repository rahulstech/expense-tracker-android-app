package dreammaker.android.expensetracker.ui.group.viewgroup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.GroupDao
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.ui.util.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    private val _resultState = MutableStateFlow<OperationResult<GroupModel>?>(null)

    val resultState: Flow<OperationResult<GroupModel>?> = _resultState

    fun emptyResult() {
        viewModelScope.launch { _resultState.emit(null) }
    }

    fun removeGroup(group: GroupModel) {
        viewModelScope.launch {
            flow {
                try {
                    val copy = group.copy()
                    groupDao.deleteGroup(group.toGroup())
                    emit(OperationResult(copy,null))
                }
                catch (ex: Throwable) {
                    emit(OperationResult(null, ex))
                }
            }
                .flowOn(Dispatchers.IO)
                .collect { _resultState.emit(it) }
        }
    }
}