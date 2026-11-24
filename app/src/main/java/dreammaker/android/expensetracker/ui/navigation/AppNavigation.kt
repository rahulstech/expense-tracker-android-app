package dreammaker.android.expensetracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dreammaker.android.expensetracker.ui.account.accountlist.AccountListRoute
import dreammaker.android.expensetracker.ui.account.inputaccount.AccountInputRoute
import dreammaker.android.expensetracker.ui.account.viewaccount.ViewAccountRoute
import dreammaker.android.expensetracker.ui.component.SnackBarCallback
import dreammaker.android.expensetracker.ui.group.groupslist.GroupListRoute
import dreammaker.android.expensetracker.ui.group.inputgroup.GroupInputRoute
import dreammaker.android.expensetracker.ui.group.viewgroup.ViewGroupRoute
import dreammaker.android.expensetracker.ui.history.historyinput.CreditDebitInputRoute
import dreammaker.android.expensetracker.ui.history.historyinput.IncomeExpenseInputRoute
import dreammaker.android.expensetracker.ui.history.historyinput.TransferInputRoute
import dreammaker.android.expensetracker.ui.home.HomeRoute

@Composable
fun AppNavigation(
    snackBarCallback: SnackBarCallback,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val navigationHandler: (NavigationEvent)-> Unit = remember {{ event ->
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
    }}

    NavHost(
        navController,
        startDestination = AppDestination.Home,
        modifier = Modifier.fillMaxSize()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {

        // routes of account

        composable<AppDestination.Home> {
            HomeRoute(onNavigation = navigationHandler)
        }

        composable<AppDestination.AddAccount> {
            AccountInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigate = navigationHandler
            )
        }

        composable<AppDestination.EditAccount> {
            AccountInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigate = navigationHandler
            )
        }

        composable<AppDestination.AccountList> {
            AccountListRoute(
                snackBarCallback = snackBarCallback,
                onNavigate = navigationHandler,
            )
        }

        composable<AppDestination.ViewAccount> {
            ViewAccountRoute(
                snackBarCallback = snackBarCallback,
                onNavigate = navigationHandler
            )
        }

        // routes of group

        composable<AppDestination.AddGroup> {
            GroupInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigate = navigationHandler
            )
        }

        composable<AppDestination.EditGroup> {
            GroupInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigate = navigationHandler
            )
        }

        composable<AppDestination.GroupList> {
            GroupListRoute(
                snackBarCallback = snackBarCallback,
                onNavigation = navigationHandler
            )
        }

        composable<AppDestination.ViewGroup> {
            ViewGroupRoute(
                snackBarCallback = snackBarCallback,
                onNavigate = navigationHandler
            )
        }

        // routes of history

        composable<AppDestination.AddCreditDebit> {
            CreditDebitInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigation = navigationHandler
            )
        }

        composable<AppDestination.EditCreditDebit> {
            CreditDebitInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigation = navigationHandler
            )
        }

        composable<AppDestination.AddTransfer> {
            TransferInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigation = navigationHandler
            )
        }

        composable<AppDestination.EditTransfer> {
            TransferInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigation = navigationHandler
            )
        }

        composable<AppDestination.AddIncomeExpense> {
            IncomeExpenseInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigation = navigationHandler
            )
        }

        composable<AppDestination.EditIncomeExpense> {
            IncomeExpenseInputRoute(
                snackBarCallback = snackBarCallback,
                onNavigation = navigationHandler
            )
        }

        composable<AppDestination.ViewAccountHistory> {  }

        composable<AppDestination.ViewGroupHistory> { }

        composable<AppDestination.ViewHistoryDetails> { }
    }
}