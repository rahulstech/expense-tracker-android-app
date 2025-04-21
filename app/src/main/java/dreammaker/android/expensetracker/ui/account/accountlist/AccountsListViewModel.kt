package dreammaker.android.expensetracker.ui.account.accountlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase

class AccountsListViewModel(app: Application): AndroidViewModel(app) {
    private val accountDao: AccountDao
    init {
        val db = ExpensesDatabase.getInstance(app)
        accountDao = db.accountDao
    }

    private lateinit var accounts: LiveData<List<AccountModel>>

    fun getAllAccounts(): LiveData<List<AccountModel>> {
        if (!::accounts.isInitialized) {
            accounts = accountDao.getAllAccounts()
        }
        return accounts
    }
}