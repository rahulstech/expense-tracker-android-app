package rahulstech.android.expensetracker.domain

import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.domain.model.Account

interface AccountRepository{

    fun getDefaultAccount(): Flow<Account?>

    fun hasDefaultAccount(): Boolean

    fun setDefaultAccount(account: Account)

    fun removeDefaultAccount()

    // --- New Coroutine and Flow based methods ---

    suspend fun createAccount(account: Account): Account

    fun getAccountById(id: Long): Flow<Account?>

    fun getAllAccounts(): Flow<List<Account>>

    fun getRecentlyUsedAccounts(count: Int = 3): Flow<List<Account>>

    fun getTotalBalance(): Flow<Double>

    suspend fun editAccount(account: Account): Boolean

    suspend fun creditAccountBalance(id: Long, amount: Number)

    suspend fun debitAccountBalance(id: Long, amount: Number)

    suspend fun removeAccount(id: Long)

    suspend fun removeMultipleAccounts(ids: List<Long>)
}
