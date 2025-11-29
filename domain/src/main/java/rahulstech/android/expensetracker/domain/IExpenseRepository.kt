package rahulstech.android.expensetracker.domain

interface IExpenseRepository {

    val accountRepository: AccountRepository

    val groupRepository: GroupRepository

    val historyRepository: HistoryRepository

    val backupRepository: BackupRepository

    val restoreRepository: RestoreRepository
}