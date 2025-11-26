package rahulstech.android.expensetracker.domain.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.PagingData
import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.HistoryDao
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.HistoryType
import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.HistoryFilterParameters
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.toHistory

internal class HistoryRepositoryImpl(
    private val db: IExpenseDatabase,
    private val accountRepository: AccountRepository,
    private val groupRepository: GroupRepository,
): HistoryRepository {

    private val historyDao: HistoryDao = db.historyDao

    override fun insertHistory(history: History): History =
        db.runInTransaction<History> {
            // insert history
            val entity = history.toHistoryEntity()
            val id = historyDao.insertHistory(entity)

            // update balance and due
            updateAccountBalance(entity)
            updateAccountBalance(entity,false)
            updateGroupDue(entity)

            history.id = id
            history
        }

    override fun findHistoryById(id: Long): History? =
        historyDao.findHistoryById(id)?.toHistory()

    override fun getLiveHistoryById(id: Long): LiveData<History?> =
        historyDao.getLiveHistoryById(id).map { it?.toHistory() }

    override fun getPagedHistories(params: HistoryFilterParameters): Flow<PagingData<History>> =
        params.getPagedHistories(historyDao)

    override fun updateHistory(history: History): Boolean =
        db.runInTransaction<Boolean> {
            val oldEntity = historyDao.findHistoryById(history.id)?.history
            if (null == oldEntity) {
                return@runInTransaction false
            }

            val newEntity = history.toHistoryEntity()
            val changes = historyDao.updateHistory(newEntity)

            if (changes == 1) {
                resetAccountBalance(oldEntity)
                resetAccountBalance(oldEntity,false)
                resetGroupDue(oldEntity)

                updateAccountBalance(newEntity)
                updateAccountBalance(newEntity,false)
                updateGroupDue(newEntity)

                return@runInTransaction true
            }
            false
        }

    override fun deleteHistory(id: Long, reset: Boolean) {
        db.runInTransaction {
            if (reset) {
                val entity = historyDao.findHistoryById(id)?.history
                if (null == entity) {
                    return@runInTransaction
                }
                if (1 == historyDao.deleteHistory(id)) {
                    resetAccountBalance(entity)
                    resetAccountBalance(entity,false)
                    resetGroupDue(entity)
                }
            }
            else {
                historyDao.deleteHistory(id)
            }
        }
    }

    override fun deleteMultipleHistories(ids: List<Long>, reset: Boolean) {
        if (reset) {
            ids.forEach { id ->
                deleteHistory(id,true)
            }
        }
        else {
            historyDao.deleteMultipleHistories(ids)
        }
    }

    // utility methods

    private fun updateAccountBalance(history: HistoryEntity, primary: Boolean = true) {
        if (primary) {
            when(history.type) {
                HistoryType.CREDIT -> {
                    history.primaryAccountId?.let { id ->
                        accountRepository.creditBalance(id,history.amount)
                    }
                }
                HistoryType.DEBIT,
                HistoryType.TRANSFER -> {
                    history.primaryAccountId?.let{ id ->
                        accountRepository.debitBalance(id,history.amount)
                    }
                }
            }
        }
        else {
            when(history.type) {
                HistoryType.TRANSFER -> {
                    history.primaryAccountId?.let { id ->
                        accountRepository.creditBalance(id,history.amount)
                    }
                }
                else -> {}
            }
        }
    }

    private fun resetAccountBalance(history: HistoryEntity, primary: Boolean = true) {
        if (primary) {
            when(history.type) {
                HistoryType.CREDIT -> {
                    history.primaryAccountId?.let { id ->
                        accountRepository.debitBalance(id,history.amount)
                    }
                }
                HistoryType.DEBIT,
                HistoryType.TRANSFER -> {
                    history.primaryAccountId?.let{ id ->
                        accountRepository.creditBalance(id,history.amount)
                    }
                }
            }
        }
        else {
            when(history.type) {
                HistoryType.TRANSFER -> {
                    history.secondaryAccountId?.let { id ->
                        accountRepository.creditBalance(id,history.amount)
                    }
                }
                else -> {}
            }
        }
    }

    private fun updateGroupDue(history: HistoryEntity) {
        when(history.type) {
            HistoryType.CREDIT -> {
                history.groupId?.let { id ->
                    groupRepository.debitDue(id,history.amount)
                }
            }
            HistoryType.DEBIT -> {
                history.groupId?.let { id ->
                    groupRepository.creditDue(id,history.amount)
                }
            }
            else -> {}
        }
    }

    private fun resetGroupDue(history: HistoryEntity, ) {
        when(history.type) {
            HistoryType.CREDIT -> {
                history.groupId?.let { id ->
                    groupRepository.creditDue(id,history.amount)
                }
            }
            HistoryType.DEBIT -> {
                history.groupId?.let { id ->
                    groupRepository.debitDue(id,history.amount)
                }
            }
            else -> {}
        }
    }
}