package rahulstech.android.expensetracker.domain.impl

import dreammaker.android.expensetracker.database.IExpenseDatabase
import rahulstech.android.expensetracker.domain.BackupRepository
import rahulstech.android.expensetracker.domain.LocalCache
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.toAccount
import rahulstech.android.expensetracker.domain.model.toGroup
import rahulstech.android.expensetracker.domain.model.toHistory

internal class BackupRepositoryImpl(
    private val db: IExpenseDatabase,
    private val cache: LocalCache,
): BackupRepository {

    override fun getMultipleAccounts(): List<Account> {
        val defaultAccountId = cache.getDefaultAccountId()
        return db.accountDao.getAllAccounts()
            .map { entity ->
                val account = entity.toAccount()
                if (account.id == defaultAccountId) {
                    account.copy(isDefault = true)
                }
                else {
                    account
                }
            }
    }

    override fun getMultipleGroups(): List<Group> {
        val defaultGroupId = cache.getDefaultGroupId()
        return db.groupDao.getAllGroups()
            .map { entity ->
                val group = entity.toGroup()
                if (group.id == defaultGroupId) {
                    group.copy(isDefault = true)
                }
                else {
                    group
                }
            }
    }

    override fun getMultipleHistories(
        size: Int,
        skip: Long
    ): List<History> {
        return db.historyDao.getHistories(size,skip)
            .map { entity -> entity.toHistory() }
    }
}