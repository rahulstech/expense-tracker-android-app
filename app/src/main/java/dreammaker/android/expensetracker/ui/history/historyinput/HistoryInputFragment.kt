package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.chip.Chip
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.HistoryInputLayoutBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.createAccountChip
import dreammaker.android.expensetracker.ui.util.createPersonChip
import dreammaker.android.expensetracker.ui.util.disable
import dreammaker.android.expensetracker.ui.util.enable
import dreammaker.android.expensetracker.ui.util.getAccountModel
import dreammaker.android.expensetracker.ui.util.getDate
import dreammaker.android.expensetracker.ui.util.getHistoryType
import dreammaker.android.expensetracker.ui.util.getPersonModel
import dreammaker.android.expensetracker.ui.util.setActivityTitle
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class HistoryInputFragment : Fragment() {

    companion object {
        private val TAG = HistoryInputFragment::class.simpleName
        private const val HISTORY_INPUT_DATE_FORMAT = "EEEE, dd MMMM, yyyy"
        const val ARG_HISTORY_TYPE = "arg.history_type"
        const val ARG_HISTORY_DATE = "arg.history_date"
        const val ARG_SOURCE = "arg.source"
        const val ARG_DESTINATION = "arg.destination"
        const val ARG_SOURCE_ACCOUNT = "arg.source.account"
        const val ARG_DESTINATION_ACCOUNT = "arg.destination.account"
        const val ARG_SOURCE_PERSON = "arg.source.person"
        const val ARG_DESTINATION_PERSON = "arg.destination.person"
    }

    private var _binding: HistoryInputLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HistoryInputViewModel
    private lateinit var navController: NavController
    private lateinit var selectedDate: Date
    private lateinit var selectedType: HistoryType

    private val observer = Observer<HistoryModel?> { onHistoryLoaded(it) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireParentFragment(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[HistoryInputViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireArguments().containsKey(Constants.ARG_ACTION)) {
            throw IllegalStateException("'${Constants.ARG_ACTION}' is not found in arguments")
        }
        val action = requireArguments().getString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
        if (action == Constants.ACTION_EDIT && !requireArguments().containsKey(Constants.ARG_ID)) {
            throw IllegalStateException("when ${Constants.ARG_ACTION}='${Constants.ACTION_EDIT}' then '${Constants.ARG_ID}' is required")
        }
        else if (!requireArguments().containsKey(ARG_HISTORY_TYPE)) {
            throw IllegalStateException("when ${Constants.ARG_ACTION}='${Constants.ACTION_EDIT}' then $ARG_HISTORY_TYPE is required")
        }
    }

    private fun getArgAction(): String = arguments?.getString(Constants.ARG_ACTION) ?: ""

    private fun isActionEdit(): Boolean = getArgAction() == Constants.ACTION_EDIT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HistoryInputLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)

        selectedType = requireArguments().getHistoryType(ARG_HISTORY_TYPE)!!
        prepareDateInput(binding)
        prepareType(selectedType, binding)
        prepareSourceInput(selectedType, binding)
        prepareDestinationInput(selectedType, binding)
        binding.btnSave.setOnClickListener { onClickSave() }
        binding.btnCancel.setOnClickListener { onClickCancel() }
        viewModel.getSelectionLiveData(ARG_SOURCE).observe(viewLifecycleOwner, this::onSourceSelected)
        viewModel.getSelectionLiveData(ARG_DESTINATION).observe(viewLifecycleOwner, this::onDestinationSelected)
        if (isActionEdit()) {
            if (viewModel.getStoredHistory() == null) {
                val id = requireArguments().getLong(Constants.ARG_ID)
                viewModel.findHistory(id, selectedType).observe(viewLifecycleOwner, observer)
            }
        }
        lifecycleScope.launch {
            viewModel.resultState
                .filterNotNull()
                .collect {
                    onSave(it)
                    viewModel.emptyResult()
                }
        }
    }

    private fun onHistoryLoaded(history: HistoryModel?) {
        if (null == history) {
            Toast.makeText(requireContext(), R.string.message_history_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return
        }
        else {
            binding.inputAmount.setText(history.amount!!.toString())
            binding.inputNote.setText(history.note)
            setSelectedDate(history.date!!)
            val type = history.type!!
            val src: Any? = when {
                type.needsSourceAccount() -> history.srcAccount
                type.needsSourcePerson() -> history.srcPerson
                else -> null
            }
            val dest: Any? = when {
                type.needsDestinationAccount() -> history.destAccount
                type.needsDestinationPerson() -> history.destPerson
                else -> null
            }
            onSourceSelected(src)
            onDestinationSelected(dest)
            binding.inputDate.disable()
            binding.inputSource.disable()
            binding.selectedSourceContainer.children.firstOrNull()?.disable()
            binding.inputDestination.disable()
            binding.selectedDestinationContainer.children.firstOrNull()?.disable()
            viewModel.history.removeObserver(observer)
        }
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
            binding.inputDate.enable()
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

    private fun onClickSave() {
        val history = getInputHistory()
        if (!validateInput(history)) {
            if (isActionEdit()) {
                viewModel.setHistory(history)
            }
            else {
                viewModel.addHistory(history)
            }
        }
    }

    private fun validateInput(history: HistoryModel): Boolean {
        val type = history.type!!
        binding.amountInputLayout.error = null
        binding.errorSource.visibilityGone()
        binding.errorDestination.visibilityGone()
        var hasError = false
        if (null == history.amount) {
            hasError = true
            binding.amountInputLayout.error = getString(R.string.error_invalid_history_amount_input)
        }
        if ((type.needsSourceAccount() && null == history.srcAccountId) && (type.needsSourcePerson() && null == history.srcPersonId)) {
            hasError = true
            binding.errorSource.visible()
        }
        if ((type.needsDestinationAccount() && null == history.destAccountId) && (type.needsDestinationPerson() && null == history.destPerson)) {
            hasError = true
            binding.errorDestination.visible()
        }
        return hasError
    }

    private fun getInputHistory(): HistoryModel {
        val type = selectedType
        val date = selectedDate
        val amount = binding.inputAmount.text.toString().toFloatOrNull()
        val note = binding.inputNote.text.toString()
        return if (isActionEdit()) {
            viewModel.getStoredHistory()!!.copy(
                type=type, date=date, amount=amount, note=note
            )
        }
        else {
            val src = getSelectedSource()
            val dest = getSelectedDestination()
            val srcAccountId: Long? = if (src is AccountModel) src.id else null
            val destAccountId: Long? = if (dest is AccountModel) dest.id else null
            val srcPersonId: Long? = if (src is PersonModel) src.id else null
            val destPersonId: Long? = if (dest is PersonModel) dest.id else null
            HistoryModel(
                null,type,
                srcAccountId,destAccountId,srcPersonId,destPersonId,null,null,null,null,
                amount,date,note
            )
        }
    }

    private fun getSelectedSource(): Any? = viewModel.getSelection(ARG_SOURCE)

    private fun getSelectedDestination(): Any? = viewModel.getSelection(ARG_DESTINATION)

    private fun onSave(result: OperationResult<HistoryModel>?) {
        result?.let {
            if (result.isFailure()) {
                Log.e(TAG, "onSave: action=${getArgAction()}", result.error)
                if (isActionEdit()) {
                    Toast.makeText(requireContext(), R.string.message_fail_edit_history, Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(requireContext(), R.string.message_fail_create_history, Toast.LENGTH_LONG).show()
                }
            }
            else {
                if (isActionEdit()) {
                    Toast.makeText(requireContext(), R.string.message_success_edit_history, Toast.LENGTH_LONG).show()
                    popBackStack()
                }
                else {
                    Toast.makeText(requireContext(), R.string.message_success_create_history, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onClickCancel() {
        popBackStack()
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

    private fun onSourceSelected(value: Any?) {
        if (value is AccountModel) {
            showSourceAccount(value, binding)
        }
        else if (value is PersonModel) {
            showSourcePerson(value, binding)
        }
    }

    private fun onDestinationSelected(value: Any?) {
        if (value is AccountModel) {
            showDestinationAccount(value,binding)
        }
        else if (value is PersonModel) {
            showDestinationPerson(value,binding)
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

    override fun onResume() {
        super.onResume()
        setActivityTitle(getString(
            if (isActionEdit()) R.string.title_input_history_edit
            else R.string.title_input_history_create)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun popBackStack() {
        val parentNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_container)
        parentNavController.popBackStack()
    }
}