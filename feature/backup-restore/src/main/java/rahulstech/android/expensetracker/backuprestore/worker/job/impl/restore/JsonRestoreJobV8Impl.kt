package rahulstech.android.expensetracker.backuprestore.worker.job.impl.restore

import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.Progress
import rahulstech.android.expensetracker.backuprestore.worker.job.JsonArrayChunkReader
import rahulstech.android.expensetracker.backuprestore.worker.job.JsonRestoreJob
import rahulstech.android.expensetracker.domain.RestoreRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import java.io.InputStream

class JsonRestoreJobV8Impl(
    source: InputStream,
    val repo: RestoreRepository,
    progressCallback: (Progress)-> Unit = {},
): JsonRestoreJob(8,source, progressCallback) {

    override suspend fun doRestore() {
        while (hasNext()) {
            val name = readNextName()
            when(name) {
                Constants.JSON_FIELD_ACCOUNTS -> restoreAccounts(readNextObjectArray(Account::class.java))
                Constants.JSON_FIELD_GROUPS -> restoreGroups(readNextObjectArray(Group::class.java))
                Constants.JSON_FIELD_HISTORIES -> restoreHistories(readNextObjectArrayInChunk(
                    HistoryData::class.java))
                else -> skipNext()
            }
        }
    }

    private suspend fun restoreAccounts(accounts: List<Account>) {
        repo.addAccounts(accounts)
    }

    private suspend fun restoreGroups(groups: List<Group>) {
        repo.addGroups(groups)
    }

    private suspend fun restoreHistories(reader: JsonArrayChunkReader<HistoryData>) {
        while (reader.hasNext()) {
            val chunk = reader.readNextChunk()
                .map {
                    it.toHistory()
                }
            repo.addHistories(chunk)
        }
    }
}
