package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.SingleAccountPickerListWithSearchLayoutBinding
import dreammaker.android.expensetracker.ui.util.AccountModelParcel
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.SelectionStoreViewModel
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible

class AccountSelectionViewModel: SelectionStoreViewModel<Long>()

class PickHistoryAccountFragment : Fragment() {

    private val TAG = PickHistoryAccountFragment::class.simpleName

    private var _binding: SingleAccountPickerListWithSearchLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AccountPickerListAdapter
    private lateinit var selectionStore: SelectionStore<Long>
    protected val navController: NavController by lazy { findNavController() }
    protected val viewModel: AccountPickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SingleAccountPickerListWithSearchLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.optionsList.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = AccountPickerListAdapter().also {
                this@PickHistoryAccountFragment.adapter = it
            }
        }
        binding.btnChoose.setOnClickListener { handlePickAccount() }
        prepareSelectionStore(adapter)
        viewModel.getAllAccounts().observe(viewLifecycleOwner, this::onAccountsLoaded)
    }

    private fun prepareSelectionStore(adapter: AccountPickerListAdapter) {
        val selectionViewModel = ViewModelProvider(this)[AccountSelectionViewModel::class.java]
        selectionStore = SelectionStore(SelectionMode.SINGLE, adapter, selectionViewModel).apply { setInitialKey(getInitialSelection()) }
        adapter.selectionStore = selectionStore
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
            if (selectionStore.hasSelection()) {
                binding.btnChoose.visible()
            }
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
        val key = selectionStore.selectedKey ?: return null
        return viewModel.getAllAccounts().value?.find { it.id == key }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}