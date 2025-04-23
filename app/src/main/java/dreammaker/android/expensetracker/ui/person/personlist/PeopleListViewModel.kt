package dreammaker.android.expensetracker.ui.person.personlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.PersonDao
import dreammaker.android.expensetracker.database.PersonModel

class PeopleListViewModel(app: Application): AndroidViewModel(app) {

    private val personDao: PersonDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        personDao = db.personDao
    }

    private lateinit var people: LiveData<List<PersonModel>>

    fun getAllPeople(): LiveData<List<PersonModel>> {
        if (!::people.isInitialized) {
            people = personDao.getAllPeople()
        }
        return people
    }
}