package rahulstech.android.expensetracker.backuprestore.strategy.restore

import rahulstech.android.expensetracker.backuprestore.strategy.AccountData
import rahulstech.android.expensetracker.backuprestore.strategy.Constants
import rahulstech.android.expensetracker.backuprestore.strategy.Destination
import rahulstech.android.expensetracker.backuprestore.strategy.ExpenseDatabaseOutput
import rahulstech.android.expensetracker.backuprestore.strategy.GroupData
import rahulstech.android.expensetracker.backuprestore.strategy.HistoryData

class ExpenseDatabaseDestination(override val output: ExpenseDatabaseOutput) : Destination {

    override fun setup() {
        output.create()
    }

    override fun cleanup() {
        output.destroy()
    }

    override fun canWrite(name: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun writeSingle(name: String, entry: Any) {
        TODO("Not yet implemented")
    }

    override fun writeMultiple(name: String, entries: List<Any>) {
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    override fun appendMultiple(name: String, entries: List<Any>) {
        when(name) {
            Constants.EXPENSE_DB_ACCOUNTS -> appendMultipleAccounts(entries as List<AccountData>)
            Constants.EXPENSE_DB_GROUPS -> appendMultipleGroups(entries as List<GroupData>)
            Constants.EXPENSE_DB_HISTORIES -> appendMultipleHistories(entries as List<HistoryData>)
        }
    }

    private fun appendMultipleAccounts(entries: List<AccountData>) {

    }

    private fun appendMultipleGroups(entries: List<GroupData>) {

    }

    private fun appendMultipleHistories(entries: List<HistoryData>) {

    }

    private fun <I,O> map(source: List<I>, converter: (I)->O): List<O> {
        return source.mapTo(mutableListOf(),converter)
    }
}