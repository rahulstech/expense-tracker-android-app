package rahulstech.android.expensetracker.backuprestore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import rahulstech.android.expensetracker.domain.BackupRepository
import rahulstech.android.expensetracker.domain.RestoreRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.io.InputStream
import java.time.LocalDate
import kotlin.math.min

data class SimpleData(
    val data: Int
)

fun String.asInputStream(): InputStream = trimIndent().byteInputStream(Charsets.UTF_8)

class FakeRestoreRepositoryImpl: RestoreRepository {

    var accounts: List<Account>? = null
    var groups: List<Group>? = null
    var histories: List<History>? = null

    override suspend fun addAccounts(accounts: List<Account>) {
        this.accounts = accounts
    }

    override suspend fun addGroups(groups: List<Group>) {
        this.groups = groups
    }

    override suspend fun addHistories(histories: List<History>) {
        this.histories = histories
    }
}

class FakeBackupRepositoryImpl: BackupRepository {

    val accounts = listOf(
        Account(id = 1, name = "Account 1", balance = 100.0),
        Account(id = 2, name = "Account 2", balance = 0.0, isDefault = true)
    )

    val groups = listOf(
        Group(id = 1, name = "Group 1", balance = 100.0),
        Group(id = 2, name = "Group 2", balance = 0.0),
    )

    val histories = listOf(
        History.CreditHistory(id=1,date= LocalDate.of(2025,4,6), amount = 100.0, note = "credit", primaryAccountId = 1, groupId = 2),
        History.DebitHistory(id=2,date= LocalDate.of(2025,4,6), amount = 120.0, primaryAccountId = 1),
        History.TransferHistory(id=3,date= LocalDate.of(2025,4,6), amount = 100.0, primaryAccountId = 1, secondaryAccountId = 2),
    )

    fun getMultipleHistories(
        size: Int,
        skip: Long
    ): List<History> {
        val from = skip.toInt()
        val to = min(from+size,histories.size)
        if (from >= histories.size) return emptyList()
        return histories.subList(from,to)
    }

    override fun getAccounts(): Flow<List<Account>> = flowOf(accounts)

    override fun getGroups(): Flow<List<Group>> = flowOf(groups)

    override fun getHistories(size: Int, skip: Long): Flow<List<History>> = flowOf(getMultipleHistories(size, skip))
}
