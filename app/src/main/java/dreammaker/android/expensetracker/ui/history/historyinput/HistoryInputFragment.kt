package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.chip.Chip
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.HistoryInputLayoutBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.createAccountChip
import dreammaker.android.expensetracker.ui.util.createPersonChip
import dreammaker.android.expensetracker.ui.util.getAccountModel
import dreammaker.android.expensetracker.ui.util.getDate
import dreammaker.android.expensetracker.ui.util.getHistoryType
import dreammaker.android.expensetracker.ui.util.getPersonModel
import dreammaker.android.expensetracker.ui.util.setActivityTitle

class HistoryInputFragment : Fragment() {

    companion object {
        private val TAG = HistoryInputFragment::class.simpleName
        private const val HISTORY_INPUT_DATE_FORMAT = "EEEE, dd MMMM, yyyy"
        const val ARG_ACTION = "action"
        const val ACTION_CREATE_HISTORY = "action.history.create"
        const val ACTION_EDIT_HISTORY = "action.history.edit"
        const val ARG_HISTORY_TYPE = "arg.history_type"
        const val ARG_HISTORY_ID = "arg.history_id"
        const val ARG_HISTORY_DATE = "arg.history_date"
        const val ARG_SOURCE = "arg.source"
        const val ARG_DESTINATION = "arg.destination"
        const val ARG_SOURCE_ACCOUNT = "arg.source.account"
        const val ARG_DESTINATION_ACCOUNT = "arg.destination.account"
        const val ARG_SOURCE_PERSON = "arg.source.person"
        const val ARG_DESTINATION_PERSON = "arg.destination.person"
    }

