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
        val _account = account.copy(lastUsed = LocalDateTime.now(), totalUsed = 1)
        val id = accountDao.insert(_account.toAccountEntity())
        cache.setAccountTotalUsed(id, 1)
        return _account.copy(id = id)
    }

    override fun getAccountById(id: Long): Flow<Account?> {
        return combine(accountDao.findAccountByIdFlow(id), cache.getDefaultAccountIdFlow()) { entity, defaultId ->
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

    override fun getTotalBalance(): Flow<Double> {
        return analyticsDao.getTotalAccountBalance().flowOn(Dispatchers.IO)
    }

    override suspend fun editAccount(account: Account): Boolean {
        val totalUsed = cache.getAccountTotalUsed(account.id)
        val _account = account.copy(lastUsed = LocalDateTime.now(), totalUsed = totalUsed + 1)
        accountDao.update(_account.toAccountEntity())
        cache.setAccountTotalUsed(account.id, _account.totalUsed)
        return true
    }

    override suspend fun creditAccountBalance(id: Long, amount: Number) {
        val account = getAccountById(id).first()
        account?.let {
            val updatedAccount = it.copy(balance = it.balance + amount.toDouble())
            editAccount(updatedAccount)
        }
    }

    override suspend fun debitAccountBalance(id: Long, amount: Number) {
        val account = getAccountById(id).first()
        account?.let {
            val updatedAccount = it.copy(balance = it.balance - amount.toDouble())
            editAccount(updatedAccount)
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
