package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.History
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.PersonModel

class HistoryInputViewModel(app: Application) : AndroidViewModel(app) {

    private val historyDao: HistoryDao
    private val selections: MutableMap<String, LiveData<Any?>> = mutableMapOf()
    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
    }

    fun addHistory(history: History) {}

    fun getAccount(key: String): AccountModel? = getSelectionLiveData(key).value as AccountModel?

    fun getPerson(key: String): PersonModel? = getSelectionLiveData(key).value as PersonModel?

    fun setSelection(key: String, value: Any?) {
        (getSelectionLiveData(key) as MutableLiveData).postValue(value)
    }

    fun getSelection(key: String, defaultValue: Any? = null): Any? = getSelectionLiveData(key,defaultValue).value

    fun getSelectionLiveData(key: String, defaultValue: Any? = null): LiveData<Any?> {
        if (!selections.containsKey(key)) {
            selections[key] = MutableLiveData(defaultValue)
        }
        return selections[key]!!
    }


}