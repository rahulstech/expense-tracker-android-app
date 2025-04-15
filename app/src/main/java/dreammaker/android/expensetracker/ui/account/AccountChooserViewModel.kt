package dreammaker.android.expensetracker.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.ui.util.SelectionStore

class AccountChooserViewModel(val app: Application): AndroidViewModel(app) {

    private val accountDao: AccountDao

    private lateinit var allAccounts: LiveData<List<AccountModel>>

    var selectionStore: SelectionStore<Long>? = null

    init {
        val db = ExpensesDatabase.getInstance(app)
        accountDao = db.accountDao
    }

    fun getAllAccounts(): LiveData<List<AccountModel>> {
        if (!::allAccounts.isInitialized) {
            allAccounts = accountDao.getAllAccounts()
        }
        return allAccounts
    }
}