package rahulstech.android.expensetracker.backuprestore

object Constants {
    const val JSON_FIELD_VERSION = "version"
    const val JSON_FIELD_ACCOUNTS = "accounts"
    const val JSON_FIELD_PEOPLE = "people"
    const val JSON_FIELD_MONEY_TRANSFER = "money_transfers"
    const val JSON_FIELD_TRANSACTIONS = "transactions"
    const val JSON_FIELD_HISTORIES = "histories"
    const val JSON_FIELD_GROUPS = "groups"
    const val JSON_FIELD_SETTINGS = "settings"
    const val JSON_FIELD_APP_SETTINGS = "app_settings"
    const val JSON_FIELD_AGENT_SETTINGS = "agent_settings"

    const val DATA_PROGRESS_MAX = "progress_max"
    const val DATA_PROGRESS_CURRENT = "progress_current"
    const val DATA_PROGRESS_MESSAGE = "progress_message"
    const val DATA_BACKUP_FILE = "backup_file"
    const val DATA_BACKUP_FILE_NAME = "backup_file_name"

    const val TAG_BACKUP_WORK = "backup_work"
    const val TAG_PERIODIC_BACKUP_WORK = "periodic_backup_work"
    const val TAG_JSON_BACKUP_WORK = "json_backup_work"
    const val TAG_GZIP_BACKUP_WORK = "gzip_backup_work"
    const val TAG_RESTORE_WORK = "restore_work"
    const val TAG_JSON_RESTORE_WORK = "json_restore_work"
    const val TAG_GZIP_RESTORE_WORK = "gzip_restore_work"

    const val REQUEST_CANCEL_BACKUP = 100

    val BACKUP_FILE_MIME_TYPES = arrayOf("application/gzip")
}