package dreammaker.android.expensetracker.ui.person.inputperson

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.PersonDao
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.ui.util.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class PersonInputViewModel(app: Application): AndroidViewModel(app) {

    private val personDao: PersonDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        personDao = db.personDao
    }

    lateinit var personLiveData: LiveData<PersonModel?>

    fun getStoredPerson(): PersonModel? {
        if (!::personLiveData.isInitialized) {
            return null
        }
        return personLiveData.value
    }

    fun findPersonById(id: Long): LiveData<PersonModel?> {
        if (!::personLiveData.isInitialized) {
            personLiveData = personDao.findPersonById(id)
        }
        return personLiveData
    }

    private val _resultState: MutableStateFlow<OperationResult<PersonModel>?> = MutableStateFlow(null)
    val resultState: Flow<OperationResult<PersonModel>?> = _resultState

    fun emptyState() {
        viewModelScope.launch { _resultState.emit(null) }
    }

    fun addPerson(person: PersonModel) {
        viewModelScope.launch {
            flow {
                try {
                    val id = personDao.insertPerson(person.toPerson())
                    val copy = person.copy(id=id)
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

    fun setPerson(person: PersonModel) {
        viewModelScope.launch {
            flow {
                try {
                    val copy = person.copy()
                    personDao.updatePerson(person.toPerson())
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