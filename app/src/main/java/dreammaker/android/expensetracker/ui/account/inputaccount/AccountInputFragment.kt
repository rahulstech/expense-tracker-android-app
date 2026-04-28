package dreammaker.android.expensetracker.ui.account.inputaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.hasArgument
import dreammaker.android.expensetracker.util.isActionEdit

@AndroidEntryPoint
class AccountInputFragment : Fragment() {

    private val viewModel: AccountInputViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasArgument(Constants.ARG_ACTION)) {
            throw IllegalStateException("'${Constants.ARG_ACTION}' argument not found")
        }
        if (getAction() == Constants.ACTION_EDIT && !hasArgument(Constants.ARG_ID)) {
            throw IllegalStateException("'${Constants.ARG_ID}' argument not found; it is required for ${Constants.ARG_ACTION} = '${Constants.ACTION_EDIT}'")
        }

        if (isActionEdit()) {
            viewModel.findAccountById(getArgId())
        }
    }

    private fun getAction(): String? = arguments?.getString(Constants.ARG_ACTION)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ExpenseTrackerTheme {
                    val uiState by viewModel.uiState.collectAsState()
                    val context = LocalContext.current

                    LaunchedEffect(uiState.isLoadingAccount, uiState.account) {
                        if (isActionEdit() && !uiState.isLoadingAccount && null == uiState.account) {
                            // account not found, exit with a message
                            QuickMessages.toastError(
                                context,
                                context.getString(R.string.message_account_not_found),
                                true
                            )
                            navController.popBackStack()
                        }
                    }

                    AccountInputScreen(
                        uiState = uiState,
                        onSave = { account ->
                            viewModel.saveAccount(account, isActionEdit())
                        },
                        onCancel = {
                            navController.popBackStack()
                        },
                    )
                }
            }
        }
    }
}
