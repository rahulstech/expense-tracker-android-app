package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.SingleAccountPickerListWithSearchLayoutBinding
import dreammaker.android.expensetracker.util.AccountModelParcel
import dreammaker.android.expensetracker.util.Constants
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

    private var _binding: SingleAccountPickerListWithSearchLayoutBinding? = null
    private val binding get() = _binding!!
    private val navController: NavController by lazy { findNavController() }
    private val viewModel: AccountPickerViewModel by viewModels()
    private lateinit var adapter: AccountPickerListAdapter
    private lateinit var selectionHelper: SelectionHelper<Long>

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
        val accountId = arguments?.getLong(Constants.ARG_INITIAL_SELECTION)
        return accountId
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
        val resultKey = requireArguments().getString(Constants.ARG_RESULT_KEY)!!
        val resultValue = if (null == selectedAccount) null else AccountModelParcel(selectedAccount)
        navController.previousBackStackEntry?.savedStateHandle?.set(resultKey,resultValue)
        navController.popBackStack()
    }

    private fun getSelectedAccount(): AccountModel? {
        if (selectionHelper.count()==0) return null
        val key = selectionHelper.getSelections()[0]
        return viewModel.getAllAccounts().value?.find { it.id == key }
        return null;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}