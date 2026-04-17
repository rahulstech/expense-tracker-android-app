package dreammaker.android.expensetracker.ui.home

import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group

data class HomeScreenState(
    val totalBalance: Double = 0.0,
    val recentAccounts: List<Account> = emptyList(),
    val recentGroups: List<Group> = emptyList()
)

sealed interface HomeScreenEvent {
    data object ViewAllAccounts : HomeScreenEvent
    data object ViewAllGroups : HomeScreenEvent
    data object AddHistory : HomeScreenEvent
    data class ClickAccount(val account: Account) : HomeScreenEvent
    data class ClickGroup(val group: Group) : HomeScreenEvent

    data object AddNewAccount: HomeScreenEvent

    data object AddNewGroup: HomeScreenEvent
}
