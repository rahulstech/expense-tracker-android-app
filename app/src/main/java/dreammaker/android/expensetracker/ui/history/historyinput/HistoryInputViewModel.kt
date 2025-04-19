package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.History
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.PersonModel

class HistoryInputViewModel(app: Application) : AndroidViewModel(app) {

    val historyDao: HistoryDao

    val srcAccount: MutableLiveData<AccountModel?> = MutableLiveData(null)

    val destAccount: MutableLiveData<AccountModel?> = MutableLiveData(null)

    val srcPerson: MutableLiveData<PersonModel?> = MutableLiveData(null)

    val destPerson: MutableLiveData<PersonModel?> = MutableLiveData(null)

    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
    }

    fun addHistory(history: History) {
        val id = historyDao.insertHistory(history)
    }

    fun setAccount(key: String, account: AccountModel?) {
        when(key) {
            HistoryInputFragment.ARG_SOURCE -> srcAccount.value = account
            HistoryInputFragment.ARG_DESTINATION -> destAccount.value = account
        }
    }

    fun getAccount(key: String): AccountModel? {
        return when(key) {
            HistoryInputFragment.ARG_SOURCE -> srcAccount.value
            HistoryInputFragment.ARG_DESTINATION -> destAccount.value
            else -> null
        }
    }

    fun setPerson(key: String, person: PersonModel?) {
        when(key) {
            HistoryInputFragment.ARG_SOURCE -> srcPerson.value = person
            HistoryInputFragment.ARG_DESTINATION -> destPerson.value = person
        }
    }

    fun getPerson(key: String): PersonModel? {
        return when(key) {
            HistoryInputFragment.ARG_SOURCE -> srcPerson.value
            HistoryInputFragment.ARG_DESTINATION -> destPerson.value
            else -> null
        }
    }
}