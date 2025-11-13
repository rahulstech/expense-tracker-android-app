package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.SingleAccountPickerListWithSearchLayoutBinding
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputViewModel
import dreammaker.android.expensetracker.util.SelectionHelper
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible

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

class PickHistoryAccountFragment : Fragment() {

    private val TAG = PickHistoryAccountFragment::class.simpleName

    private var _binding: SingleAccountPickerListWithSearchLayoutBinding? = null
    private val binding get() = _binding!!
    private val navController: NavController by lazy { findNavController() }
    private val viewModel: AccountPickerViewModel by viewModels()
    private val historyViewModel: HistoryInputViewModel by activityViewModels()
    private lateinit var adapter: AccountPickerListAdapter
    private lateinit var selectionHelper: SelectionHelper<Long>

    private fun getArgIsPrimary(): Boolean =
        arguments?.getBoolean(Constants.KEY_IS_PRIMARY,true) ?: true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SingleAccountPickerListWithSearchLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnChoose.setOnClickListener { handlePickAccount() }

        adapter = AccountPickerListAdapter()
        binding.optionsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.optionsList.adapter = adapter

        viewModel.getAllAccounts().observe(viewLifecycleOwner, this::onAccountsLoaded)

        selectionHelper = SelectionHelper<Long>(adapter) {
            SelectionTracker.Builder<Long>(
                "singleAccountSelection",
                binding.optionsList,
                AccountPickerSelectionKeyProvider(adapter),
                AccountPickerDetailsLookup(binding.optionsList),
                StorageStrategy.createLongStorage()
            )
        }

        selectionHelper.startSelection(SelectionPredicates.createSelectSingleAnything()) {
            selectionHelper.selectItem(getInitialSelection())
        }
    }

    private fun getInitialSelection(): Long? {
        return historyViewModel.getAccount(getArgIsPrimary())?.id
    }

    private fun onAccountsLoaded(accounts: List<AccountModel>) {
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
        Log.i(TAG, "selected account $selectedAccount is_primary = ${getArgIsPrimary()}")
        historyViewModel.setAccount(selectedAccount, getArgIsPrimary())
        navController.popBackStack()
    }

    private fun getSelectedAccount(): AccountModel? {
        if (selectionHelper.count()==0) return null
        val key = selectionHelper.getSelections()[0]
        return viewModel.getAllAccounts().value?.find { it.id == key }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}