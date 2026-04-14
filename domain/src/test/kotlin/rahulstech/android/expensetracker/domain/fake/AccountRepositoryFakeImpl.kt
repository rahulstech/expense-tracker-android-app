package rahulstech.android.expensetracker.domain.fake

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.model.Account

class AccountRepositoryFakeImpl: AccountRepository {

    private val _accounts = mutableMapOf<Long, Account>(
        1L to Account("Account 1", 1000f, 1L),
        2L to Account("Account 2", 2000f, 2L)
    )
    val accounts: Map<Long, Account> = _accounts

    override fun insertAccount(account: Account): Account = account

    override fun getLiveAccountById(id: Long): LiveData<Account?> = MutableLiveData(accounts[id])

    override fun findAccountById(id: Long): Account? = accounts[id]

    override fun getLiveAllAccounts(): LiveData<List<Account>> = MutableLiveData(accounts.values.toList())

    override fun getLiveRecentlyUsedAccounts(count: Int): LiveData<List<Account>> = MutableLiveData(emptyList())

    override fun getLiveTotalBalance(): LiveData<Double> = MutableLiveData(0.00)

    override fun updateAccount(account: Account): Boolean = true

    override fun creditBalance(id: Long, amount: Number) {
        _accounts[id]?.let { account ->
            _accounts[id] = account.copy(balance = account.balance + amount.toFloat())
        }
    }

    override fun debitBalance(id: Long, amount: Number) {
        _accounts[id]?.let { account ->
            _accounts[id] = account.copy(balance = account.balance - amount.toFloat())
        }
    }

    override fun deleteAccount(id: Long) { }

    override fun deleteMultipleAccounts(ids: List<Long>) {}

    override fun getDefaultAccount(): Flow<Account?> = flowOf(null)

    override fun hasDefaultAccount(): Boolean = false

    override fun setDefaultAccount(account: Account) {}

    override fun removeDefaultAccount() {}
}