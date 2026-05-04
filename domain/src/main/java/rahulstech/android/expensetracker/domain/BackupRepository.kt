package rahulstech.android.expensetracker.domain

import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History

interface BackupRepository {

    suspend fun getAccounts(): List<Account>

    suspend fun getGroups(): List<Group>

    suspend fun getHistories(size: Int, skip: Long = 0): List<History>
}