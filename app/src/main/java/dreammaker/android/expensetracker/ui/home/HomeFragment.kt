package dreammaker.android.expensetracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val navController: NavController by lazy { findNavController() }
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ExpenseTrackerTheme {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    HomeScreen(
                        state = state,
                        onEvent = { event ->
                            handleHomeScreenEvent(event)
                        }
                    )
                }
            }
        }
    }

    private fun handleHomeScreenEvent(event: HomeScreenEvent) {
        when (event) {
            HomeScreenEvent.AddHistory -> navigateToCreateHistory()
            HomeScreenEvent.ViewAllAccounts -> navController.navigate(R.id.action_home_to_accounts_list)
            HomeScreenEvent.ViewAllGroups -> navController.navigate(R.id.action_home_to_groups_list)
            is HomeScreenEvent.ClickAccount -> {
                navController.navigate(R.id.action_home_to_view_account, bundleOf (
                    Constants.ARG_ID to event.account.id
                ))
            }
            is HomeScreenEvent.ClickGroup -> {
                navController.navigate(R.id.action_home_to_view_group, Bundle().apply {
                    putLong(Constants.ARG_ID, event.group.id)
                })
            }

            HomeScreenEvent.AddNewAccount -> {
                navController.navigate(R.id.navigate_to_edit_account)
            }
            HomeScreenEvent.AddNewGroup -> {
                navController.navigate(R.id.navigate_to_create_group)
            }
        }
    }

    private fun navigateToCreateHistory() {
        navController.navigate(R.id.navigate_to_create_transaction)
    }
}
