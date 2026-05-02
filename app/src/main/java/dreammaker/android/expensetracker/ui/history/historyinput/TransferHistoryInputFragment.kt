package dreammaker.android.expensetracker.ui.history.historyinput

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.ui.history.historyinput.TransactionInputFragment.Companion.ARG_HISTORY_DATE
import dreammaker.android.expensetracker.util.AccountParcelable
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.isActionEdit
import rahulstech.android.expensetracker.domain.model.Account
import java.time.LocalDate

@AndroidEntryPoint
class TransferHistoryInputFragment : Fragment() {

    private val viewModel: HistoryInputViewModel by viewModels()

    private val navController: NavController by lazy {  findNavController() }

    private fun getArgDate(): LocalDate = arguments?.getDate(ARG_HISTORY_DATE) ?: LocalDate.now()

    private fun getArgAccount(): Account?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountParcelable::class.java)?.toAccount() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.setIsTransfer(true)
            viewModel.setDate(getArgDate())
            viewModel.setAccountSelection(getArgAccount())

            if (isActionEdit()) {
                viewModel.findHistory(getArgId())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ExpenseTrackerTheme {
                    TransferInputScreen(
                        viewModel = viewModel,
                        isEdit = isActionEdit(),
                        onAddNewAccount = {
                            navController.navigate(R.id.navigate_to_create_account)
                        },
                        exit = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
