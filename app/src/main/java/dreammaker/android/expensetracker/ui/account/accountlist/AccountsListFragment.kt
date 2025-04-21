package dreammaker.android.expensetracker.ui.account.accountlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Account
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.AccountListBinding
import dreammaker.android.expensetracker.ui.util.Constants


class AccountsListFragment : Fragment() {

    private var _binding: AccountListBinding? = null
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
        _binding = AccountListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        adapter = AccountsAdapter()
        adapter.itemClickListener = this::handleAccountClick
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
        toggleEmptyView()
    }

    private fun toggleEmptyView() {
        if (adapter.currentList.isEmpty()) {
            binding.list.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        }
        else {
            binding.emptyView.visibility = View.GONE
            binding.list.visibility = View.VISIBLE
        }
    }

    private fun handleAccountClick(adapter: RecyclerView.Adapter<*>, view: View, position: Int) {
        val account = this.adapter.currentList[position]!!
        val args = Bundle().apply {
            putLong(Constants.ARG_ID, account.id!!)
        }
        navController.navigate(R.id.action_account_list_to_view_account, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.account_list_menu, menu)
//        onPrepareSearchMenu(
//            menu.findItem(R.id.search_account),
//            R.string.search_account,
//            saveData!!.queryAccount
//        )
    }

//    override fun onQueryTextSubmit(query: String): Boolean {
//        onFilter(query)
//        return true
//    }
//
//    override fun onQueryTextChange(newText: String): Boolean {
//        super.onQueryTextChange(newText)
//        if (Check.isEmptyString(newText)) {
//            onFilter(null)
//            return true
//        }
//        return false
//    }

//    private fun onFilter(key: String?) {
//        adapter!!.filter.filter(key)
//    }

//    override fun onItemChildClicked(
//        accountsAdapter: AccountsAdapter,
//        vh: AccountViewHolder,
//        v: View
//    ) {
//        if (null == context) return
//        val account: Account = adapter!!.getItem(vh.absoluteAdapterPosition)
//        if (vh.options === v) {
//            val menu = PopupMenu(context!!, v)
//            menu.inflate(R.menu.account_list_item_options_menu)
//            menu.setOnMenuItemClickListener { item: MenuItem ->
//                viewModel!!.selectedAccount = account
//                val itemId = item.itemId
//                if (R.id.edit === itemId) {
//                    onEditAccount(account)
//                    return@setOnMenuItemClickListener true
//                } else if (R.id.delete === itemId) {
//                    onDeleteAccount(account)
//                    return@setOnMenuItemClickListener true
//                }
//                false
//            }
//            menu.show()
//        } else if (vh.root === v) {
//            onViewTransactions(account)
//        }
//    }

//    private fun onAboutAccountFetched(accounts: List<AboutAccount>) {
//        adapter!!.submitList(accounts)
//        adapter!!.onRestoreData(saveData!!.adapterSaveData)
//    }

    private fun onAddAccount() {
//        viewModel!!.selectedAccount = null
//        navController!!.navigate(R.id.action_accountsList_to_inputAccount)
    }

    private fun onDeleteAccount(account: Account) {
//        if (null == context) return
//        AlertDialog.Builder(context!!)
//            .setMessage(resources.getQuantityString(R.plurals.warning_delete_accounts, 1))
//            .setPositiveButton(android.R.string.cancel, null)
//            .setNegativeButton(android.R.string.ok) { di: DialogInterface?, which: Int ->
//                viewModel!!.selectedAccount = null
//                viewModel!!.deleteAccount(account)
//            }
//            .show()
    }
}