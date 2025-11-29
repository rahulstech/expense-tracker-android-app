package rahulstech.android.expensetracker.domain

import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History

interface RestoreRepository {

    fun insertMultipleAccounts(accounts: List<Account>)

    fun insertMultipleGroups(groups: List<Group>)

    fun insertMultipleHistories(histories: List<History>)
}