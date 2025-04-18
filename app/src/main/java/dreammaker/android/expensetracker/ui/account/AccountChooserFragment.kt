package dreammaker.android.expensetracker.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.ChooserListLayoutBinding
import dreammaker.android.expensetracker.ui.util.SelectionChipMaker
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.createAccountChip

interface IAccountChooserViewModel {

    var accountSelectionStore: SelectionStore<Long>?

    fun getAllAccounts(): LiveData<List<AccountModel>>
}

open class AccountChooserFragment(protected val selectionMode: SelectionMode) : Fragment() {

    private val TAG = AccountChooserFragment::class.simpleName

    private lateinit var adapter: AccountChooserListAdapter
    private lateinit var selectionStore: SelectionStore<Long>

    protected lateinit var binding: ChooserListLayoutBinding
    protected lateinit var navController: NavController
    protected lateinit var viewModel: IAccountChooserViewModel

    private lateinit var selectionChipMaker: SelectionChipMaker<Long>

    private val keyToAccountMap = mutableMapOf<Long,AccountModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChooserListLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        adapter = AccountChooserListAdapter()
        binding.optionsList.adapter = adapter
        binding.optionsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.btnChoose.setOnClickListener { handlePickAccount() }
        prepareSelectionStore(adapter)
        prepareSelectionChipMaker(

        )
        viewModel.getAllAccounts().observe(viewLifecycleOwner, this::onAccountsLoaded)
    }

    private fun prepareSelectionStore(adapter: AccountChooserListAdapter) {
        selectionStore = viewModel.accountSelectionStore ?: SelectionStore(selectionMode)
        selectionStore.selectionProvider = adapter
        selectionStore.itemSelectionListener = this::handleAccountSelection
        viewModel.accountSelectionStore = selectionStore
        adapter.selectionStore = selectionStore
    }

    private fun prepareSelectionChipMaker() {
        selectionChipMaker = SelectionChipMaker(
            binding.selectionsContainer
        ,{
            val account = getAccountForKey(it)!!
            val chip = createAccountChip(requireContext(), account)
            chip
        },{_,key ->
            selectionStore.changeSelection(key,false)
        })
    }

    protected open fun onAccountsLoaded(accounts: List<AccountModel>?) {
        keyToAccountMap.clear()
        accounts?.forEach { keyToAccountMap[it.id!!] = it }
        adapter.submitList(accounts)

        if (selectionStore.hasSelection()) {
            if (selectionMode == SelectionMode.SINGLE) {
                selectionChipMaker.addChip(selectionStore.selectedKey!!)
            }
            else {
                selectionChipMaker.addChips(selectionStore.selectedKeys!!)
            }
        }
    }

    protected open fun handlePickAccount() {}

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.accountSelectionStore = selectionStore
    }

    protected open fun handleAccountSelection(store: SelectionStore<Long>, key: Long, position: Int, selected: Boolean) {
        if (selectionMode == SelectionMode.SINGLE) {
            selectionChipMaker.removeAllChips()
        }
        if (selected) {
            selectionChipMaker.addChip(key)
        }
        else {
            selectionChipMaker.removeChip(key)
        }
    }

    protected fun getAccountAtPosition(position: Int): AccountModel = adapter.currentList[position]

    protected fun getAccountForKey(key: Long): AccountModel? = keyToAccountMap[key]

    protected fun getSelectedAccount(): AccountModel? = keyToAccountMap[selectionStore.selectedKey]

    protected fun getSelectedAccounts(): List<AccountModel> {
        val keySet = selectionStore.selectedKeys
        if (keyToAccountMap.isEmpty() || keySet.isNullOrEmpty()) {
            return emptyList()
        }
        return keySet.map { keyToAccountMap[it]!! }
    }
}