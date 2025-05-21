package rahulstech.android.expensetracker.backuprestore.worker.restore

import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.worker.Constants

class FakeWriteHelper: JsonRestoreWorker.WriteHelper {

    var callback: ((String,Any?)->Unit)? = null

    override fun open() {}

    override fun close() {}

    override fun writeAccounts(accounts: List<AccountData>) {
        callback?.invoke(Constants.JSON_FIELD_ACCOUNTS, accounts)
    }

    override fun writeGroups(groups: List<GroupData>) {
        callback?.invoke(Constants.JSON_FIELD_GROUPS,groups)
    }

    override fun writeHistories(histories: List<HistoryData>) {
        callback?.invoke(Constants.JSON_FIELD_HISTORIES,histories)
    }

    override fun writeAppSettings(settings: AppSettingsData) {
        callback?.invoke(Constants.JSON_FIELD_APP_SETTINGS,settings)
    }
}