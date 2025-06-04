package dreammaker.android.expensetracker.ui.account.accountlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.AccountsListBinding
import dreammaker.android.expensetracker.util.Constants
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible


class AccountsListFragment : Fragment() {
    private val TAG = AccountsListFragment::class.simpleName

    private var _binding: AccountsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AccountsAdapter
    private val viewModel: AccountsListViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AccountsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = AccountsAdapter()
        adapter.itemClickListener = { _,_,position -> handleAccountClick(adapter.currentList[position]) }
        binding.list.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.list.adapter = adapter
        binding.add.setOnClickListener {
            navController.navigate(R.id.action_account_list_to_create_account, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
            })
        }
        viewModel.getAllAccounts().observe(viewLifecycleOwner, this::onAccountsLoaded)
    }

    private fun onAccountsLoaded(accounts: List<AccountModel>) {
        Log.i(TAG, "loaded account ${accounts.size}")
        adapter.submitList(accounts)
        if (accounts.isEmpty()) {
            binding.list.visibilityGone()
            binding.emptyView.visible()
        }
        else {
            binding.emptyView.visibilityGone()
            binding.list.visible()
        }
    }

    private fun handleAccountClick(account: AccountModel) {
        val args = Bundle().apply {
            putLong(Constants.ARG_ID, account.id!!)
        }
        navController.navigate(R.id.action_account_list_to_view_account, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}