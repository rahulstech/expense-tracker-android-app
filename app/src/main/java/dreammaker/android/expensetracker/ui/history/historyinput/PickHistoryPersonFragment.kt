package dreammaker.android.expensetracker.ui.history.historyinput

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.ui.person.PersonChooserFragment
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.setActivityTitle

class PickHistoryPersonFragment: PersonChooserFragment(SelectionMode.SINGLE) {

    companion object {
        private val TAG = PickHistoryPersonFragment::class.simpleName
    }

    private lateinit var historyViewModel: HistoryInputViewModel

    override fun getInitialSelections(): List<Long> {
        val person = historyViewModel.getPerson(requireArguments().getString(Constants.ARG_RESULT_KEY)!!)
        person?.let {
            return@getInitialSelections listOf(person.id!!)
        }
        return emptyList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        historyViewModel = ViewModelProvider(requireParentFragment(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[HistoryInputViewModel::class.java]
        if (arguments?.containsKey(Constants.ARG_DESTINATION_LABEL) == true) {
            setActivityTitle(arguments?.getString(Constants.ARG_DESTINATION_LABEL) as CharSequence)
        }
    }

    override fun onPeopleLoaded(people: List<PersonModel>?) {
        super.onPeopleLoaded(people)
        togglePickerButtonVisibility()
    }

    override fun handlePersonSelection(
        store: SelectionStore<Long>,
        key: Long,
        position: Int,
        selected: Boolean
    ) {
        super.handlePersonSelection(store, key, position, selected)
        togglePickerButtonVisibility()
    }

    private fun togglePickerButtonVisibility() {
        if (viewModel.personSelectionStore?.hasSelection() == true) {
            binding.btnChoose.show()
        }
        else {
            binding.btnChoose.hide()
        }
    }

    override fun handlePickPeople() {
        val selectedPerson = getSelectedPerson()
        val resultKey = requireArguments().getString(Constants.ARG_RESULT_KEY)!!
        historyViewModel.setPerson(resultKey, selectedPerson)
        navController.popBackStack()
    }
}