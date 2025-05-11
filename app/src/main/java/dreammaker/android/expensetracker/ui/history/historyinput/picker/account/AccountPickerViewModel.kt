package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase


class AccountPickerViewModel(app: Application): AndroidViewModel(app) {

    private val accountDao: AccountDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        accountDao = db.accountDao
    }

    private lateinit var allAccount: LiveData<List<AccountModel>>

    fun getAllAccounts(): LiveData<List<AccountModel>> {
        if (!::allAccount.isInitialized) {
            allAccount = accountDao.getAllAccounts()
        }
        return allAccount
    }
}
