package dreammaker.android.expensetracker.ui.history.historyinput

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.util.AccountParcelable
import dreammaker.android.expensetracker.util.GroupParcelable
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.isActionEdit
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import java.time.LocalDate

@AndroidEntryPoint
class TransactionInputFragment : Fragment() {

    companion object {
        const val ARG_HISTORY_DATE = "arg.history_date"
    }

    private val viewModel: HistoryInputViewModel by viewModels()

    private val navController: NavController by lazy {  findNavController() }

    private fun getArgDate(): LocalDate = arguments?.getDate(ARG_HISTORY_DATE) ?: LocalDate.now()

    private fun getArgAccount(): Account?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountParcelable::class.java)?.toAccount() }

    private fun getArgGroup(): Group?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_GROUP, GroupParcelable::class.java)?.toGroup() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.setIsTransfer(false)
            viewModel.setDate(getArgDate())
            viewModel.setAccountSelection(getArgAccount())
            viewModel.setGroup(getArgGroup())
            
            if (isActionEdit()) {
                viewModel.findHistory(getArgId())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.history_input_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                val isSaving = viewModel.uiState.value.isSaving
                menu.findItem(R.id.action_save)?.isEnabled = !isSaving
                menu.findItem(R.id.action_cancel)?.isEnabled = !isSaving
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        viewModel.saveHistory()
                        true
                    }
                    R.id.action_cancel -> {
                        navController.popBackStack()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { it.isSaving }
                    .distinctUntilChanged()
                    .collectLatest {
                        requireActivity().invalidateOptionsMenu()
                    }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

            // TODO: why view composition strategy
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val onAddNewAccount = remember { { navController.navigate(R.id.navigate_to_create_account) } }
                val onAddNewGroup = remember { { navController.navigate(R.id.navigate_to_create_group) } }
                val exit = remember { { navController.popBackStack(); Unit } }
                ExpenseTrackerTheme {
                    TransactionInputScreen(
                        viewModel = viewModel,
                        isEdit = isActionEdit(),
                        onAddNewAccount = onAddNewAccount,
                        onAddNewGroup = onAddNewGroup,
                        exit = exit
                    )
                }
            }
        }
    }
}
