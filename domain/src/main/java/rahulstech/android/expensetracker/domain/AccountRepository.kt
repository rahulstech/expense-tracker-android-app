package rahulstech.android.expensetracker.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dreammaker.android.expensetracker.database.dao.AccountDao
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.toAccount

class AccountRepository(
    private val accountDao: AccountDao
) {

    fun insertAccount(account: Account): Account {
        val id = accountDao.insertAccount(account.toAccountEntity())
        return account.copy(id=id)
    }

    fun getLiveAccountById(id: Long): LiveData<Account?> = accountDao.getLiveAccountById(id).map { it?.toAccount() }

    fun getLiveAccounts(): LiveData<List<Account>> =
        accountDao.getLiveAccounts().map{ entities -> entities.map { it.toAccount() }}

    fun updateAccount(account: Account): Account? {
        val changes = accountDao.updateAccount(account.toAccountEntity())
        if (changes == 1) {
            return account.copy()
        }
        return null
    }

    fun deleteAccount(id: Long): Boolean {
        return 1 == accountDao.deleteAccount(id)
    }

    fun deleteMultipleAccounts(ids: List<Long>) {

    }
}
