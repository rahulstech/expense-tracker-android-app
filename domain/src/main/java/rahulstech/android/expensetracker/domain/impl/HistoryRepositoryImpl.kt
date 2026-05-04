package rahulstech.android.expensetracker.domain.impl

import androidx.paging.PagingData
import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.HistoryDao
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.HistoryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.HistoryFilterParameters
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.HistoryTotalCreditTotalDebit
import rahulstech.android.expensetracker.domain.model.toHistory
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val db: IExpenseDatabase,
    private val accountRepository: AccountRepository,
    private val groupRepository: GroupRepository,
): HistoryRepository {

    private val historyDao: HistoryDao = db.historyDao

    override fun getPagedHistories(params: HistoryFilterParameters): Flow<PagingData<History>> =
        params.getPagedHistories(historyDao)

    override fun getTotalCreditDebit(params: HistoryFilterParameters): Flow<HistoryTotalCreditTotalDebit> =
        params.getTotalCreditDebit(historyDao)

    // --- New Coroutine and Flow based methods ---

    override suspend fun createHistory(history: History): History {
        return db.runInTransaction {
            val entity = history.toHistoryEntity()
            val id = historyDao.insert(entity)
            
            updateAccountBalanceSync(entity)
            updateAccountBalanceSync(entity, false)
            updateGroupDueSync(entity)
            
            history.id = id
            history
        }
    }

    override suspend fun getHistory(id: Long): History? {
        return historyDao.findHistoryDetailsByIdFlow(id).first()?.toHistory()
    }

    override fun getHistoryById(id: Long): Flow<History?> {
        return historyDao.findHistoryDetailsByIdFlow(id).map { it?.toHistory() }.flowOn(Dispatchers.IO)
    }

    override suspend fun editHistory(history: History): Boolean {
        return db.runInTransaction {
            val oldEntity = historyDao.findHistoryDetailsByIdFlow(history.id).first()?.history ?: return@runInTransaction false

            val newEntity = history.toHistoryEntity()
            val changes = historyDao.update(newEntity)

            if (changes == 1) {
                resetAccountBalanceSync(oldEntity)
                resetAccountBalanceSync(oldEntity, false)
                resetGroupDueSync(oldEntity)

                updateAccountBalanceSync(newEntity)
                updateAccountBalanceSync(newEntity, false)
                updateGroupDueSync(newEntity)

                return@runInTransaction true
            }
            false
        }
    }

    override suspend fun removeHistory(id: Long, reset: Boolean) {
        db.runInTransaction {
            if (reset) {
                val entity = historyDao.findHistoryDetailsByIdFlow(id).first()?.history ?: return@runInTransaction
                if (1 == historyDao.delete(id)) {
                    resetAccountBalanceSync(entity)
                    resetAccountBalanceSync(entity, false)
                    resetGroupDueSync(entity)
                }
            } else {
                historyDao.delete(id)
            }
        }
    }

    override suspend fun removeMultipleHistories(ids: List<Long>, reset: Boolean) {
        if (reset) {
            ids.forEach { removeHistory(it, true) }
        } else {
            historyDao.deleteMultiple(ids)
        }
    }

    // utility methods (Sync versions for use inside Transactions)

    private suspend fun updateAccountBalanceSync(history: HistoryEntity, primary: Boolean = true) {
        if (primary) {
            when(history.type) {
                HistoryType.CREDIT -> history.primaryAccountId?.let { accountRepository.creditAccountBalance(it, history.amount) }
                HistoryType.DEBIT, HistoryType.TRANSFER -> history.primaryAccountId?.let { accountRepository.debitAccountBalance(it, history.amount) }
            }
        } else {
            if (history.type == HistoryType.TRANSFER) {
                history.secondaryAccountId?.let { accountRepository.debitAccountBalance(it, history.amount) }
            }
        }
    }

    private suspend fun resetAccountBalanceSync(history: HistoryEntity, primary: Boolean = true) {
        if (primary) {
            when(history.type) {
                HistoryType.CREDIT -> history.primaryAccountId?.let { accountRepository.debitAccountBalance(it, history.amount) }
                HistoryType.DEBIT, HistoryType.TRANSFER -> history.primaryAccountId?.let { accountRepository.creditAccountBalance(it, history.amount) }
            }
        } else {
            if (history.type == HistoryType.TRANSFER) {
                history.secondaryAccountId?.let { accountRepository.debitAccountBalance(it, history.amount) }
            }
        }
    }

    private suspend fun updateGroupDueSync(history: HistoryEntity) {
        when(history.type) {
            HistoryType.CREDIT -> history.groupId?.let { groupRepository.debitGroupDue(it, history.amount) }
            HistoryType.DEBIT -> history.groupId?.let { groupRepository.creditGroupDue(it, history.amount) }
            else -> {}
        }
    }

    private suspend fun resetGroupDueSync(history: HistoryEntity) {
        when(history.type) {
            HistoryType.CREDIT -> history.groupId?.let { groupRepository.creditGroupDue(it, history.amount) }
            HistoryType.DEBIT -> history.groupId?.let { groupRepository.debitGroupDue(it, history.amount) }
            else -> {}
        }
    }
}
