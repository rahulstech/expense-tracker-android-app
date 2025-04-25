package dreammaker.android.expensetracker.ui.person.personlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.GroupDao
import dreammaker.android.expensetracker.database.GroupModel

class GroupListViewModel(app: Application): AndroidViewModel(app) {

    private val groupDao: GroupDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        groupDao = db.groupDao
    }

    private lateinit var groups: LiveData<List<GroupModel>>

    fun getAllGroups(): LiveData<List<GroupModel>> {
        if (!::groups.isInitialized) {
            groups = groupDao.getAllGroups()
        }
        return groups
    }
}