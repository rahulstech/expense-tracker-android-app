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
    data class AddAccountHistory(val accountId: Long) : AppDestination

    @Serializable
    data class SendMoney(val sourceAccountId: Long) : AppDestination

    @Serializable
    data object AddGroup: AppDestination

    @Serializable
    data class EditGroup(val groupId: Long): AppDestination

    @Serializable
    data object GroupList: AppDestination

    @Serializable
    data class ViewGroup(val groupId: Long): AppDestination

    @Serializable
    data class AddGroupHistory(val groupId: Long) : AppDestination

    @Serializable
    data class ViewAccountHistory(val accountId: Long): AppDestination

    @Serializable
    data class ViewGroupHistory(val groupId: Long): AppDestination

    @Serializable
    data object AddHistory: AppDestination

    @Serializable
    data class EditHistory(val historyId: Long): AppDestination


    @Serializable
    data class ViewHistoryDetails(val historyId: Long): AppDestination
}