package rahulstech.android.expensetracker.backuprestore.strategy.restore

import dreammaker.android.expensetracker.database.Account
import dreammaker.android.expensetracker.database.Group
import dreammaker.android.expensetracker.database.History
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.worker.Constants
import rahulstech.android.expensetracker.backuprestore.strategy.Destination
import rahulstech.android.expensetracker.backuprestore.strategy.ExpenseDatabaseOutput
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData

class ExpenseDatabaseDestination(override val output: ExpenseDatabaseOutput) : Destination {

    private val ALLOWED_NAMES = arrayOf(Constants.EXPENSE_DB_ACCOUNTS, Constants.EXPENSE_DB_GROUPS, Constants.EXPENSE_DB_HISTORIES)

    override fun setup() {
        output.create()
    }

    override fun cleanup() {
        output.destroy()
    }

    override fun canWrite(name: String): Boolean = name in ALLOWED_NAMES

    override fun writeSingle(name: String, entry: Any?) {}

    override fun writeMultiple(name: String, entries: List<Any>) {}

    override fun beginAppendMultiple(name: String) {}

    override fun endAppendMultiple(name: String) {}

    @Suppress("UNCHECKED_CAST")
    override fun appendMultiple(name: String, entries: List<Any>) {
        when(name) {
            Constants.EXPENSE_DB_ACCOUNTS -> appendMultipleAccounts(entries as List<AccountData>)
            Constants.EXPENSE_DB_GROUPS -> appendMultipleGroups(entries as List<GroupData>)
            Constants.EXPENSE_DB_HISTORIES -> appendMultipleHistories(entries as List<HistoryData>)
        }
    }

    private fun appendMultipleAccounts(entries: List<AccountData>) {
        val accounts: List<Account> = map(entries) { data -> data.toAccountModel().toAccount() }
        output.get().accountDao.insertAccounts(accounts)
    }

    private fun appendMultipleGroups(entries: List<GroupData>) {
        val groups: List<Group> = map(entries) { data -> data.toGroupModel().toGroup() }
        output.get().groupDao.insertGroups(groups)
    }

    private fun appendMultipleHistories(entries: List<HistoryData>) {
        val histories: List<History> = map(entries) { data -> data.toHistoryModel().toHistory() }
        output.get().historyDao.insertHistories(histories)
    }

    private fun <I,O> map(source: List<I>, converter: (I)->O): List<O> {
        return source.mapTo(mutableListOf(),converter)
    }
}