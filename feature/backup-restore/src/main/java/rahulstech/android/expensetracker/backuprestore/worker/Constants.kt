package rahulstech.android.expensetracker.backuprestore.worker

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


    const val EXPENSE_DB_ACCOUNTS = "accounts"
    const val EXPENSE_DB_GROUPS = "groups"
    const val EXPENSE_DB_HISTORIES = "histories"

    const val APP_SETTINGS = "app_settings"
    const val AGENT_SETTINGS = "agent_settings"

    const val DESTINATION_EXPENSE_DB = "expense_db"
    const val DESTINATION_APP_SETTINGS = "app_settings"
    const val DESTINATION_AGENT_SETTINGS = "agent_settings"

    const val DATA_JSON_BACKUP_FILE = "temp_json_backup_file"
    const val DATA_PROGRESS_MAX = "progress_max"
    const val DATA_PROGRESS_CURRENT = "progress_current"
    const val DATA_PROGRESS_MESSAGE = "progress_message"

    const val TAG_BACKUP_WORK = "backup_work"
    const val TAG_JSON_BACKUP_WORK = "json_backup_work"

    const val DIR_BACKUP = "backup"
}