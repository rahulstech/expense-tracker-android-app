package dreammaker.android.expensetracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dreammaker.android.expensetracker.ui.account.accountlist.AccountListRoute
import dreammaker.android.expensetracker.ui.account.inputaccount.AccountInputRoute
import dreammaker.android.expensetracker.ui.account.viewaccount.ViewAccountRoute
import dreammaker.android.expensetracker.ui.group.groupslist.GroupListRoute
import dreammaker.android.expensetracker.ui.group.inputgroup.GroupInputRoute
import dreammaker.android.expensetracker.ui.group.viewgroup.ViewGroupRoute
import dreammaker.android.expensetracker.ui.home.HomeRoute

@Composable
fun AppNavigation(
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val navigationHandler: (NavigationEvent)-> Unit = { event ->
        when(event) {
            is NavigationEvent.Up -> {
                navController.navigateUp()
            }

            is NavigationEvent.Exit -> {
                navController.popBackStack()
            }

            is NavigationEvent.To -> {
                navController.navigate(event.destination)
            }
        }
    }

    NavHost(
        navController,
        startDestination = AppDestination.Home,
        modifier = Modifier.fillMaxSize()
            .systemBarsPadding()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {

        // routes of account

        composable<AppDestination.Home> {
            HomeRoute(onNavigation = navigationHandler)
        }

        composable<AppDestination.AddAccount> {
            AccountInputRoute(
                snackBarHostState = snackBarHostState,
                onNavigate = navigationHandler
            )
        }

        composable<AppDestination.EditAccount> {
            AccountInputRoute(
                snackBarHostState = snackBarHostState,
                onNavigate = navigationHandler
            )
        }

        composable<AppDestination.AccountList> {
            AccountListRoute(
                snackBarHostState = snackBarHostState,
                onNavigate = navigationHandler,
            )
        }

        composable<AppDestination.ViewAccount> {
            ViewAccountRoute(
                snackBarHostState = snackBarHostState,
                onNavigate = navigationHandler
            )
        }

        // routes of group

        composable<AppDestination.AddGroup> {
            GroupInputRoute(
                snackBarHostState = snackBarHostState,
                onNavigate = navigationHandler
            )
        }

        composable<AppDestination.EditGroup> {
            GroupInputRoute(
                snackBarHostState = snackBarHostState,
                onNavigate = navigationHandler
            )
        }

        composable<AppDestination.GroupList> {
            GroupListRoute(
                snackBarHostState = snackBarHostState,
                onNavigation = navigationHandler
            )
        }

        composable<AppDestination.ViewGroup> {
            ViewGroupRoute(
                snackBarHostState = snackBarHostState,
                onNavigate = navigationHandler
            )
        }

        // routes of history

        composable<AppDestination.AddHistory> {  }

        composable<AppDestination.ViewAccountHistory> {  }

        composable<AppDestination.ViewGroupHistory> { }

        composable<AppDestination.EditHistory> { }

        composable<AppDestination.ViewHistoryDetails> { }
    }
}