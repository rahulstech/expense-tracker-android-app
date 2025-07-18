package dreammaker.android.expensetracker.ui.history.historyinput.picker.group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.GroupDao
import dreammaker.android.expensetracker.database.GroupModel


class GroupPickerViewModel(app: Application): AndroidViewModel(app) {

    private val groupDao: GroupDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        groupDao = db.groupDao
    }

    private lateinit var allGroups: LiveData<List<GroupModel>>

    fun getAllGroups(): LiveData<List<GroupModel>> {
        if (!::allGroups.isInitialized) {
            allGroups = groupDao.getAllGroups()
        }
        return allGroups
    }
}
