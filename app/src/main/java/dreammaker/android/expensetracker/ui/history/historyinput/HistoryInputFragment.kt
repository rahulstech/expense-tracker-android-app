package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.chip.Chip
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HistoryInputLayoutBinding
import dreammaker.android.expensetracker.ui.util.AccountModelParcel
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.createAccountChip
import dreammaker.android.expensetracker.ui.util.disable
import dreammaker.android.expensetracker.ui.util.enable
import dreammaker.android.expensetracker.ui.util.getDate
import dreammaker.android.expensetracker.ui.util.getHistoryType
import dreammaker.android.expensetracker.ui.util.hasArgument
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class HistoryInputFragment : Fragment() {

    companion object {
        private val TAG = HistoryInputFragment::class.simpleName
        private const val HISTORY_INPUT_DATE_FORMAT = "EEEE, dd MMMM, yyyy"
        private const val KEY_SELECTED_GROUP = "key_selected_group"
        const val ARG_HISTORY_TYPE = "arg.history_type"
        const val ARG_HISTORY_DATE = "arg.history_date"
        const val ARG_SOURCE = "arg.source"
        const val ARG_DESTINATION = "arg.destination"
        const val ARG_GROUP = "arg.group"
    }

    private var _binding: HistoryInputLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupPickerAdapter: GroupPickerAdapter
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
        if (!requireArguments().containsKey(ARG_HISTORY_TYPE)) {
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
        prepareDateInput()
        prepareType(selectedType)
        prepareSourceInput(selectedType)
        prepareDestinationInput(selectedType)
        prepareGroupInput(selectedType)
        binding.btnSave.setOnClickListener { onClickSave() }
        binding.btnCancel.setOnClickListener { onClickCancel() }

        getSelectedAccountLiveData(ARG_SOURCE)?.observe(viewLifecycleOwner) { showSourceAccount(it?.toAccountModel()) }
        getSelectedAccountLiveData(ARG_DESTINATION)?.observe(viewLifecycleOwner){ showDestinationAccount(it?.toAccountModel())}
        lifecycleScope.launch {
            viewModel.resultState
                .filterNotNull()
                .collect {
                    onSave(it)
                    viewModel.emptyResult()
                }
        }
    }

    private fun getSelectedAccountLiveData(key: String): LiveData<AccountModelParcel?>?
    = navController.currentBackStackEntry?.savedStateHandle?.getLiveData(key, null)

    private fun getSelectedAccount(key: String): AccountModel?
    = navController.currentBackStackEntry?.savedStateHandle?.getLiveData<AccountModelParcel?>(key, null)?.value?.toAccountModel()

    private fun removeSelectedAccount(key: String) {
        navController.currentBackStackEntry?.savedStateHandle?.set(key,null)
    }

    private fun getSelectedGroup(): GroupModel? {
        val group = binding.groupInput.selectedItem as GroupModel
        if (group == NoGroup) {
            return null
        }
        return group
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
            showSourceAccount(history.srcAccount)
            showDestinationAccount(history.destAccount)
            binding.inputDate.disable()
            binding.inputSource.disable()
            binding.selectedSourceContainer.children.firstOrNull()?.disable()
            binding.inputDestination.disable()
            binding.selectedDestinationContainer.children.firstOrNull()?.disable()

            val type = history.type!!
            if (type.needsSourceAccount()) {
                binding.sourceLayout.visible()
            }
            if (type.needsDestinationAccount()) {
                binding.destinationLayout.visible()
            }
            history.groupId?.let { setSelectedGroup(it) }

            viewModel.history.removeObserver(observer)
        }
    }

    private fun prepareType(type: HistoryType) {
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
            HistoryType.TRANSFER -> {
                text = getString(R.string.label_history_type_transfer)
                backgroundColor = ColorStateList.valueOf(resources.getColor(R.color.colorTransfer, null))
            }
        }
        binding.type.text = text
        binding.type.chipBackgroundColor = backgroundColor
    }

    private fun prepareDateInput() {
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

    private fun prepareSourceInput(type: HistoryType) {
        if (arguments?.containsKey(ARG_SOURCE) == true) {
            val source = requireArguments().getParcelable<AccountModelParcel?>(ARG_SOURCE)?.toAccountModel()
            source?.let {
                showSourceAccount(source, true)
                binding.inputSource.disable()
                binding.sourceLayout.visible()
            }
        }
        else if (type.needsSourceAccount()) {
            binding.inputSource.setOnClickListener{
                val args = Bundle().apply {
                    putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_source_account))
                    putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                    getSelectedAccount(ARG_SOURCE)?.let { putLong(Constants.ARG_INITIAL_SELECTION, it.id!!) }
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
            binding.sourceLayout.visible()
        }
    }

    private fun prepareDestinationInput(type: HistoryType) {
        if (arguments?.containsKey(ARG_DESTINATION) == true) {
            val destination = requireArguments().getParcelable<AccountModelParcel?>(ARG_DESTINATION)?.toAccountModel()
            destination?.let {
                showDestinationAccount(destination, true)
                binding.inputDestination.disable()
                binding.destinationLayout.visible()
            }
        }
        else if (type.needsDestinationAccount()) {
            binding.inputDestination.setOnClickListener{
                val args = Bundle().apply {
                    putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_destination_account))
                    putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                    getSelectedAccount(ARG_DESTINATION)?.let { putLong(Constants.ARG_INITIAL_SELECTION, it.id!!) }
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
            binding.destinationLayout.visible()
        }
    }

    private fun prepareGroupInput(type: HistoryType) {
        groupPickerAdapter = GroupPickerAdapter(requireContext())
        binding.groupInput.adapter = groupPickerAdapter

        if (type != HistoryType.TRANSFER) {
            viewModel.getAllGroups().observe(viewLifecycleOwner) { groups ->
                groupPickerAdapter.submitList(groups)
                // TODO: restore selection position
                if (hasArgument(ARG_GROUP)) {
                    val groupId = requireArguments().getLong(ARG_GROUP)
                    setSelectedGroup(groupId)
                }
            }
            binding.groupInput.onItemSelectedListener = object : OnItemSelectedListener  {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    navController.currentBackStackEntry?.savedStateHandle?.set(KEY_SELECTED_GROUP,position)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    navController.currentBackStackEntry?.savedStateHandle?.set(KEY_SELECTED_GROUP,null)
                }
            }
            binding.groupLayout.visible()
        }
    }

    private fun setSelectedGroup(groupId: Long) {
        val selectedPosition = groupPickerAdapter.groups.indexOfFirst { it.id == groupId }
        Log.d(TAG, "setSelectedGroup: groupId=$groupId selectedPosition=$selectedPosition groups-adapter-size=${groupPickerAdapter.groups.size}")
        if (selectedPosition >= 0) {
            binding.groupInput.setSelection(selectedPosition)
            binding.groupInput.disable()
        }
    }

    private fun onClickSave() {
        val history = getInputHistory()
        Log.d(TAG, "onClickSave: input-history=$history")
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
        if ((type.needsSourceAccount() && null == history.srcAccountId)) {
            hasError = true
            binding.errorSource.visible()
        }
        if ((type.needsDestinationAccount() && null == history.destAccountId)) {
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
        val groupId: Long? = getSelectedGroup()?.id
        return if (isActionEdit()) {
            viewModel.getStoredHistory()!!.copy(
                type=type, date=date, amount=amount, note=note, groupId=groupId
            )
        }
        else {
            val srcAccountId: Long? = getSelectedAccount(ARG_SOURCE)?.id
            val destAccountId: Long? = getSelectedAccount(ARG_DESTINATION)?.id
            HistoryModel(
                null,type, srcAccountId,destAccountId,groupId,null,null,null, amount,date,note
            )
        }
    }

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

    private fun showSourceAccount(account: AccountModel?, disabled: Boolean = false) {
        createChip(binding.selectedSourceContainer, account, { removeSelectedAccount(ARG_SOURCE) }, disabled)
    }

    private fun showDestinationAccount(account: AccountModel?, disabled: Boolean = false) {
        createChip(binding.selectedDestinationContainer, account, { removeSelectedAccount(ARG_DESTINATION) }, disabled)
    }

    private fun createChip(container: ViewGroup, data: AccountModel?, onRemove: ()->Unit, disabled: Boolean): Chip? {
        container.removeAllViews()
        data?.let {
            val chip = createAccountChip(requireContext(),data)
            chip.setOnClickListener {
                container.removeView(chip)
                onRemove()
            }
            chip.isEnabled = !disabled
            container.addView(chip)
            return@createChip chip
        }
        return null
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