package dreammaker.android.expensetracker.ui.person

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.ChooserListLayoutBinding
import dreammaker.android.expensetracker.ui.util.ARG_DESTIATION_LABEL
import dreammaker.android.expensetracker.ui.util.SelectionChipMaker
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.createPersonChip

interface IPersonChooserViewModel {

    var personSelectionStore: SelectionStore<Long>?

    fun getAllPeople(): LiveData<List<PersonModel>>
}

open class PersonChooserFragment(val selectionMode: SelectionMode): Fragment() {

    protected lateinit var binding: ChooserListLayoutBinding
    protected lateinit var viewModel: IPersonChooserViewModel
    protected lateinit var navController: NavController
    private lateinit var selectionStore: SelectionStore<Long>
    private lateinit var adapter: PersonChooserListAdapter
    private lateinit var selectionChipMaker: SelectionChipMaker<Long>
    private val keyToPersonMap = mutableMapOf<Long,PersonModel>()

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
        binding.optionsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.optionsList.adapter = adapter
        binding.btnChoose.setOnClickListener{ handlePickPeople() }
        prepareSelectionStore(adapter)
        prepareSelectionChipMaker()

        viewModel.getAllPeople().observe(viewLifecycleOwner, this::onPeopleLoaded)
    }

    private fun prepareSelectionStore(adapter: PersonChooserListAdapter) {
        selectionStore = viewModel.personSelectionStore ?: SelectionStore(selectionMode)
        selectionStore.selectionProvider = adapter
        selectionStore.itemSelectionListener = this::handlePersonSelection
        adapter.selectionStore = selectionStore
        viewModel.personSelectionStore = selectionStore
    }

    private fun prepareSelectionChipMaker() {
        selectionChipMaker = SelectionChipMaker(
            binding.selectionsContainer
        , {
            val person = getPersonForKey(it) ?: return@SelectionChipMaker null
            val chip = createPersonChip(requireContext(), person)
            chip
        },{ _,key ->
            selectionStore.changeSelection(key,false)
        })
    }

    protected open fun onPeopleLoaded(people: List<PersonModel>?) {
        keyToPersonMap.clear()
        people?.forEach{ keyToPersonMap[it.id!!] = it}
        adapter.submitList(people)
        if (selectionStore.hasSelection()) {
            if (selectionMode == SelectionMode.SINGLE) {
                selectionChipMaker.addChip(selectionStore.selectedKey!!)
            }
            else if (selectionMode == SelectionMode.MULTIPLE) {
                selectionChipMaker.addChips(selectionStore.selectedKeys!!)
            }
        }
    }

    protected open fun handlePickPeople() {}

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.personSelectionStore = selectionStore
    }

    protected open fun handlePersonSelection(store: SelectionStore<Long>, key: Long, position: Int, selected: Boolean) {
        val mode = store.selectionMode
        if (mode == SelectionMode.SINGLE) {
            selectionChipMaker.removeAllChips()
        }
        if (selected) {
            selectionChipMaker.addChip(key)
        }
        else {
            selectionChipMaker.removeChip(key)
        }
    }

    protected fun getPersonAtPosition(position: Int): PersonModel = adapter.currentList[position]

    protected fun getPersonForKey(key: Long): PersonModel? = keyToPersonMap[key]

    protected fun getSelectedPerson(): PersonModel? = keyToPersonMap[selectionStore.selectedKey]

    protected fun getSelectedPeople(): List<PersonModel> {
        val keySet = selectionStore.selectedKeys
        if (keyToPersonMap.isEmpty() || keySet.isNullOrEmpty()) {
            return emptyList()
        }
        return keySet.map { keyToPersonMap[it]!! }
    }
}