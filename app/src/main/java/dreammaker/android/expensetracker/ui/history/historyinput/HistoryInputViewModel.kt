package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.History
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.PersonDao
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.ui.account.IAccountChooserViewModel
import dreammaker.android.expensetracker.ui.person.IPersonChooserViewModel
import dreammaker.android.expensetracker.ui.util.SelectionStore
import kotlinx.coroutines.flow.MutableStateFlow

class HistoryInputViewModel(app: Application) : AndroidViewModel(app), IAccountChooserViewModel, IPersonChooserViewModel {

    private val historyDao: HistoryDao
    private val accountDao: AccountDao
    private val personDao: PersonDao

    override var accountSelectionStore: SelectionStore<Long>? = null

    override var personSelectionStore: SelectionStore<Long>? = null

    private lateinit var allAccount: LiveData<List<AccountModel>>

    private lateinit var allPeople: LiveData<List<PersonModel>>

    val selectedSrcAccount = MutableStateFlow<AccountModel?>(null)

    val selectedDestAccount = MutableStateFlow<AccountModel?>(null)

    val selectedSrcPerson = MutableStateFlow<PersonModel?>(null)

    val selectedDestPerson = MutableStateFlow<PersonModel?>(null)

    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
        accountDao = db.accountDao
        personDao = db.personDao
    }

    fun addHistory(history: History) {
        val id = historyDao.insertHistory(history)
    }

    override fun getAllAccounts(): LiveData<List<AccountModel>> {
        if (!::allAccount.isInitialized) {
            allAccount = accountDao.getAllAccounts()
        }
        return allAccount
    }

    override fun getAllPeople(): LiveData<List<PersonModel>> {
        if (!::allPeople.isInitialized) {
            allPeople = personDao.getAllPeople()
        }
        return allPeople
    }
}