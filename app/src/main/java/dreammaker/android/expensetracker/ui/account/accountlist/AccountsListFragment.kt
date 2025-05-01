package dreammaker.android.expensetracker.ui.account.accountlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.AccountsListBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible


class AccountsListFragment : Fragment() {

    private var _binding: AccountsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AccountsAdapter
    private lateinit var viewModel: AccountsListViewModel
    private lateinit var navController: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[AccountsListViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AccountsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
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
        adapter.submitList(accounts)
        if (adapter.currentList.isEmpty()) {
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