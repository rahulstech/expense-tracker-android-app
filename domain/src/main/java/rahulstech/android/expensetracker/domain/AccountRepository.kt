package rahulstech.android.expensetracker.domain

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.domain.model.Account

interface AccountRepository{

    fun insertAccount(account: Account): Account

    fun getLiveAccountById(id: Long): LiveData<Account?>

    fun findAccountById(id: Long): Account?

    fun getLiveAllAccounts(): LiveData<List<Account>>

    fun getLiveRecentlyUsedAccounts(count: Int = 3): LiveData<List<Account>>

    fun getLiveTotalBalance(): LiveData<Double>

    fun updateAccount(account: Account): Boolean

    fun creditBalance(id: Long, amount: Number)

    fun debitBalance(id: Long, amount: Number)

    fun deleteAccount(id: Long)

    fun deleteMultipleAccounts(ids: List<Long>)


    fun getDefaultAccount(): Flow<Account?>

    fun hasDefaultAccount(): Boolean

    fun setDefaultAccount(account: Account)

    fun removeDefaultAccount()
}
