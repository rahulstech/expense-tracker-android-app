package dreammaker.android.expensetracker.ui.person.viewperson

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

class ViewPersonViewModel(app: Application): AndroidViewModel(app) {

    private val personDao: PersonDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        personDao = db.personDao
    }

    private lateinit var personLiveDate: LiveData<PersonModel?>

    fun findPersonById(id: Long): LiveData<PersonModel?> {
        if (!::personLiveDate.isInitialized) {
            personLiveDate = personDao.findPersonById(id)
        }
        return personLiveDate
    }

    private val _resultState = MutableStateFlow<OperationResult<PersonModel>?>(null)

    val resultState: Flow<OperationResult<PersonModel>?> = _resultState

    fun emptyResult() {
        viewModelScope.launch { _resultState.emit(null) }
    }

    fun removePerson(person: PersonModel) {
        viewModelScope.launch {
            flow {
                try {
                    val copy = person.copy()
                    personDao.deletePerson(person.toPerson())
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