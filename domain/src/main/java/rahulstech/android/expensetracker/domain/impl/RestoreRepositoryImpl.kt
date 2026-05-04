package rahulstech.android.expensetracker.domain.impl

import dreammaker.android.expensetracker.database.IExpenseDatabase
import rahulstech.android.expensetracker.domain.LocalCache
import rahulstech.android.expensetracker.domain.RestoreRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import javax.inject.Inject

class RestoreRepositoryImpl @Inject constructor(
    private val db: IExpenseDatabase,
    private val cache: LocalCache,
): RestoreRepository {

    override suspend fun addAccounts(accounts: List<Account>) {
        if (accounts.isEmpty()) return
        val defaultAccount = accounts.find { it.isDefault }
        val entities = accounts.map { it.toAccountEntity() }
        db.accountDao.insertMultiple(entities)
        defaultAccount?.let { cache.setDefaultAccountId(it.id) }
    }

    override suspend fun addGroups(groups: List<Group>) {
        if (groups.isEmpty()) return
        val entities = groups.map { it.toGroupEntity() }
        db.groupDao.insertMultiple(entities)
    }

    override suspend fun addHistories(histories: List<History>) {
        if (histories.isEmpty()) return
        val entities = histories.map { it.toHistoryEntity() }
        db.historyDao.insertMultiple(entities)
    }
}