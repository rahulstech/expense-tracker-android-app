package rahulstech.android.expensetracker.domain.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.AccountDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.LocalCache
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.toAccount
import java.time.LocalDateTime

internal class AccountRepositoryImpl(
    db: IExpenseDatabase,
    private val cache: LocalCache
): AccountRepository {

    private val accountDao: AccountDao = db.accountDao

    override fun insertAccount(account: Account): Account {
        val _account = account.copy(lastUsed = LocalDateTime.now(), totalUsed = 1)
        val id = accountDao.insertAccount(_account.toAccountEntity())
        cache.setAccountTotalUsed(account.id,1)
        return _account.copy(id=id)
    }

    override fun findAccountById(id: Long): Account? =
        accountDao.findAccountById(id)?.toAccount(isDefault = cache.getDefaultAccountId() == id)

    override fun getLiveAccountById(id: Long): LiveData<Account?> {
        return combine(accountDao.getFlowAccountById(id), cache.getDefaultAccountIdFlow()) { account, defaultId ->
            account?.toAccount(isDefault = account.id == defaultId)
        }
            .flowOn(Dispatchers.IO)
            .asLiveData()
    }

    override fun getLiveAllAccounts(): LiveData<List<Account>> =
        accountDao.getLiveAllAccounts().map{ entities -> entities.map { it.toAccount() }}

    override fun getLiveRecentlyUsedAccounts(count: Int): LiveData<List<Account>> =
        accountDao.getLiveRecentlyUsedAccounts(count).map { entities -> entities.map { it.toAccount() } }

    override fun getLiveTotalBalance(): LiveData<Double> =
        accountDao.getLiveTotalBalance().map { totalBalance -> totalBalance ?: 0.toDouble() }

    override fun updateAccount(account: Account): Boolean {
        val _account = account.copy(lastUsed = LocalDateTime.now(), totalUsed = cache.getAccountTotalUsed(account.id)+1)
        val changes = accountDao.updateAccount(_account.toAccountEntity())
        if (changes == 1) {
            cache.setAccountTotalUsed(account.id,_account.totalUsed)
            return true
        }
        return false
    }

    override fun creditBalance(id: Long, amount: Number) {
        val account = accountDao.findAccountById(id)
        account?.let {
            val account = it.toAccount()
            val updatedAccount = account.copy(balance = account.balance + amount.toFloat())
            updateAccount(updatedAccount)
        }
    }

    override fun debitBalance(id: Long, amount: Number) {
        val account = accountDao.findAccountById(id)
        account?.let {
            val account = it.toAccount()
            val updatedAccount = account.copy(balance = account.balance - amount.toFloat())
            updateAccount(updatedAccount)
        }
    }

    override fun deleteAccount(id: Long) {
        accountDao.deleteAccount(id)
        cache.removeAccountTotalUsed(id)
    }

    override fun deleteMultipleAccounts(ids: List<Long>) {
        accountDao.deleteMultipleAccounts(ids)
        ids.forEach { cache.removeAccountTotalUsed(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDefaultAccount(): Flow<Account?> = cache.getDefaultAccountIdFlow().flatMapLatest { id ->
        if (null == id) {
            emptyFlow()
        }
        else {
            val account = findAccountById(id)
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