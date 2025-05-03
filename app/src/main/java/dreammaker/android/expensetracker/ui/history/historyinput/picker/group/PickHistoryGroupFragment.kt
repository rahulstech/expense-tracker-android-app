package dreammaker.android.expensetracker.ui.history.historyinput.picker.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.databinding.SingleGroupPickerListWithSearchLayoutBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.GroupModelParcel
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore

open class PickHistoryGroupFragment : Fragment() {

    private val TAG = PickHistoryGroupFragment::class.simpleName

    private var _binding: SingleGroupPickerListWithSearchLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: GroupPickerListAdapter
    private lateinit var selectionStore: SelectionStore<Long>
    protected val navController: NavController by lazy { findNavController() }
    protected val viewModel: GroupPickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SingleGroupPickerListWithSearchLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.optionsList.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = GroupPickerListAdapter().also {
                this@PickHistoryGroupFragment.adapter = it
            }

        }
        binding.btnChoose.setOnClickListener { handlePickGroup() }
        prepareSelectionStore(adapter)
        viewModel.getAllGroups().observe(viewLifecycleOwner, this::onGroupsLoaded)
    }

    private fun prepareSelectionStore(adapter: GroupPickerListAdapter) {
        selectionStore = viewModel.groupSelectionStore
            ?: SelectionStore<Long>(SelectionMode.SINGLE).apply { setInitialKey(getInitialSelection()) }
        selectionStore.itemSelectionListener = { _,_,_,_ ->
            if (selectionStore.hasSelection()) {
                binding.btnChoose.show()
            }
            else {
                binding.btnChoose.hide()
            }
        }
        selectionStore.selectionProvider = adapter
        viewModel.groupSelectionStore = selectionStore
        adapter.selectionStore = selectionStore
    }

    protected open fun getInitialSelection(): Long? {
        val accountId = arguments?.getLong(Constants.ARG_INITIAL_SELECTION)
        return accountId
    }

    protected open fun onGroupsLoaded(accounts: List<GroupModel>) {
        adapter.submitList(accounts)
        if (selectionStore.hasSelection()) {
            binding.btnChoose.show()
        }
    }

    private fun handlePickGroup() {
        val selectedGroup = getSelectedGroup()
        val resultKey = requireArguments().getString(Constants.ARG_RESULT_KEY)!!
        val resultValue = if (null == selectedGroup) null else GroupModelParcel(selectedGroup)
        navController.previousBackStackEntry?.savedStateHandle?.set(resultKey,resultValue)
        navController.popBackStack()
    }

    private fun getSelectedGroup(): GroupModel? {
        val key = selectionStore.selectedKey ?: return null
        return viewModel.getAllGroups().value?.find { it.id == key }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}