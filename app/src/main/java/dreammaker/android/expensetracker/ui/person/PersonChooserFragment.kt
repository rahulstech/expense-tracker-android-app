package dreammaker.android.expensetracker.ui.person

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.databinding.ChooserListLayoutBinding
import dreammaker.android.expensetracker.ui.util.ARG_DESTIATION_LABEL
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.createPersonChip

class PersonChooserFragment: Fragment() {

    private lateinit var binding: ChooserListLayoutBinding
    private lateinit var viewModel: PersonChooserViewModel
    private lateinit var navController: NavController
    private lateinit var selectionStore: SelectionStore<Long>
    private lateinit var adapter: PersonChooserListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[PersonChooserViewModel::class.java]
        if (arguments?.containsKey(ARG_DESTIATION_LABEL) == true) {
            requireActivity().title = arguments?.getString(ARG_DESTIATION_LABEL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChooserListLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        adapter = PersonChooserListAdapter()
        selectionStore = viewModel.selectionStore ?: SelectionStore()
        selectionStore.selectionProvider = adapter
        selectionStore.itemSelectionListener = this::handlePersonSelection
        adapter.selectionStore = selectionStore
        binding.optionsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.optionsList.adapter = adapter
        viewModel.getAllPeople().observe(viewLifecycleOwner, adapter::submitList)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.selectionStore = selectionStore
    }

    private fun handlePersonSelection(store: SelectionStore<Long>, key: Long, position: Int, selected: Boolean) {
        val mode = store.selectionMode
        if (mode == SelectionMode.SINGLE) {
            binding.selectionsContainer.removeAllViews()
            val person = adapter.currentList[position]
            val chip = createPersonChip(requireContext(), person)
            chip.tag = key
            chip.setOnCloseIconClickListener {
                selectionStore.changeSelection(key,false)
                binding.selectionsContainer.removeView(it)
            }
            binding.selectionsContainer.addView(chip)
        }
    }

    private fun handlePickPerson() {}
}