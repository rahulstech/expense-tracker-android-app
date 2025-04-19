package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.HistoryInputLayoutBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.createAccountChip
import dreammaker.android.expensetracker.ui.util.createPersonChip
import dreammaker.android.expensetracker.ui.util.getDate
import dreammaker.android.expensetracker.ui.util.getHistoryType
import dreammaker.android.expensetracker.ui.util.setActivityTitle

class HistoryInputFragment : Fragment() {

    companion object {
        private val TAG = HistoryInputFragment::class.simpleName
        private const val HISTORY_INPUT_DATE_FORMAT = "dd-MMMM-yyyy"
        const val ARG_ACTION = "action"
        const val ACTION_CREATE_HISTORY = "action.history.create"
        const val ACTION_EDIT_HISTORY = "action.history.edit"
        const val ARG_HISTORY_TYPE = "arg.history_type"
        const val ARG_HISTORY_ID = "arg.history_id"
        const val ARG_HISTORY_DATE = "arg.history_date"
        const val ARG_SOURCE = "arg.source"
        const val ARG_DESTINATION = "arg.destination"
        const val ARG_SOURCE_ACCOUNT = "arg.source_account"
        const val ARG_DESTINATION_ACCOUNT = "arg.destination_account"
        const val ARG_SOURCE_PERSON = "arg.source_person"
        const val ARG_DESTINATION_PERSON = "arg.destination_person"
    }

    private lateinit var binding: HistoryInputLayoutBinding
    private lateinit var viewModel: HistoryInputViewModel
    private lateinit var navController: NavController
    private lateinit var selectedDate: Date
    private lateinit var selectedType: HistoryType

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireParentFragment(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[HistoryInputViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireArguments().containsKey(ARG_ACTION)) {
            throw IllegalStateException("'$ARG_ACTION' is not found in arguments")
        }
        val action = requireArguments().getString(ARG_ACTION, ACTION_CREATE_HISTORY)
        if (action == ACTION_EDIT_HISTORY && !requireArguments().containsKey(ARG_HISTORY_ID)) {
            throw IllegalStateException("when $ARG_ACTION=$ACTION_EDIT_HISTORY then $ARG_HISTORY_ID is required")
        }
        else if (!requireArguments().containsKey(ARG_HISTORY_TYPE)) {
            throw IllegalStateException("when $ARG_ACTION=$ACTION_CREATE_HISTORY then $ARG_HISTORY_TYPE is required")
        }

        val type = arguments?.getHistoryType(ARG_HISTORY_TYPE)
        val title: String = getString(if (action == ACTION_CREATE_HISTORY) {
            when(type) {
                HistoryType.CREDIT -> R.string.title_input_history_create_credit
                HistoryType.DEBIT -> R.string.title_input_history_create_debit
                HistoryType.TRANSFER -> R.string.title_input_history_create_transfer
                HistoryType.EXPENSE -> R.string.title_input_history_create_expense
                HistoryType.INCOME -> R.string.title_input_history_create_income
                null -> R.string.title_input_history
            }
        } else if (action == ACTION_EDIT_HISTORY) {
            R.string.title_input_history
        } else {
            R.string.title_input_history
        })
        setActivityTitle(title)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HistoryInputLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        setSelectedDate(requireArguments().getDate(ARG_HISTORY_DATE, Date())!!)
        binding.inputDate.setOnClickListener{ onClickInputDate(selectedDate) }
        selectedType = requireArguments().getHistoryType(ARG_HISTORY_TYPE,HistoryType.DEBIT)!!
        prepareSourceInput(selectedType)
        prepareDestinationInput(selectedType)
        viewModel.srcAccount.observe(viewLifecycleOwner, this::setSourceAccount)
        viewModel.destAccount.observe(viewLifecycleOwner, this::setDestinationAccount)
        viewModel.srcPerson.observe(viewLifecycleOwner, this::setSourcePerson)
        viewModel.destPerson.observe(viewLifecycleOwner, this::setDestinationPerson)
    }

    private fun prepareSourceInput(type: HistoryType) {
        if (type == HistoryType.CREDIT) {
            binding.labelInputSource.setText(R.string.label_history_input_source_credit)
            binding.inputSource.setOnClickListener{
                val args = Bundle().apply {
                    putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_credit_source_person))
                    putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                }
                navController.navigate(R.id.action_history_input_to_person_chooser_list, args)
            }
        }
        else if (type == HistoryType.DEBIT) {
            binding.labelInputSource.setText(R.string.label_history_input_source_debit)
            binding.inputSource.setOnClickListener{
                val args = Bundle().apply {
                    putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_debit_source_account))
                    putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
        }
        else if (type == HistoryType.TRANSFER) {
            binding.labelInputSource.setText(R.string.label_history_input_source_transfer)
            binding.inputSource.setOnClickListener{
                val args = Bundle().apply {
                    putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_transfer_source_person))
                    putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
        }
    }

    private fun prepareDestinationInput(type: HistoryType) {
        if (type == HistoryType.CREDIT) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_credit)
            binding.inputDestination.setOnClickListener{
                val args = Bundle().apply {
                    putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_credit_destination_account))
                    putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
        }
        else if (type == HistoryType.DEBIT) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_debit)
            binding.inputDestination.setOnClickListener{
                val args = Bundle().apply {
                    putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_debit_destination_person))
                    putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                }
                navController.navigate(R.id.action_history_input_to_person_chooser_list, args)
            }
        }
        else if (type == HistoryType.TRANSFER) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_transfer)
            binding.inputDestination.setOnClickListener{
                val args = Bundle().apply {
                    putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_transfer_destination_person))
                    putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
        }
    }

    private fun onClickInputDate(date: Date) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val newDate = Date(year, month, day)
            setSelectedDate(newDate)
        }, date.year, date.month, date.dayOfMonth)
        datePicker.show()
    }

    private fun setSelectedDate(date: Date) {
        selectedDate = date
        binding.inputDate.text = date.format(HISTORY_INPUT_DATE_FORMAT)
    }

    private fun setSourceAccount(account: AccountModel?) {
        binding.selectedSourceContainer.removeAllViews()
        account?.let {
            val chip = createAccountChip(requireContext(), account)
            binding.selectedSourceContainer.addView(chip)
        }
    }

    private fun setSourcePerson(person: PersonModel?) {
        binding.selectedSourceContainer.removeAllViews()
        person?.let {
            val chip = createPersonChip(requireContext(), person)
            chip.setOnCloseIconClickListener {
                viewModel.srcPerson.value = null
            }
            binding.selectedSourceContainer.addView(chip)
        }
    }

    private fun setDestinationAccount(account: AccountModel?) {
        binding.selectedDestinationContainer.removeAllViews()
        account?.let {
            val chip = createAccountChip(requireContext(), account)
            chip.setOnCloseIconClickListener {

            }
            binding.selectedDestinationContainer.addView(chip)
        }
    }

    private fun setDestinationPerson(person: PersonModel?) {
        binding.selectedDestinationContainer.removeAllViews()
        person?.let {
            val chip = createPersonChip(requireContext(), person)
            chip.setOnCloseIconClickListener {
                viewModel.destPerson.value = null
            }
            binding.selectedDestinationContainer.addView(chip)
        }
    }
}