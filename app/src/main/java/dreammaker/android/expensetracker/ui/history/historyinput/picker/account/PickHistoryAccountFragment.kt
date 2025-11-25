package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.SingleAccountPickerListWithSearchLayoutBinding
import dreammaker.android.expensetracker.ui.AccountListItem
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputViewModel
import dreammaker.android.expensetracker.util.SelectionHelper
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import rahulstech.android.expensetracker.domain.model.Account

class AccountPickerSelectionKeyProvider(private val adapter: AccountPickerListAdapter): ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long? = adapter.getSelectionKey(position)

    override fun getPosition(key: Long): Int = adapter.getKeyPosition(key)
}

class AccountPickerDetailsLookup(private val rv: RecyclerView): ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long?>? {
        val itemView = rv.findChildViewUnder(e.x,e.y)
        return itemView?.let { view ->
            val vh = rv.getChildViewHolder(view) as AccountPickerViewHolder
            vh.getSelectedItemDetails()
        }
    }
}

class AccountPickerPredicate(val adapter: AccountPickerListAdapter): SelectionTracker.SelectionPredicate<Long>() {
    override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
        val position = adapter.getKeyPosition(key)
        return adapter.getItemViewType(position) == AccountPickerListAdapter.TYPE_ITEM
    }

    override fun canSetStateAtPosition(
        position: Int,
        nextState: Boolean
    ): Boolean = adapter.getItemViewType(position) == AccountPickerListAdapter.TYPE_ITEM

    override fun canSelectMultiple(): Boolean = false
}

class PickHistoryAccountFragment : Fragment() {

    companion object {
        private val TAG = PickHistoryAccountFragment::class.simpleName
        const val ARG_DISABLED_ACCOUNT = "arg_disabled_account"
    }

    private lateinit var binding: SingleAccountPickerListWithSearchLayoutBinding
    private val navController: NavController by lazy { findNavController() }
    private val viewModel: AccountPickerViewModel by viewModels()
    private val historyViewModel: HistoryInputViewModel by activityViewModels()
    private lateinit var adapter: AccountPickerListAdapter
    private lateinit var selectionHelper: SelectionHelper<Long>

    private val argIsPrimary: Boolean
        get() = arguments?.getBoolean(Constants.KEY_IS_PRIMARY,true) ?: true

    private val argDisabledAccountId: Long?
        get() = arguments?.getLong(ARG_DISABLED_ACCOUNT)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SingleAccountPickerListWithSearchLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchBar.searchInput.apply {
            hint = getString(R.string.label_search_account_name)
            addTextChangedListener { editable ->
                viewModel.searchText = editable.toString()
            }
        }
        binding.btnChoose.setOnClickListener { handlePickAccount() }

        adapter = AccountPickerListAdapter()
        binding.optionsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.optionsList.adapter = adapter

        prepareItemSelection()

        viewModel.getAllAccounts().observe(viewLifecycleOwner, this::onAccountsLoaded)
    }

    private fun prepareItemSelection() {
        selectionHelper = SelectionHelper(adapter,this,viewLifecycleOwner) {
            SelectionTracker.Builder(
                "singleAccountSelection",
                binding.optionsList,
                AccountPickerSelectionKeyProvider(adapter),
                AccountPickerDetailsLookup(binding.optionsList),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(AccountPickerPredicate(adapter))
        }

        selectionHelper.startSelection(initialSelection = getInitialSelection())
    }

    private fun getInitialSelection(): Long? {
        return historyViewModel.getAccount(argIsPrimary)?.id
    }

    private fun onAccountsLoaded(accounts: List<AccountListItem>) {
        adapter.submitList(accounts)
        if (accounts.isEmpty()) {
            binding.optionsList.visibilityGone()
            binding.emptyPlaceholder.visible()
        }
        else {
            binding.emptyPlaceholder.visibilityGone()
            binding.optionsList.visible()
        }
    }

    private fun handlePickAccount() {
        val selectedAccount = getSelectedAccount()
        Log.d(TAG, "selected account $selectedAccount is_primary = $argIsPrimary")
        historyViewModel.setAccount(selectedAccount, argIsPrimary)
        navController.popBackStack()
    }

    private fun getSelectedAccount(): Account? {
        val key = selectionHelper.getFirstSelection() ?: return null
        val item = viewModel.accountListItems.find { item ->
            item is AccountListItem.Item && item.data.id == key
        }
        return (item as? AccountListItem.Item)?.data
    }
}