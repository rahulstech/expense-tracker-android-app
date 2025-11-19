package dreammaker.android.expensetracker.ui.history.historyinput.picker.group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Group

class GroupPickerViewModel(
    app: Application
): AndroidViewModel(app) {

    private val groupRepo = ExpenseRepository.getInstance(app).groupRepository

    private lateinit var allGroups: LiveData<List<Group>>

    fun getAllGroups(): LiveData<List<Group>> {
        if (!::allGroups.isInitialized) {
            allGroups = groupRepo.getLiveAllGroups()
        }
        return allGroups
    }
}
