package rahulstech.android.expensetracker.domain.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.AccountDao
import dreammaker.android.expensetracker.database.dao.GroupDao
import dreammaker.android.expensetracker.database.dao.HistoryDao
import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.HistoryType
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.LocalCache
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.Callable

class FakeLocalCache: LocalCache {

    val accountTotalUsed = mutableMapOf<Long,Long>(
        1L to 1,
        2L to 3
    )
    val groupTotalUsed = mutableMapOf<Long,Long>(
        1L to 1,
        2L to 3
    )

    override fun getAccountTotalUsed(id: Long, defaultValue: Long): Long =
        accountTotalUsed.getOrPut(id) { defaultValue }

    override fun setAccountTotalUsed(id: Long, value: Long) {
        accountTotalUsed.put(id, value)
    }

    override fun removeAccountTotalUsed(id: Long) {
        accountTotalUsed.remove(id)
    }

    override fun getGroupTotalCount(id: Long, defaultValue: Long): Long =
        groupTotalUsed.getOrPut(id) { defaultValue }

    override fun setGroupTotalCount(id: Long, value: Long) {
        groupTotalUsed.put(id,value)
    }

    override fun removeGroupTotalUsed(id: Long) {
        groupTotalUsed.remove(id)
    }
}

class FakeExpenseDatabase: IExpenseDatabase {

    override val accountDao = FakeAccountDao()
    override val groupDao = FakeGroupDao()
    override val historyDao = FakeHistoryDao()

    override fun <V> runInTransaction(task: Callable<V>): V =
        task.call()

    override fun runInTransaction(task: Runnable) {
        task.run()
    }
}

class FakeAccountDao: AccountDao {

    val accounts = mutableMapOf(
        1L to AccountEntity(1L,"Account 1", 100f, LocalDateTime.of(2025,6,11,14,20,19),1),
        2L to AccountEntity(2L,"Account 2", 2000f, LocalDateTime.of(2025,5,12,16,23,19),3)
    )

    lateinit var lastInsertedAccount: AccountEntity

    override fun insertAccount(account: AccountEntity): Long {
        lastInsertedAccount = account.copy(id=3)
        return lastInsertedAccount.id
    }

    override fun insertAccounts(accounts: List<AccountEntity>) {}

    override fun getLiveAllAccounts(): LiveData<List<AccountEntity>> = MutableLiveData()

    override fun getAllAccounts(): List<AccountEntity> = emptyList()

    override fun findAccountById(id: Long): AccountEntity? = accounts[id]

    override fun getLiveAccountById(id: Long): LiveData<AccountEntity?> = MutableLiveData()

    override fun getLiveTotalBalance(): LiveData<Double?> = MutableLiveData()

    override fun getLiveRecentlyUsedAccounts(count: Int): LiveData<List<AccountEntity>> = MutableLiveData()

    override fun updateAccount(account: AccountEntity): Int {
        if (accounts.containsKey(account.id)) {
            accounts[account.id] = account.copy()
            return 1
        }
        return 0
    }

    override fun deleteAccount(id: Long): Int {
        if (accounts.containsKey(id)) {
            accounts.remove(id)
            return 1
        }
        return 0
    }

    override fun deleteMultipleAccounts(ids: List<Long>): Int {
        var removed = 0
        ids.forEach { id ->
            if (accounts.containsKey(id)) {
                accounts.remove(id)
                removed++
            }
        }
        return removed
    }
}

class FakeGroupDao: GroupDao {

    val groups = mutableMapOf(
        1L to GroupEntity(1L,"Group 1", 100f, LocalDateTime.of(2025,6,11,14,20,19),1),
        2L to GroupEntity(2L,"Group 2", 2000f, LocalDateTime.of(2025,5,12,16,23,19),3)
    )

    lateinit var lastInsertedGroup: GroupEntity

    override fun insertGroup(group: GroupEntity): Long {
        lastInsertedGroup = group.copy(id=3L)
        return lastInsertedGroup.id
    }

    override fun insertGroups(groups: List<GroupEntity>) {}

    override fun getLiveAllGroups(): LiveData<List<GroupEntity>> = MutableLiveData()

    override fun getAllGroups(): List<GroupEntity> = emptyList()

    override fun findGroupById(id: Long): GroupEntity? = groups[id]

    override fun getLiveGroupById(id: Long): LiveData<GroupEntity?> = MutableLiveData()

    override fun getLiveRecentlyUsedGroups(count: Int): LiveData<List<GroupEntity>> = MutableLiveData()

