package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.PersonDao
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.ui.util.SelectionStore

class PersonPickerViewModel(app: Application): AndroidViewModel(app) {

    private val personDao: PersonDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        personDao = db.personDao
    }

    var personSelectionStore: SelectionStore<Long>? = null

    private lateinit var allPeople: LiveData<List<PersonModel>>
    fun getAllPeople(): LiveData<List<PersonModel>> {
        if (!::allPeople.isInitialized) {
            allPeople = personDao.getAllPeople()
        }
        return allPeople
    }
}
