package rahulstech.android.expensetracker.domain.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.HistoryDao
import dreammaker.android.expensetracker.database.dao.HistoryQueryBuilder
import dreammaker.android.expensetracker.database.model.HistoryEntity
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
import rahulstech.android.expensetracker.domain.model.HistoryTotalIncomeTotalExpense
import rahulstech.android.expensetracker.domain.model.HistoryType
import rahulstech.android.expensetracker.domain.model.toHistory
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val db: IExpenseDatabase,
    private val historyDao: HistoryDao,
    private val accountRepository: AccountRepository,
    private val groupRepository: GroupRepository,
): HistoryRepository {

    override fun getPagedHistories(params: HistoryFilterParameters): Flow<PagingData<History>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30
            ),
            pagingSourceFactory = {
                historyDao.getPagedHistories(params.getHistoryQueryBuilder().build())
            }
        ).flow.flowOn(Dispatchers.IO).map { pagingData ->
            pagingData.map { it.toHistory() }
        }
    }

    override fun getTotalCreditDebit(params: HistoryFilterParameters): Flow<HistoryTotalCreditTotalDebit> {
        val builder = params.getHistoryQueryBuilder()
        return historyDao.getHistoryTypeTotals(builder.buildTypeTotals())
            .map { typeTotals ->
                val typeToTotal = typeTotals.associate { it.type to it.total }
                val totalCredit = typeToTotal[HistoryType.CREDIT.name] ?: BigDecimal.ZERO
                val totalDebit = typeToTotal[HistoryType.DEBIT.name] ?: BigDecimal.ZERO
                HistoryTotalCreditTotalDebit(totalCredit, totalDebit)
            }
    }

    override fun getTotalIncomeExpense(
        start: YearMonth,
        end: YearMonth
    ): Flow<HistoryTotalIncomeTotalExpense> {
        val builder = HistoryQueryBuilder().apply {
            historyTypes = listOf(HistoryType.CREDIT_INCOME.name, HistoryType.DEBIT_EXPENSE.name)
            dateRange = start.atDay(1) to end.atEndOfMonth()
        }
        return historyDao.getHistoryTypeTotals(builder.buildTypeTotals())
            .map { typeTotals ->
                val typeToTotal = typeTotals.associate { it.type to it.total }
                val totalIncome = typeToTotal[HistoryType.CREDIT_INCOME.name] ?: BigDecimal.ZERO
                val totalExpense = typeToTotal[HistoryType.DEBIT_EXPENSE.name] ?: BigDecimal.ZERO
                HistoryTotalIncomeTotalExpense(totalIncome, totalExpense)
            }
    }

    override suspend fun createHistory(history: History): History {
        return db.runInTransaction {
            val entity = history.toHistoryEntity()
            val id = historyDao.insert(entity)

            updateAccountBalanceSync(entity)
            updateAccountBalanceSync(entity, false)
            updateGroupBalanceSync(entity)
            
            history.copyId(id)
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
                resetGroupBalanceSync(oldEntity)
                updateAccountBalanceSync(newEntity)
                updateAccountBalanceSync(newEntity, false)
                updateGroupBalanceSync(newEntity)

                true
            }
            else false
        }
    }

    override suspend fun removeHistory(id: Long, reset: Boolean) {
        db.runInTransaction {
            if (reset) {
                val entity = historyDao.findHistoryDetailsByIdFlow(id).first()?.history ?: return@runInTransaction
                if (1 == historyDao.delete(id)) {
                    resetAccountBalanceSync(entity)
                    resetAccountBalanceSync(entity, false)
                    resetGroupBalanceSync(entity)
                }
            } else {
                historyDao.delete(id)
            }
        }
    }

    override suspend fun removeMultipleHistories(ids: List<Long>, reset: Boolean) {
        if (reset) {
            // TODO: improve balance calculation
            ids.forEach { removeHistory(it, true) }
        } else {
            historyDao.deleteMultiple(ids)
        }
    }

    // utility methods (Sync versions for use inside Transactions)

    private suspend fun updateAccountBalanceSync(history: HistoryEntity, primary: Boolean = true) {
        if (primary) {
            when(history.type) {
                HistoryType.CREDIT.name -> {
                     history.primaryAccountId?.let {
                        accountRepository.creditAccountBalance(it, history.amount)
                    }
                }
                HistoryType.DEBIT.name,
                HistoryType.TRANSFER.name -> {
                    history.primaryAccountId?.let {
                        accountRepository.debitAccountBalance(it, history.amount)
                    }
                }
            }
        } else if (history.type == HistoryType.TRANSFER.name) {
                history.secondaryAccountId?.let { accountRepository.debitAccountBalance(it, history.amount) }
        }
    }

    private suspend fun resetAccountBalanceSync(history: HistoryEntity, primary: Boolean = true) {
        if (primary) {
            when(history.type) {
                HistoryType.CREDIT.name -> {
                    history.primaryAccountId?.let {
                        accountRepository.debitAccountBalance(it, history.amount)
                    }
                }
                HistoryType.DEBIT.name,
                HistoryType.TRANSFER.name -> {
                    history.primaryAccountId?.let {
                        accountRepository.creditAccountBalance(it, history.amount)
                    }
                }
            }
        } else if (history.type == HistoryType.TRANSFER.name) {
                history.secondaryAccountId?.let {
                    accountRepository.debitAccountBalance(it, history.amount)
                }
        }
    }

    private suspend fun updateGroupBalanceSync(history: HistoryEntity) {
        when(history.type) {
            HistoryType.CREDIT.name -> {
                history.groupId?.let {
//                    groupRepository.debitGroupDue(it, history.amount)
                    groupRepository.creditGroupDue(it, history.amount)
                }
            }
            HistoryType.DEBIT.name -> {
                history.groupId?.let {
//                    groupRepository.creditGroupDue(it, history.amount)
                    groupRepository.debitGroupDue(it, history.amount)
                }
            }
        }
    }

    private suspend fun resetGroupBalanceSync(history: HistoryEntity) {
        when(history.type) {
            HistoryType.CREDIT.name -> {
                history.groupId?.let {
                    groupRepository.debitGroupDue(it, history.amount)
                }
            }
            HistoryType.DEBIT.name -> {
                history.groupId?.let {
                    groupRepository.creditGroupDue(it, history.amount)
                }
            }
        }
    }
}
