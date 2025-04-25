package dreammaker.android.expensetracker.ui.account.viewaccount

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.ViewAccountLayoutBinding
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.hasArgument
import dreammaker.android.expensetracker.ui.util.isVisible
import dreammaker.android.expensetracker.ui.util.putHistoryType
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ViewAccountFragment: Fragment() {

    companion object {
        private val TAG = ViewAccountFragment::class.simpleName
    }

    private var _binding: ViewAccountLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var viewModel: ViewAccountViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[ViewAccountViewModel::class.java]
        setHasOptionsMenu(true)
    }

    private fun getArgAccountId(): Long = requireArguments().getLong(Constants.ARG_ID)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        viewModel.getStoredAccount()?.let {
            inflater.inflate(R.menu.view_account_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.edit -> {
                navController.navigate(R.id.action_view_account_to_edit_account, Bundle().apply {
                    putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
                    putLong(Constants.ARG_ID, getArgAccountId())
                })
                true
            }
            R.id.delete -> {
                onClickDeleteAccount()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun onClickDeleteAccount() {
        val account = viewModel.getStoredAccount()
        account?.let {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(resources.getQuantityString(R.plurals.warning_delete_accounts, 1, account.name))
                .setPositiveButton(R.string.label_no, null)
                .setNegativeButton(R.string.label_yes) { _,_ -> viewModel.removeAccount(account) }
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasArgument(Constants.ARG_ID)) {
            throw IllegalStateException("'${Constants.ARG_ID}' argument not found")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewAccountLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        binding.addHistory.setOnClickListener {
            val target = binding.buttonsLayout
            if (target.isVisible()) {
                target.visibilityGone()
            }
            else {
                target.visible()
            }
        }
        binding.btnAddDebit.setOnClickListener {
            navController.navigate(R.id.action_view_account_to_create_history, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
                putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, HistoryType.DEBIT)
                putLong(HistoryInputFragment.ARG_SOURCE, getArgAccountId())
            })
        }
        binding.btnAddDebit.setOnClickListener {
            navController.navigate(R.id.action_view_account_to_create_history, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
                putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, HistoryType.CREDIT)
                putLong(HistoryInputFragment.ARG_DESTINATION, getArgAccountId())
            })
        }
        binding.btnAddTransfer.setOnClickListener {
            navController.navigate(R.id.action_view_account_to_create_history, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
                putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, HistoryType.TRANSFER)
                putLong(HistoryInputFragment.ARG_SOURCE, getArgAccountId())
            })
        }
        binding.btnViewHistory.setOnClickListener { handleClickViewHistory() }
        viewModel.findAccountById(getArgAccountId()).observe(viewLifecycleOwner, this::onAccountLoaded)
        lifecycleScope.launch {
            viewModel.resultFlow.filterNotNull().collect {
                onAccountDeleted(it)
                viewModel.emptyResult()
            }
        }
    }

    private fun handleClickViewHistory() {
    }

    private fun onAccountLoaded(account: AccountModel?) {
        if (account == null) {
            Toast.makeText(requireContext(), R.string.message_account_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return
        }
        else {
            prepareName(account.name!!)
            prepareBalance(account.balance!!)
            requireActivity().invalidateOptionsMenu()
        }
    }

    private fun prepareBalance(balance: Float) {
        binding.balance.text = balance.toCurrencyString()
    }

    private fun prepareName(name: String) {
        binding.name.text = name
    }

    private fun onAccountDeleted(result: OperationResult<AccountModel>?) {
        result?.let {
            if (!result.isFailure()) {
                navController.popBackStack()
            }
            else {
                Log.e(TAG, "onAccountDeleted: id=$id", result.error)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}