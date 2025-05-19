package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.content.Context
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.settings.SettingsProvider
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AgentSettingsData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.worker.WriteHelper

class WriterHelperImpl(context: Context): WriteHelper {

    private val applicationContext = context.applicationContext

    private var _expenseDB: ExpensesDatabase? = null
    private val expenseDB: ExpensesDatabase get() = _expenseDB!!

    override fun open() {
        // open expense db connection
        _expenseDB = ExpensesDatabase.getInstance(applicationContext)
    }

    override fun close() {
        // close expense db connection
        _expenseDB?.close()
        _expenseDB = null
    }

    override fun writeAccounts(accounts: List<AccountData>) {
        val dao = expenseDB.accountDao
        val dbAccounts = accounts.map { account -> account.toAccountModel().toAccount() }
        dao.insertAccounts(dbAccounts)
    }

    override fun writeGroups(groups: List<GroupData>) {
        val dao = expenseDB.groupDao
        val dbGroups = groups.map { group -> group.toGroupModel().toGroup() }
        dao.insertGroups(dbGroups)
    }

    override fun writeHistories(histories: List<HistoryData>) {
        val dao = expenseDB.historyDao
        val dbHistories = histories.map { history -> history.toHistoryModel().toHistory() }
        dao.insertHistories(dbHistories)
    }

    override fun writeAppSettings(settings: AppSettingsData) {
        val model = settings.toSettingsModel()
        SettingsProvider.get(applicationContext).restore(model)
    }

    override fun writeAgentSettings(settings: AgentSettingsData) {
        val model = settings.toAgentSettingsModel()
        AgentSettingsProvider.get(applicationContext).restore(model)
    }
}