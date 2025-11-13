package rahulstech.android.expensetracker.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.AccountDao
import dreammaker.android.expensetracker.database.dao.GroupDao
import dreammaker.android.expensetracker.database.dao.HistoryDao
import dreammaker.android.expensetracker.database.model.HistoryType
import kotlinx.coroutines.flow.map
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.toHistory

class HistoryRepository(
    private val db: IExpenseDatabase
) {

    companion object {
        private const val UPDATE_CREDIT = 0
        private const val UPDATE_DEBIT = 1
    }

    private val accountDao: AccountDao = db.accountDao
    private val groupDao: GroupDao = db.groupDao
    private val historyDao: HistoryDao = db.historyDao

    fun insertHistory(history: History) {
        db.runInTransaction {
            // insert history
            val id = historyDao.insertHistory(history.toHistoryEntity())

            history.apply {
                primaryAccountId?.let { id ->
                    updateAccountBalance(id,history.amount,getUpdateType(history.type))
                }

                secondaryAccountId?.let { id ->
                    updateAccountBalance(id,history.amount,getUpdateType(history.type,false))
                }

                groupId?.let { id ->
                    updateGroupDue(
                        id,history.amount,getUpdateType(history.type))
                }
            }
        }
    }

    fun findLiveHistoryById(id: Long): LiveData<History?> {
        return historyDao.findHistoryDetailsByIdAsFlow(id).map { details ->
            details?.toHistory()
        }
            .asLiveData()
    }

    fun updateHistory(history: History) {
        db.runInTransaction {

            val oldHistory = historyDao.findHistoryById(history.id)

            val changes = historyDao.updateHistory(history.toHistoryEntity())

            oldHistory.apply {
                primaryAccountId?.let { id ->
                    updateAccountBalance(id,oldHistory.amount,getReverseUpdateType(oldHistory.type))
                }
                secondaryAccountId?.let { id ->
                    updateAccountBalance(id,oldHistory.amount,getReverseUpdateType(oldHistory.type,false))
                }
                groupId?.let { id ->
                    updateGroupDue(id,oldHistory.amount,getReverseUpdateType(oldHistory.type))
                }
            }

            history.apply {
                primaryAccountId?.let { id ->
                    updateAccountBalance(id,oldHistory.amount,getUpdateType(oldHistory.type))
                }
                secondaryAccountId?.let { id ->
                    updateAccountBalance(id,oldHistory.amount,getUpdateType(oldHistory.type,false))
                }
                groupId?.let { id ->
                    updateGroupDue(id,oldHistory.amount,getUpdateType(oldHistory.type))
                }
            }
        }
    }

    fun deleteHistory() {}

    fun deleteMultipleHistories(ids: List<Long>) {}


    // utility methods

    private fun updateAccountBalance(id: Long, amount: Float, type: Int) {
        val account = accountDao.findAccountById(id)
        val updated = account?.let {
            when(type) {
                UPDATE_CREDIT -> account.copy(balance = account.balance - amount)
                UPDATE_DEBIT -> account.copy(balance = account.balance + amount)
                else -> null
            }
        }
        updated?.let {
            accountDao.updateAccount(it)
        }
    }

    private fun updateGroupDue(id: Long, amount: Float, type: Int) {
        val group = groupDao.findGroupById(id)
        val updated = group?.let {
            when(type) {
                UPDATE_CREDIT -> group.copy(due = group.due - amount)
                UPDATE_DEBIT -> group.copy(due = group.due + amount)
                else -> null
            }
        }
        updated?.let {
            groupDao.updateGroup(it)
        }
    }

    private fun getUpdateType(type: HistoryType, primary: Boolean = true) =
        if (primary) {
            when(type) {
                HistoryType.DEBIT -> UPDATE_DEBIT
                HistoryType.CREDIT -> UPDATE_CREDIT
                HistoryType.TRANSFER -> UPDATE_DEBIT
            }
        }
        else {
            when(type) {
                HistoryType.DEBIT -> UPDATE_CREDIT
                HistoryType.CREDIT -> UPDATE_DEBIT
                HistoryType.TRANSFER -> UPDATE_CREDIT
            }
        }

    private fun getReverseUpdateType(type: HistoryType, primary: Boolean = true): Int {
        val updateType = getUpdateType(type,primary)
        return when (updateType) {
            UPDATE_CREDIT -> UPDATE_DEBIT
            else -> UPDATE_CREDIT
        }
    }

}