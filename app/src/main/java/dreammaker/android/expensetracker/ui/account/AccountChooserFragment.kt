package dreammaker.android.expensetracker.ui.account

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.ChooserListLayoutBinding
import dreammaker.android.expensetracker.ui.util.ARG_DESTIATION_LABEL
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.createAccountChip
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

open class AccountChooserFragment : Fragment() {

    private val TAG = AccountChooserFragment::class.simpleName

    private lateinit var binding: ChooserListLayoutBinding
    private lateinit var viewModel: AccountChooserViewModel
    private lateinit var adapter: AccountChooserListAdapter
    private lateinit var navController: NavController
    private lateinit var selectionStore: SelectionStore<Long>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[AccountChooserViewModel::class.java]
        if (arguments?.containsKey(ARG_DESTIATION_LABEL) == true) {
            requireActivity().title = arguments?.getString(ARG_DESTIATION_LABEL)
        }
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
        selectionStore = viewModel.selectionStore ?: SelectionStore()
        selectionStore.itemSelectionListener = this::handleAccountSelection
        selectionStore.selectionProvider = adapter
        adapter.selectionStore = selectionStore
        binding.optionsList.adapter = adapter
        binding.optionsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.btnChoose.setOnClickListener{ handlePickAccounts() }
        viewModel.getAllAccounts().observe(viewLifecycleOwner, adapter::submitList)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.selectionStore = selectionStore
    }

    private fun handleAccountSelection(store: SelectionStore<Long>, key: Long, position: Int, selected: Boolean) {
        val mode = store.selectionMode
        if (mode == SelectionMode.SINGLE) {
            binding.selectionsContainer.removeAllViews()
            val account = adapter.currentList[position]
            val chip = createAccountChip(requireContext(), account)
            chip.tag = key
            chip.setOnCloseIconClickListener {
                selectionStore.changeSelection(it.tag as Long, false)
                binding.selectionsContainer.removeView(it)
            }
            binding.selectionsContainer.addView(chip)
        }

        if (selectionStore.hasSelection()) {
            binding.btnChoose.show()
        }
        else {
            binding.btnChoose.hide()
        }
    }

    private fun handlePickAccounts() {

    }
}