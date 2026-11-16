package dreammaker.android.expensetracker.ui.navigation

sealed interface NavigationEvent {

    data object Up: NavigationEvent

    data class To(val destination: AppDestination): NavigationEvent

    data object Exit: NavigationEvent
}