package dreammaker.android.expensetracker.ui.account

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.databinding.ChooserListLayoutBinding
import dreammaker.android.expensetracker.ui.util.SelectionChipMaker
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.createAccountChip

class AccountChooserViewModel(app: Application): AndroidViewModel(app) {

    private val accountDao: AccountDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        accountDao = db.accountDao
    }

    var accountSelectionStore: SelectionStore<Long>? = null

    private lateinit var allAccount: LiveData<List<AccountModel>>

    fun getAllAccounts(): LiveData<List<AccountModel>> {
        if (!::allAccount.isInitialized) {
            allAccount = accountDao.getAllAccounts()
        }
        return allAccount
    }
}

open class AccountChooserFragment(protected val selectionMode: SelectionMode) : Fragment() {

    private val TAG = AccountChooserFragment::class.simpleName

    private lateinit var adapter: AccountChooserListAdapter
    private lateinit var selectionStore: SelectionStore<Long>

    protected lateinit var binding: ChooserListLayoutBinding
    protected lateinit var navController: NavController
    protected lateinit var viewModel: AccountChooserViewModel

    private lateinit var selectionChipMaker: SelectionChipMaker<Long>

    private val keyToAccountMap = mutableMapOf<Long,AccountModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[AccountChooserViewModel::class.java]
    }

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
        selectionStore = viewModel.accountSelectionStore
            ?: SelectionStore<Long>(selectionMode).apply { setInitialKeys(getInitialSelections()) }
        selectionStore.selectionProvider = adapter
        selectionStore.itemSelectionListener = this::handleAccountSelection
        viewModel.accountSelectionStore = selectionStore
        adapter.selectionStore = selectionStore
    }

    protected open fun getInitialSelections(): List<Long> = emptyList()

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