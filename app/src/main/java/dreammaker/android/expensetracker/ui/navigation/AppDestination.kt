package dreammaker.android.expensetracker.ui.navigation

import kotlinx.serialization.Serializable


sealed interface AppDestination {

    @Serializable
    data object Home: AppDestination

    @Serializable
    data object AddAccount: AppDestination

    @Serializable
    data class EditAccount(val accountId: Long): AppDestination

    @Serializable
    data object AccountList: AppDestination

    @Serializable
    data class ViewAccount(val accountId: Long): AppDestination

    @Serializable
    data object AddGroup: AppDestination

    @Serializable
    data class EditGroup(val groupId: Long): AppDestination

    @Serializable
    data object GroupList: AppDestination

    @Serializable
    data class ViewGroup(val groupId: Long): AppDestination

    @Serializable
    data class ViewAccountHistory(val accountId: Long): AppDestination

    @Serializable
    data class ViewGroupHistory(val groupId: Long): AppDestination

    @Serializable
    data class ViewHistoryDetails(val historyId: Long): AppDestination


    @Serializable
    sealed class HistoryInput: AppDestination


    @Serializable
    data class AddCreditDebit(val accountId: Long? = null, val groupId: Long? = null): HistoryInput()

    @Serializable
    data class EditCreditDebit(val historyId: Long): HistoryInput()

    @Serializable
    data class AddTransfer(val sourceAccountId: Long): HistoryInput()

    @Serializable
    data class EditTransfer(val historyId: Long): HistoryInput()

    @Serializable
    data class AddIncomeExpense(val accountId: Long? = null): HistoryInput()

    @Serializable
    data class EditIncomeExpense(val historyId: Long): HistoryInput()
}