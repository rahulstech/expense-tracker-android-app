package dreammaker.android.expensetracker.ui.person

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.PersonDao
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.ui.util.SelectionStore

class PersonChooserViewModel(app: Application): AndroidViewModel(app) {

    private lateinit var personDao: PersonDao
    private lateinit var allPeople: LiveData<List<PersonModel>>

    var selectionStore: SelectionStore<Long>? = null

    init {
        val db = ExpensesDatabase.getInstance(app)
        personDao = db.personDao
    }

    fun getAllPeople(): LiveData<List<PersonModel>> {
        if (!::allPeople.isInitialized) {
            allPeople = personDao.getAllPeople()
        }
        return allPeople
    }
}