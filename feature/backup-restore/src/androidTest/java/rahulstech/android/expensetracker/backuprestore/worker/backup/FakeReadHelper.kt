package rahulstech.android.expensetracker.backuprestore.worker.backup

import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.settings.ViewHistory
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData

class FakeReadHelper: JsonBackupWorker.ReadHelper {

    private val accounts = listOf(
        AccountData(1,"Account 1", 150.0f),
        AccountData(2,"Account 2", -150.0f)
    )

    private val groups = listOf(
        GroupData(1, "Group 1", 150.0f),
        GroupData(2,"Group 2", -150.0f)
    )

    private val histories = listOf(
        HistoryData(1, HistoryType.CREDIT, 1, null, null, 150f, Date(2023,4,16),null) ,
        HistoryData(2, HistoryType.DEBIT, 1, null, null, 150f, Date(2023,4,16),"debit"),
        HistoryData(3, HistoryType.CREDIT, 1, null, 1, 150f, Date(2023,4,16),"credit") ,
        HistoryData(4, HistoryType.DEBIT, 1, null, 1, 150f, Date(2023,4,16),null),
        HistoryData(5, HistoryType.TRANSFER, 1, 2, null, 150f, Date(2023,4,16),null)
    )

    override fun open() {}

    override fun close() {}

    override fun readAccounts(): List<AccountData> = accounts

    override fun readGroups(): List<GroupData> = groups

    override fun readHistories(from: Long, size: Long): List<HistoryData> {
        val start = Math.max(0, from)
        val end = Math.min(start + size, histories.size.toLong())
        if (start < end) {
            return histories.subList(start.toInt(), end.toInt())
        }
        return emptyList()
    }

    override fun readAppSettings(): AppSettingsData {
        return AppSettingsData(ViewHistory.DAILY)
    }
}