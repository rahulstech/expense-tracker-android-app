package rahulstech.android.expensetracker.domain.impl

import dreammaker.android.expensetracker.database.IExpenseDatabase
import rahulstech.android.expensetracker.domain.LocalCache
import rahulstech.android.expensetracker.domain.RestoreRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History

internal class RestoreRepositoryImpl(
    private val db: IExpenseDatabase,
    private val cache: LocalCache,
): RestoreRepository {

    override fun insertMultipleAccounts(accounts: List<Account>) {
        if (accounts.isEmpty()) return
        val defaultAccount = accounts.find { it.isDefault }
        val entities = accounts.map {
            it.toAccountEntity()
        }
        db.accountDao.insertAccounts(entities)
        defaultAccount?.let { cache.setDefaultAccount(it) }
    }

    override fun insertMultipleGroups(groups: List<Group>) {
        if (groups.isEmpty()) return
        val defaultGroup = groups.find { it.isDefault }
        val entities = groups.map { it.toGroupEntity() }
        db.groupDao.insertGroups(entities)
        defaultGroup?.let { cache.setDefaultGroup(it) }
    }

    override fun insertMultipleHistories(histories: List<History>) {
        if (histories.isEmpty()) return
        val entities = histories.map { it.toHistoryEntity() }
        db.historyDao.insertHistories(entities)
    }
}