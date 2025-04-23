package dreammaker.android.expensetracker.ui.account.viewaccount

import android.content.Context
import android.os.Bundle
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
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.ViewAccountLayoutBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import kotlinx.coroutines.launch

class ViewAccountFragment: Fragment() {

    companion object {
        private val TAG = ViewAccountFragment::class.simpleName
    }

    private var binding: ViewAccountLayoutBinding? = null
    private lateinit var navController: NavController
    private lateinit var viewModel: ViewAccountViewModel

    private var account: AccountModel? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[ViewAccountViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (null != account) {
            inflater.inflate(R.menu.view_account_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit -> {
                navController.navigate(R.id.action_view_account_to_edit_account, Bundle().apply {
                    putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
                    putLong(Constants.ARG_ID, requireArguments().getLong(Constants.ARG_ID))
                })
            }
            R.id.delete -> onClickDeleteAccount()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onClickDeleteAccount() {
        // TODO: modify account delete
        viewModel.removeAccount(account!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey(Constants.ARG_ID) != true) {
            throw IllegalStateException("'${Constants.ARG_ID}' argument not found")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewAccountLayoutBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)

        binding!!.btnViewHistory.setOnClickListener { handleClickViewHistory() }

        val id = requireArguments().getLong(Constants.ARG_ID)
        viewModel.findAccountById(id).observe(viewLifecycleOwner, this::onAccountLoaded)

        lifecycleScope.launch {
            viewModel.resultFlow.collect {
                onAccountDeleted(it)
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
            this.account = account
            prepareName(account.name!!, binding!!)
            prepareBalance(account.balance!!, binding!!)
            requireActivity().invalidateOptionsMenu()
        }
    }

    private fun prepareBalance(balance: Float, binding: ViewAccountLayoutBinding) {
        binding.balance.text = balance.toCurrencyString()
    }

    private fun prepareName(name: String, binding: ViewAccountLayoutBinding) {
        binding.name.text = name
    }

    private fun onAccountDeleted(result: OperationResult<AccountModel>?) {
        result?.let {
            if (!result.isFailure()) {
                navController.popBackStack()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}