    private var binding: HistoryInputLayoutBinding? = null
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HistoryInputLayoutBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        selectedType = requireArguments().getHistoryType(ARG_HISTORY_TYPE)!!
        prepareDateInput(binding!!)
        prepareType(selectedType, binding!!)
        prepareSourceInput(selectedType, binding!!)
        prepareDestinationInput(selectedType, binding!!)
        viewModel.getSelectionLiveData(ARG_SOURCE).observe(viewLifecycleOwner, this::onSourceSelected)
        viewModel.getSelectionLiveData(ARG_DESTINATION).observe(viewLifecycleOwner, this::onDestinationSelected)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        setActivityTitle(getString(R.string.title_input_history_create))
    }

    private fun prepareType(type: HistoryType, binding: HistoryInputLayoutBinding) {
        var text: String? = null
        var backgroundColor: ColorStateList? = null
        val resources = requireContext().resources
        when(type) {
            HistoryType.CREDIT -> {
                text = getString(R.string.label_history_type_credit)
                backgroundColor = ColorStateList.valueOf(resources.getColor(R.color.colorCredit, null))
            }
            HistoryType.DEBIT -> {
                text = getString(R.string.label_history_type_debit)
                backgroundColor = ColorStateList.valueOf(resources.getColor(R.color.colorDebit, null))
            }
            HistoryType.EXPENSE -> {
                text = getString(R.string.label_history_type_expense)
                backgroundColor = ColorStateList.valueOf(resources.getColor(R.color.colorExpense, null))
            }
            HistoryType.INCOME -> {
                text = getString(R.string.label_history_type_income)
                backgroundColor = ColorStateList.valueOf(resources.getColor(R.color.colorIncome, null))
            }
            HistoryType.TRANSFER -> {
                text = getString(R.string.label_history_type_transfer)
                backgroundColor = ColorStateList.valueOf(resources.getColor(R.color.colorTransfer, null))
            }
        }
        binding.type.text = text
        binding.type.chipBackgroundColor = backgroundColor
    }

    private fun prepareDateInput(binding: HistoryInputLayoutBinding) {
        if (requireArguments().containsKey(ARG_HISTORY_DATE)) {
            val date = requireArguments().getDate(ARG_HISTORY_DATE)!!
            setSelectedDate(date)
            binding.inputDate.isEnabled = false
        }
        else {
            val date = Date()
            setSelectedDate(date)
            binding.inputDate.setOnClickListener{ onClickInputDate(selectedDate) }
        }
    }

    private fun prepareSourceInput(type: HistoryType, binding: HistoryInputLayoutBinding) {
        var source: Any? = null
        if (type == HistoryType.CREDIT) {
            binding.labelInputSource.setText(R.string.label_history_input_source_credit)
            if (requireArguments().containsKey(ARG_SOURCE_PERSON)) {
                source = requireArguments().getPersonModel(ARG_SOURCE_PERSON)
            }
            else {
                binding.inputSource.setOnClickListener{
                    val args = Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_credit_source))
                        putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                    }
                    navController.navigate(R.id.action_history_input_to_person_chooser_list, args)
                }
            }
        }
        else if (type == HistoryType.DEBIT) {
            binding.labelInputSource.setText(R.string.label_history_input_source_debit)
            if (requireArguments().containsKey(ARG_SOURCE_ACCOUNT)) {
                source = requireArguments().getAccountModel(ARG_SOURCE_ACCOUNT)
            }
            else {
                binding.inputSource.setOnClickListener{
                    val args = Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_debit_source_account))
                        putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                    }
                    navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
                }
            }
        }
        else if (type == HistoryType.TRANSFER) {
            binding.labelInputSource.setText(R.string.label_history_input_source_transfer)
            if (requireArguments().containsKey(ARG_SOURCE_PERSON)) {
                source = requireArguments().getPersonModel(ARG_SOURCE_PERSON)
            }
            else {
                binding.inputSource.setOnClickListener{
                    val args = Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_transfer_source_account))
                        putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                    }
                    navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
                }
            }

        }
        else if (type == HistoryType.EXPENSE) {
            binding.labelInputSource.setText(R.string.label_history_input_source_expense)
            if (requireArguments().containsKey(ARG_SOURCE_PERSON)) {
                source = requireArguments().getPersonModel(ARG_SOURCE_PERSON)
            }
            else {
                binding.inputSource.setOnClickListener{
                    val args = Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_expense_source_account))
                        putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                    }
                    navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
                }
            }
        }
        source?.let {
            viewModel.setSelection(ARG_SOURCE, source)
            binding.inputSource.isEnabled = false
            binding.selectedSourceContainer.isEnabled = false
        }
    }

    private fun prepareDestinationInput(type: HistoryType, binding: HistoryInputLayoutBinding) {
        var destination: Any?  = null
        if (type == HistoryType.CREDIT) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_credit)
            if (requireArguments().containsKey(ARG_DESTINATION_ACCOUNT)) {
                destination = requireArguments().getAccountModel(ARG_DESTINATION_ACCOUNT)
            }
            else {
                binding.inputDestination.setOnClickListener{
                    val args = Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_credit_destination_account))
                        putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                    }
                    navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
                }
            }
        }
        else if (type == HistoryType.DEBIT) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_debit)
            if (requireArguments().containsKey(ARG_DESTINATION_PERSON)) {
                destination = requireArguments().getPersonModel(ARG_DESTINATION_PERSON)
            }
            else {
                binding.inputDestination.setOnClickListener{
                    val args = Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_debit_destination_person))
                        putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                    }
                    navController.navigate(R.id.action_history_input_to_person_chooser_list, args)
                }
            }
        }
        else if (type == HistoryType.TRANSFER) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_transfer)
            if (requireArguments().containsKey(ARG_DESTINATION_ACCOUNT)) {
                destination = requireArguments().getAccountModel(ARG_DESTINATION_ACCOUNT)
            }
            else {
                binding.inputDestination.setOnClickListener{
                    val args = Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_transfer_destination_account))
                        putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                    }
                    navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
                }
            }
        }
        else if (type == HistoryType.INCOME) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_income)
            if (requireArguments().containsKey(ARG_DESTINATION_ACCOUNT)) {
                destination = requireArguments().getAccountModel(ARG_DESTINATION_ACCOUNT)
            }
            else {
                binding.inputDestination.setOnClickListener{
                    val args = Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_income_destination))
                        putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                    }
                    navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
                }
            }
        }
        destination?.let {
            viewModel.setSelection(ARG_DESTINATION, destination)
            binding.selectedDestinationContainer.isEnabled = false
            binding.inputDestination.isEnabled = false
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
        binding?.inputDate?.text = date.format(HISTORY_INPUT_DATE_FORMAT)
    }

    private fun onSourceSelected(value: Any?) {
        binding?.let {
            val binding = this.binding!!
            if (value is AccountModel) {
                showSourceAccount(value, binding)
            }
            else if (value is PersonModel) {
                showSourcePerson(value, binding)
            }
        }
    }

    private fun onDestinationSelected(value: Any?) {
        binding?.let {
            val binding = this.binding!!
            if (value is AccountModel) {
                showDestinationAccount(value,binding)
            }
            else if (value is PersonModel) {
                showDestinationPerson(value,binding)
            }
        }
    }

    private fun showSourceAccount(account: AccountModel, binding: HistoryInputLayoutBinding) {
        createChip(binding.selectedSourceContainer, account, { createAccountChip(requireContext(),it) }, ARG_SOURCE)
    }

    private fun showDestinationAccount(account: AccountModel, binding: HistoryInputLayoutBinding) {
        createChip(binding.selectedDestinationContainer, account, { createAccountChip(requireContext(),it) }, ARG_DESTINATION)
    }

    private fun showSourcePerson(person: PersonModel, binding: HistoryInputLayoutBinding) {
        createChip(binding.selectedSourceContainer,person, { createPersonChip(requireContext(), it) }, ARG_SOURCE)
    }

    private fun showDestinationPerson(person: PersonModel, binding: HistoryInputLayoutBinding) {
        createChip(binding.selectedDestinationContainer, person,
            { createPersonChip(requireContext(), it) }, ARG_DESTINATION)
    }

    private fun <T> createChip(container: ViewGroup, data: T, factory: (T)->Chip, removeSelectionForKey: String) {
        container.removeAllViews()
        val chip = factory(data)
        chip.setOnClickListener {
            container.removeView(chip)
            viewModel.setSelection(removeSelectionForKey, null)
        }
        container.addView(chip)
    }
}