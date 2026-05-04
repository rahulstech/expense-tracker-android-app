package rahulstech.android.expensetracker.domain

import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History

interface RestoreRepository {

    suspend fun addAccounts(accounts: List<Account>)

    suspend fun addGroups(groups: List<Group>)

    suspend fun addHistories(histories: List<History>)
}
