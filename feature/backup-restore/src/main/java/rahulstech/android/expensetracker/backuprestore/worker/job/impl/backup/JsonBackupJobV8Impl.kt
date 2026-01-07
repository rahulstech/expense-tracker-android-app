package rahulstech.android.expensetracker.backuprestore.worker.job.impl.backup

import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.Progress
import rahulstech.android.expensetracker.backuprestore.util.toHistoryData
import rahulstech.android.expensetracker.backuprestore.worker.job.JsonBackupJob
import rahulstech.android.expensetracker.domain.BackupRepository
import java.io.OutputStream

class JsonBackupJobV8Impl(
    destination: OutputStream,
    val repo: BackupRepository,
    progressCallback: (Progress)-> Unit = {}
): JsonBackupJob(8,destination,progressCallback) {

    override fun doBackup() {
        backupAccounts()
        backupGroups()
        backupHistories()
    }

    fun backupAccounts() {
        val accounts = repo.getMultipleAccounts()
        writeArray(Constants.JSON_FIELD_ACCOUNTS, accounts)
    }

    fun backupGroups() {
        val groups = repo.getMultipleGroups()
        writeArray(Constants.JSON_FIELD_GROUPS, groups)
    }

    fun backupHistories() {
        writeArrayInChunk<HistoryData>(Constants.JSON_FIELD_HISTORIES).use { writer ->
            var skip: Long = 0
            var chunk: List<HistoryData> = emptyList()
            do {
                chunk = repo.getMultipleHistories(1000,skip)
                    .map { it.toHistoryData() }
                skip += chunk.size.toLong()
                writer.writeNextChunk(chunk)
            } while (!chunk.isEmpty())
        }
    }
}