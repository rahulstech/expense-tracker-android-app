package rahulstech.android.expensetracker.domain.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.AccountDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
        accountDao.findAccountById(id)?.toAccount()

    override fun getLiveAccountById(id: Long): LiveData<Account?> =
        accountDao.getLiveAccountById(id).map { it?.toAccount() }

    override fun getLiveAllAccounts(): LiveData<List<Account>> =
        accountDao.getLiveAllAccounts().map{ entities -> entities.map { it.toAccount() }}

    override fun getLiveRecentlyUsedAccounts(count: Int): LiveData<List<Account>> =
        accountDao.getLiveRecentlyUsedAccounts(count).map { entities -> entities.map { it.toAccount() } }

    override fun getLiveTotalBalance(): LiveData<Double> =
        accountDao.getLiveTotalBalance().map { totalBalance -> totalBalance ?: 0.toDouble() }

//    override fun getDefaultAccount(): Flow<Account?> = flow {
//        cache.getDefaultAccount()?.let { id ->
//            val account = findAccountById(id)
//            // if default account deleted from database then also delete from the cache
//            if (null == account) {
//                cache.removeDefaultAccount()
//                emit(null)
//            }
//            else {
//                emit(account)
//            }
//        }
//    }

    override fun getDefaultAccount(): Flow<Account?> = emptyFlow()

    override fun hasDefaultAccount(): Boolean = null != cache.getDefaultAccountId()

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
            val updatedAccount = account.copy(balance = account.balance.toFloat() - amount.toFloat())
            updateAccount(updatedAccount)
        }
    }

    override fun changeDefaultAccount(account: Account?) {
//        if (null == account) {
//            cache.removeDefaultAccount()
//        }
//        else {
//            cache.setDefaultAccount(account.id)
//        }
    }

    override fun deleteAccount(id: Long) {
        accountDao.deleteAccount(id)
        cache.removeAccountTotalUsed(id)
    }

    override fun deleteMultipleAccounts(ids: List<Long>) {
        accountDao.deleteMultipleAccounts(ids)
        ids.forEach { cache.removeAccountTotalUsed(it) }
    }
}