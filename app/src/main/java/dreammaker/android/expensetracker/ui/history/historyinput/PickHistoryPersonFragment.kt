package dreammaker.android.expensetracker.ui.history.historyinput

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.ui.history.historyinput.PickerHistoryAccountFragment.Companion.SELECTED_DEST_ACCOUNT
import dreammaker.android.expensetracker.ui.history.historyinput.PickerHistoryAccountFragment.Companion.SELECTED_SRC_ACCOUNT
import dreammaker.android.expensetracker.ui.person.PersonChooserFragment
import dreammaker.android.expensetracker.ui.util.ARG_DESTIATION_LABEL
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.setActivityTitle

class PickHistoryPersonFragment: PersonChooserFragment(SelectionMode.SINGLE) {

    companion object {
        val ARG_KEY_SELECTED_PERSON = "arg.key_selected_person"
        val SELECTED_SRC_PERSON = "selectedSrcPerson"
        val SELECTED_DEST_PERSON = "selectedDestPerson"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[HistoryInputViewModel::class.java]
        if (arguments?.containsKey(ARG_DESTIATION_LABEL) == true) {
            setActivityTitle(arguments?.getString(ARG_DESTIATION_LABEL) as CharSequence)
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
        val historyInputVM = viewModel as HistoryInputViewModel
        val keySelectedPerson = requireArguments().getString(ARG_KEY_SELECTED_PERSON)
        val selectedPerson = getSelectedPerson()
        if (keySelectedPerson == SELECTED_SRC_PERSON) {
            historyInputVM.selectedSrcPerson.value = selectedPerson
        }
        else if (keySelectedPerson == SELECTED_DEST_PERSON) {
            historyInputVM.selectedDestPerson.value = selectedPerson
        }
        navController.popBackStack()
    }
}