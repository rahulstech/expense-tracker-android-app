package rahulstech.android.expensetracker.domain.impl

import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.AccountDao
import dreammaker.android.expensetracker.database.dao.AnalyticsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.LocalCache
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.toAccount
import java.time.LocalDateTime
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    db: IExpenseDatabase,
    private val cache: LocalCache
): AccountRepository {

    private val accountDao: AccountDao = db.accountDao
    private val analyticsDao: AnalyticsDao = db.analyticsDao

    // --- New Coroutine and Flow based methods ---

    override suspend fun createAccount(account: Account): Account {
        val newAccount = account.copy(lastUsed = LocalDateTime.now(), totalUsed = 1)
        val id = accountDao.insert(newAccount.toAccountEntity())
        return newAccount.copy(id = id)
    }

    override fun getAccountById(id: Long): Flow<Account?> {
        return combine(accountDao.findByIdFlow(id), cache.getDefaultAccountIdFlow()) { entity, defaultId ->
            entity?.toAccount(isDefault = entity.id == defaultId)
        }.flowOn(Dispatchers.IO)
    }

    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAccountsFlow().map { entities ->
            entities.map { it.toAccount() }
        }
    }

    override fun getRecentlyUsedAccounts(count: Int): Flow<List<Account>> {
        return accountDao.getLastUsedAccountsFlow(count).map { entities ->
            entities.map { it.toAccount() }
        }
    }

    override fun getThreeFrequentlyUsedAccounts(): Flow<List<Account>> {
        return accountDao.getFrequentlyUserAccountsFlow(3).map { entities ->
            entities.map { it.toAccount() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getTotalBalance(): Flow<Double> = analyticsDao.getTotalAccountBalance()

    override suspend fun editAccount(account: Account): Boolean {
        val updatedAccount = account.copy(lastUsed = LocalDateTime.now())
        accountDao.update(updatedAccount.toAccountEntity())
        return true
    }

    override suspend fun creditAccountBalance(id: Long, amount: Double) {
        val entity = accountDao.findByIdFlow(id).first()
        entity?.let {
            val updatedAccount = it.copy(
                balance = it.balance + amount,
                lastUsed = LocalDateTime.now(),
                totalUsed = it.totalUsed?.let { totalUsed -> totalUsed+1 } ?: 1
            )
            accountDao.update(updatedAccount)
        }
    }

    override suspend fun debitAccountBalance(id: Long, amount: Double) {
        val entity = accountDao.findByIdFlow(id).first()
        entity?.let {
            val updatedAccount = it.copy(
                balance = it.balance - amount,
                lastUsed = LocalDateTime.now(),
                totalUsed = it.totalUsed?.let { totalUsed -> totalUsed+1 } ?: 1
            )
            accountDao.update(updatedAccount)
        }
    }

    override suspend fun removeAccount(id: Long) {
        accountDao.deleteById(id)
        cache.removeAccountTotalUsed(id)
    }

    override suspend fun removeMultipleAccounts(ids: List<Long>) {
        accountDao.deleteMultipleByIds(ids)
        ids.forEach { cache.removeAccountTotalUsed(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDefaultAccount(): Flow<Account?> = cache.getDefaultAccountIdFlow().flatMapLatest { id ->
        if (null == id) {
            emptyFlow()
        }
        else {
            val account = getAccountById(id).first()
            flowOf(account)
        }
    }
        .flowOn(Dispatchers.IO)

    override fun hasDefaultAccount(): Boolean = null != cache.getDefaultAccountId()

    override fun setDefaultAccount(account: Account) {
        cache.setDefaultAccountId(account.id)
    }

    override fun removeDefaultAccount() {
        cache.removeDefaultAccount()
    }
}
