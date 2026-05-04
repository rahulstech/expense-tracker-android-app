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
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    db: IExpenseDatabase,
    private val cache: LocalCache,
): BackupRepository {

    private val accountDao = db.accountDao
    private val groupDao = db.groupDao
    private val historyDao = db.historyDao

    override suspend fun getAccounts(): List<Account> {
        val defaultAccountId = cache.getDefaultAccountId()
        return accountDao.getAllAccounts()
            .map { entity ->
                val account = entity.toAccount()
                if (account.id == defaultAccountId) {
                    account.copy(isDefault = true)
                } else {
                    account
                }
            }
    }

    override suspend fun getGroups(): List<Group> {
        return groupDao.getAllGroups()
            .map { entity -> entity.toGroup() }
    }

    override suspend fun getHistories(size: Int, skip: Long): List<History> {
        return historyDao.getHistories(size, skip)
            .map { entity -> entity.toHistory() }
    }
}
