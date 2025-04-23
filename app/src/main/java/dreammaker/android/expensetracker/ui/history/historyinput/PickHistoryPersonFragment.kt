package dreammaker.android.expensetracker.ui.history.historyinput

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.PickerListLayoutBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.setActivityTitle

open class PickHistoryPersonFragment: Fragment() {

    private var _binding: PickerListLayoutBinding? = null
    private val binding get() = _binding!!

    protected lateinit var viewModel: PersonPickerViewModel
    private lateinit var historyViewModel: HistoryInputViewModel
    protected lateinit var navController: NavController
    private lateinit var selectionStore: SelectionStore<Long>
    private lateinit var adapter: PersonPickerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[PersonPickerViewModel::class.java]
        historyViewModel = ViewModelProvider(requireParentFragment(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[HistoryInputViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PickerListLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        NavigationUI.setupActionBarWithNavController(requireActivity() as AppCompatActivity, navController)

        adapter = PersonPickerAdapter()
        binding.optionsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.optionsList.adapter = adapter
        binding.btnChoose.setOnClickListener{ handlePickPeople() }
        prepareSelectionStore(adapter)

        viewModel.getAllPeople().observe(viewLifecycleOwner, this::onPeopleLoaded)
    }

    private fun prepareSelectionStore(adapter: PersonPickerAdapter) {
        selectionStore = viewModel.personSelectionStore
            ?: SelectionStore<Long>(SelectionMode.SINGLE).apply { setInitialKey(getInitialSelection())}
        selectionStore.selectionProvider = adapter
        selectionStore.itemSelectionListener = { _,_,_,_ ->
            if (selectionStore.hasSelection()) {
                showPickerButton()
            }
            else {
                hidePickerButton()
            }
        }
        adapter.selectionStore = selectionStore
        viewModel.personSelectionStore = selectionStore
    }

    private fun showPickerButton() {
        binding.btnChoose.visibility = View.VISIBLE
    }

    private fun hidePickerButton() {
        binding.btnChoose.visibility = View.GONE
    }

    protected open fun getInitialSelection(): Long? {
        val resultKey = requireArguments().getString(Constants.ARG_RESULT_KEY)!!
        val selection = historyViewModel.getSelection(resultKey) as PersonModel?
        return selection?.id
    }

    protected open fun onPeopleLoaded(people: List<PersonModel>?) {
        adapter.submitList(people)
        if (selectionStore.hasSelection()) {
            showPickerButton()
        }
    }

    private fun handlePickPeople() {
        val selectedPerson = getSelectedPerson()
        val resultKey = requireArguments().getString(Constants.ARG_RESULT_KEY)!!
        historyViewModel.setSelection(resultKey, selectedPerson)
        navController.popBackStack()
    }

    protected fun getSelectedPerson(): PersonModel? {
        val key = selectionStore.selectedKey ?: return null
        return adapter.currentList.find { it.id == key }
    }

    override fun onResume() {
        super.onResume()
        arguments?.getString(Constants.ARG_DESTINATION_LABEL)?.let { setActivityTitle(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}