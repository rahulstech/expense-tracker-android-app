package dreammaker.android.expensetracker.ui.account.accountlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.AccountsListBinding
import dreammaker.android.expensetracker.util.SelectionHelper
import dreammaker.android.expensetracker.util.UIState
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account

class AccountSelectionKeyProvider(val adapter: AccountsAdapter): ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long? = adapter.getSelectionKey(position)

    override fun getPosition(key: Long): Int = adapter.getKeyPosition(key)
}

class AccountDetailsLookup(val rv: RecyclerView): ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long?>? {
        val itemView = rv.findChildViewUnder(e.x,e.y)
        return itemView?.let { view ->
            val vh = rv.getChildViewHolder(view) as AccountViewHolder
            vh.getSelectedItemDetails()
        }
    }
}

class AccountsListFragment : Fragment() {

    companion object {
        private val TAG = AccountsListFragment::class.simpleName
    }

    private lateinit var binding: AccountsListBinding
    private lateinit var adapter: AccountsAdapter
    private val viewModel: AccountsListViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    private lateinit var selectionHelper: SelectionHelper<Long>
    private val cabMenuProvider = object: MenuProvider {

        override fun onCreateMenu(
            menu: Menu,
            menuInflater: MenuInflater
        ) {
            menuInflater.inflate(R.menu.accounts_list_cab_menu,menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when(menuItem.itemId) {
                R.id.delete_multiple -> {
                    onClickDeleteMultiple()
                    true
                }
                else -> false
            }
        }
    }

    private fun onClickDeleteMultiple() {
        if (selectionHelper.count() > 0) {
            QuickMessages.alertWarning(
                requireContext(),
                getString(R.string.message_warning_delete_multiple, selectionHelper.count()),
                QuickMessages.AlertButton(getString(R.string.label_no)),
                QuickMessages.AlertButton(getString(R.string.label_yes)) {
                    viewModel.deleteAccounts(selectionHelper.getSelections())
                    selectionHelper.endSelection()
                },
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AccountsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchBar.searchInput.apply {
            hint = getString(R.string.label_search_account_name)
            setText(viewModel.searchText)
            addTextChangedListener { editable ->
                viewModel.searchText = editable.toString()
            }
        }
        binding.add.setOnClickListener {
            navController.navigate(R.id.action_account_list_to_create_account, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
            })
        }

        adapter = AccountsAdapter()
        binding.list.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.list.adapter = adapter

        prepareItemSelection()

        viewModel.getAllAccounts().observe(viewLifecycleOwner, this::onAccountsLoaded)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteAccountsState.filterNotNull()
                .collectLatest { state ->
                    when (state) {
                        is UIState.UISuccess -> {
                            QuickMessages.toastSuccess(requireContext(),getString(R.string.message_success_delete_multiple_accounts))
                        }
                        is UIState.UIError -> {
                            QuickMessages.simpleAlertError(requireContext(),R.string.message_fail_delete_multiple_accounts)
                        }
                        else -> {}
                    }
                }
        }
    }

    private fun prepareItemSelection() {
        selectionHelper = SelectionHelper(adapter,this,viewLifecycleOwner) {
            SelectionTracker.Builder(
                "multipleAccountSelection",
                binding.list,
                AccountSelectionKeyProvider(adapter),
                AccountDetailsLookup(binding.list),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(SelectionPredicates.createSelectAnything())

        }.apply {
            prepareContextualActionBar(requireActivity(),cabMenuProvider)
        }

        adapter.itemLongClickListener = { _,_,position ->
            if (selectionHelper.startSelection(true)) {
                selectionHelper.selectItem(adapter.getSelectionKey(position))
            }
            true
        }

        selectionHelper.itemClickListener = { _,_,position ->
            handleAccountClick(adapter.currentList[position])
        }

        selectionHelper.itemSelectionChangeCallback = { _,_,_,_ ->
            selectionHelper.cabViewModel?.cabTitle = selectionHelper.count().toString()
        }
    }

    private fun onAccountsLoaded(accounts: List<Account>) {
        adapter.submitList(accounts)
        if (accounts.isEmpty()) {
            binding.list.visibilityGone()
            binding.emptyView.visible()
        }
        else {
            binding.emptyView.visibilityGone()
            binding.list.visible()
            Log.i(TAG,"selections count ${selectionHelper.getSelections().size}")
        }
    }

    private fun handleAccountClick(account: Account) {
        val args = Bundle().apply {
            putLong(Constants.ARG_ID, account.id)
        }
        navController.navigate(R.id.action_account_list_to_view_account, args)
    }
}