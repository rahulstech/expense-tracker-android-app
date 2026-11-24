package dreammaker.android.expensetracker.ui.history.historyinput

import dreammaker.android.expensetracker.util.onFirst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import javax.inject.Inject

class RepositoryComposition @Inject constructor(
    private val accountRepo: AccountRepository,
    private val groupRepo: GroupRepository,
    private val historyRepo: HistoryRepository
) {

    fun findHistoryById(id: Long): Flow<History?> = historyRepo.getHistoryById(id).take(1)

    suspend fun saveHistory(history: History, editing: Boolean = false): History {
        return if (editing) {
            historyRepo.editHistory(history)
            history
        }
        else {
            historyRepo.createHistory(history)
        }
    }

    fun loadAccounts(onFirst: ((List<Account>)-> Unit)? = null): Flow<AccountState> = flow {
        accountRepo.getAllAccounts()
            .collect { accounts ->
                emit(AccountState(isLoading = true))
                val recentAccounts = accounts.sortedByDescending { it.lastUsed }.take(3)
                emit(AccountState(false, accounts, recentAccounts))
            }
    }
        .flowOn(Dispatchers.IO)
        .onFirst { onFirst?.invoke(it.allAccounts) }

    fun loadGroups(onFirst: ((List<Group>)-> Unit)? = null): Flow<GroupState> = flow {
        groupRepo.getAllGroups()
            .collect { groups ->

                emit(GroupState(isLoading = true))

                val recentGroups = groups.sortedByDescending { it.lastUsed }.take(3)
                emit(GroupState(false, groups, recentGroups))
            }
    }
        .flowOn(Dispatchers.IO)
        .onFirst { onFirst?.invoke(it.allGroups) }
}