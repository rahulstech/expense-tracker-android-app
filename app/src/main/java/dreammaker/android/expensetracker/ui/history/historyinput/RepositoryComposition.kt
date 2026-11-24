package dreammaker.android.expensetracker.ui.history.historyinput

import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import javax.inject.Inject

class RepositoryComposition @Inject constructor(
    val accountRepo: AccountRepository,
    val groupRepo: GroupRepository,
    val historyRepo: HistoryRepository
) {

    fun loadHistoryById(id: Long) {}

    fun saveHistory(history: History) {}

    fun loadAccounts() {}

    fun loadGroups() {}
}