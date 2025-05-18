package rahulstech.android.expensetracker.backuprestore.worker

import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AgentSettingsData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData

interface WriteHelper {

    fun open()

    fun close()

    fun writeAccounts(accounts: List<AccountData>)

    fun writeGroups(groups: List<GroupData>)

    fun writeHistories(histories: List<HistoryData>)

    fun writeAppSettings(settings: AppSettingsData)

    fun writeAgentSettings(settingsData: AgentSettingsData)
}

interface ReadHelper {

    fun open()

    fun close()

    fun readAccounts(from: Long, size: Long): List<AccountData>

    fun readGroups(from: Long, size: Long): List<GroupData>

    fun readHistories(from: Long, size: Long): List<HistoryData>

    fun readAppSettings(): AppSettingsData

    fun readAgentSettings(): AgentSettingsData
}