package rahulstech.android.expensetracker.domain

import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History

interface BackupRepository {

    fun getMultipleAccounts(): List<Account>

    fun getMultipleGroups(): List<Group>

    fun getMultipleHistories(size: Int, skip: Long = 0): List<History>
}