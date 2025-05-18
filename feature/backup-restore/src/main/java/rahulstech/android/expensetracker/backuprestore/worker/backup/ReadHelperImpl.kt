package rahulstech.android.expensetracker.backuprestore.worker.backup

import android.content.Context
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.settings.SettingsProvider
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AgentSettingsData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.toAccountData
import rahulstech.android.expensetracker.backuprestore.util.toAgentSettingsData
import rahulstech.android.expensetracker.backuprestore.util.toGroupData
import rahulstech.android.expensetracker.backuprestore.util.toHistoryData
import rahulstech.android.expensetracker.backuprestore.util.toSettingsData
import rahulstech.android.expensetracker.backuprestore.worker.ReadHelper

class ReadHelperImpl(context: Context): ReadHelper {

    private val applicationContext = context.applicationContext

    private var _expenseDB: ExpensesDatabase? = null
    private val expenseDB: ExpensesDatabase get() = _expenseDB!!

    override fun open() {
        _expenseDB = ExpensesDatabase.getInstance(applicationContext)
    }

    override fun close() {
        _expenseDB?.close()
    }

    override fun readAccounts(from: Long, size: Long): List<AccountData> {
        return expenseDB.accountDao.getAccounts().map { account -> account.toAccountData() }
    }

    override fun readGroups(from: Long, size: Long): List<GroupData> {
        return expenseDB.groupDao.getGroups().map { group -> group.toGroupData() }
    }

    override fun readHistories(from: Long, size: Long): List<HistoryData> {
        return expenseDB.historyDao.getAllHistories(from, size).map { history -> history.toHistoryData() }
    }

    override fun readAppSettings(): AppSettingsData {
        return SettingsProvider.get(applicationContext).backup().toSettingsData()
    }

    override fun readAgentSettings(): AgentSettingsData {
        return AgentSettingsProvider.get(applicationContext).backup().toAgentSettingsData()
    }
}