    override fun updateGroup(group: GroupEntity): Int {
        if (groups.containsKey(group.id)) {
            groups[group.id] = group.copy()
            return 1
        }
        return 0
    }

    override fun deleteGroup(id: Long): Int {
        if (groups.containsKey(id)) {
            groups.remove(id)
            return 1
        }
        return 0
    }

    override fun deleteMultipleGroups(ids: List<Long>): Int {
        var removed = 0
        ids.forEach { id ->
            if (groups.containsKey(id)) {
                groups.remove(id)
                removed++
            }
        }
        return removed
    }
}

class FakeHistoryDao: HistoryDao {

    val histories = mutableMapOf(
        1L to HistoryEntity(
            id=1L,
            date=LocalDate.of(2025,8,15),
            amount=100f,
            note="Debit",
            type= HistoryType.DEBIT,
            primaryAccountId=1L,
            secondaryAccountId=null,
            groupId=2L
        )
    )

    override fun insertHistory(history: HistoryEntity): Long = 1

    override fun insertHistories(histories: List<HistoryEntity>) {}

    override fun getLiveHistoriesForAccountBetweenDates(
        accountId: Long,
        start: LocalDate,
        end: LocalDate
    ): LiveData<List<HistoryDetails>> = MutableLiveData()

    override fun getLiveHistoriesForGroupBetweenDates(
        groupId: Long,
        start: LocalDate,
        end: LocalDate
    ): LiveData<List<HistoryDetails>> = MutableLiveData()

    override fun findHistoryById(id: Long): HistoryEntity? = histories[id]

    override fun getLiveHistoryById(id: Long): LiveData<HistoryDetails?> = MutableLiveData()

    override fun getHistories(
        from: Long,
        size: Long
    ): List<HistoryEntity> = emptyList()

    override fun updateHistory(history: HistoryEntity): Int = 1

    override fun deleteHistory(id: Long): Int = 0

    override fun deleteMultipleHistories(ids: List<Long>): Int = 0
}

class FakeAccountRepository: AccountRepository {

    val accounts = mutableMapOf(
        1L to Account("Account 1", 100f, 1L, LocalDateTime.of(2025,6,11,14,20,19),1),
        2L to Account("Account 2", 2000f, 2L,LocalDateTime.of(2025,5,12,16,23,19),3)
    )

    override fun insertAccount(account: Account): Account = account

    override fun getLiveAccountById(id: Long): LiveData<Account?> = MutableLiveData()

    override fun findAccountById(id: Long): Account? = null

    override fun getLiveAllAccounts(): LiveData<List<Account>> = MutableLiveData()

    override fun getLiveRecentlyUsedAccounts(count: Int): LiveData<List<Account>> = MutableLiveData()

    override fun getLiveTotalBalance(): LiveData<Double> = MutableLiveData()

    override fun updateAccount(account: Account): Boolean = true

    override fun creditBalance(id: Long, amount: Number) {
        accounts[id]?.let { account ->
            accounts[id] = account.copy(balance = account.balance.toFloat() + amount.toFloat())
        }
    }

    override fun debitBalance(id: Long, amount: Number) {
        accounts[id]?.let { account ->
            accounts[id] = account.copy(balance = account.balance - amount.toFloat())
        }
    }

    override fun deleteAccount(id: Long) {}

    override fun deleteMultipleAccounts(ids: List<Long>) {}
}

class FakeGroupRepository: GroupRepository {

    val groups = mutableMapOf(
        1L to Group("Group 1", 100f, 1L,LocalDateTime.of(2025,6,11,14,20,19),1),
        2L to Group("Group 2", 2000f, 2L,LocalDateTime.of(2025,5,12,16,23,19),3)
    )

    override fun insertGroup(group: Group): Group = group

    override fun findGroupById(id: Long): Group? = null

    override fun getLiveGroupById(id: Long): LiveData<Group?> = MutableLiveData()

    override fun getLiveAllGroups(): LiveData<List<Group>> = MutableLiveData()

    override fun getLiveRecentlyUsedGroups(count: Int): LiveData<List<Group>> = MutableLiveData()

    override fun updateGroup(group: Group): Boolean = true

    override fun creditDue(id: Long, amount: Number) {
        groups[id]?.let { group ->
            groups[id] = group.copy(due = group.due.toFloat() + amount.toFloat())
        }
    }

    override fun debitDue(id: Long, amount: Number) {
        groups[id]?.let { group ->
            groups[id] = group.copy(due = group.due.toFloat() - amount.toFloat())
        }
    }

    override fun deleteGroup(id: Long) {}

    override fun deleteMultipleGroups(ids: List<Long>) {}

}