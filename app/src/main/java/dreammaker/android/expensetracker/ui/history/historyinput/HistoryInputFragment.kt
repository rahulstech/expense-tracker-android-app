package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
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
import dreammaker.android.expensetracker.ui.util.GroupModelParcel
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.createInputChip
import dreammaker.android.expensetracker.ui.util.disable
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
        const val ARG_HISTORY_TYPE = "arg.history_type"
        const val ARG_HISTORY_DATE = "arg.history_date"
        const val ARG_SOURCE = "arg_source"
        const val ARG_DESTINATION = "arg_destination"
    }

    private var _binding: HistoryInputLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryInputViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var selectedDate: Date
    private lateinit var selectedType: HistoryType


    /////////////////////////////////////////////////////////////////
    ///                 Fragment Argument                        ///
    ///////////////////////////////////////////////////////////////

    private fun getArgAction(): String = arguments?.getString(Constants.ARG_ACTION) ?: ""

    private fun getArgAccount(): AccountModelParcel?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountModelParcel::class.java) }

    private fun getArgGroup(): GroupModelParcel?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_GROUP, GroupModelParcel::class.java) }

    private fun isActionEdit(): Boolean = getArgAction() == Constants.ACTION_EDIT


    /////////////////////////////////////////////////////////////////
    ///                     Selections                           ///
    ///////////////////////////////////////////////////////////////

    private fun getSelectedAccountLiveData(key: String): LiveData<AccountModelParcel?>?
            = navController.currentBackStackEntry?.savedStateHandle?.getLiveData(key, null)

    private fun getSelectedAccount(key: String): AccountModel?
            = navController.currentBackStackEntry?.savedStateHandle?.get<AccountModelParcel?>(key)?.toAccountModel()

    private fun removeSelectedAccount(key: String) {
        navController.currentBackStackEntry?.savedStateHandle?.set(key,null)
    }

    private fun getSelectedGroupLiveData(): LiveData<GroupModelParcel?>?
            = navController.currentBackStackEntry?.savedStateHandle?.getLiveData(Constants.ARG_GROUP, null)

    private fun getSelectedGroup(): GroupModel?
            = navController.currentBackStackEntry?.savedStateHandle?.get<GroupModelParcel?>(Constants.ARG_GROUP)?.toGroupModel()

    private fun removeSelectedGroup() {
        navController.currentBackStackEntry?.savedStateHandle?.set(Constants.ARG_GROUP,null)
    }

    /////////////////////////////////////////////////////////////////
    ///                     Fragment Api                         ///
    ///////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireArguments().containsKey(Constants.ARG_ACTION)) {
            throw IllegalStateException("'${Constants.ARG_ACTION}' is not found in arguments")
        }
        val action = requireArguments().getString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
        if (action == Constants.ACTION_EDIT && !hasArgument(Constants.ARG_ID)) {
            throw IllegalStateException("when ${Constants.ARG_ACTION}='${Constants.ACTION_EDIT}' then '${Constants.ARG_ID}' is required")
        }
        if (!hasArgument(ARG_HISTORY_TYPE)) {
            throw IllegalStateException("when ${Constants.ARG_ACTION}='${Constants.ACTION_EDIT}' then $ARG_HISTORY_TYPE is required")
        }
    }

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
        lifecycleScope.launch {
            viewModel.resultState
                .filterNotNull()
                .collect {
                    onSave(it)
                    viewModel.emptyResult()
                }
        }
        if (isActionEdit()) {
            val history = viewModel.getStoredHistory()
            if (history == null) {
                val id = requireArguments().getLong(Constants.ARG_ID)
                val type = requireArguments().getHistoryType(ARG_HISTORY_TYPE)!!
                viewModel.findHistory(id,type).observe(viewLifecycleOwner, this::onHistoryLoaded)
            }
        }
    }

    private fun onHistoryLoaded(history: HistoryModel?) {
        Log.d(TAG, "onHistoryLoaded history=$history")
        if (null == history) {
            Toast.makeText(requireContext(), R.string.message_history_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return
        }
        else {
            binding.inputAmount.setText(history.amount!!.toString())
            binding.inputNote.setText(history.note)
            setSelectedDate(history.date!!)
            binding.inputDate.disable()
            binding.inputSource.disable()
            binding.inputDestination.disable()
            val type = history.type!!
            if (type.needsSourceAccount()) {
                showSourceAccount(history.primaryAccount,false)
                binding.sourceLayout.visible()
            }
            if (type.needsDestinationAccount()) {
                showDestinationAccount(history.secondaryAccount, false)
                binding.destinationLayout.visible()
            }
            if (type.needsGroup()) {
                showGroup(history.group, false)
                binding.selectedGroupContainer.visible()
            }
            viewModel.historyLiveData.removeObservers(viewLifecycleOwner)
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
        val history = viewModel.getStoredHistory()
        if (null != history) {
            setSelectedDate(history.date!!)
            binding.inputDate.disable()
        }
        else if (hasArgument(ARG_HISTORY_DATE)) {
            val date = requireArguments().getDate(ARG_HISTORY_DATE)!!
            setSelectedDate(date)
            binding.inputDate.disable()
        }
        else {
            val date = Date()
            setSelectedDate(date)
            binding.inputDate.setOnClickListener{ onClickInputDate(selectedDate) }
        }
    }

    private fun prepareSourceInput(type: HistoryType) {
        if (type.needsSourceAccount()) {
            val history = viewModel.getStoredHistory()
            if (null != history) {
                showSourceAccount(history.primaryAccount,false)
                binding.inputSource.disable()
            }
            else if (hasArgument(Constants.ARG_ACCOUNT)) {
                val source = getArgAccount()?.toAccountModel()
                source?.let {
                    showSourceAccount(source, false)
                    binding.inputSource.disable()
                }
            }
            else {
                getSelectedAccountLiveData(ARG_SOURCE)?.observe(viewLifecycleOwner) { showSourceAccount(it?.toAccountModel()) }
                binding.inputSource.setOnClickListener{
                    val args = Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_source_account))
                        putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                        getSelectedAccount(ARG_SOURCE)?.let { putLong(Constants.ARG_INITIAL_SELECTION, it.id!!) }
                    }
                    navController.navigate(R.id.action_create_history_to_account_picker, args)
                }
            }
            binding.sourceLayout.visible()
        }
    }

    private fun prepareDestinationInput(type: HistoryType) {
        if (type.needsDestinationAccount()) {
            val history = viewModel.getStoredHistory()
            if (null != history) {
                showDestinationAccount(history.secondaryAccount, false)
                binding.inputDestination.disable()
            }
//            else if (hasArgument(Constants.ARG_ACCOUNT) && type == HistoryType.CREDIT) {
//                val destination = getArgAccount()?.toAccountModel()
//                destination?.let {
//                    showDestinationAccount(destination, false)
//                    binding.inputDestination.disable()
//                }
//            }
            else {
                getSelectedAccountLiveData(ARG_DESTINATION)?.observe(viewLifecycleOwner){ showDestinationAccount(it?.toAccountModel())}
                binding.inputDestination.setOnClickListener{
                    navController.navigate(R.id.action_create_history_to_account_picker, Bundle().apply {
                        putString(Constants.ARG_DESTINATION_LABEL, getString(R.string.title_choose_destination_account))
                        putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                        getSelectedAccount(ARG_DESTINATION)?.let { putLong(Constants.ARG_INITIAL_SELECTION, it.id!!) }
                    })
                }
            }
            binding.destinationLayout.visible()
        }
    }

    private fun prepareGroupInput(type: HistoryType) {
        if (type.needsGroup()) {
            val history = viewModel.getStoredHistory()
            if (null != history) {
                showGroup(history.group, false)
                binding.inputGroup.disable()
            }
            else if (hasArgument(Constants.ARG_GROUP)) {
                val group = getArgGroup()?.toGroupModel()
                group?.let {
                    showGroup(it, false)
                    binding.inputGroup.disable()
                }
            }
            else {
                getSelectedGroupLiveData()?.observe(viewLifecycleOwner) { showGroup(it?.toGroupModel())}
                binding.inputGroup.setOnClickListener {
                    navController.navigate(R.id.action_create_history_to_group_picker, Bundle().apply {
                        putString(Constants.ARG_RESULT_KEY, Constants.ARG_GROUP)
                        getSelectedGroup()?.let { putLong(Constants.ARG_INITIAL_SELECTION, it.id!!) }
                    })
                }
            }
            binding.groupLayout.visible()
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
        if ((type.needsSourceAccount() && null == history.primaryAccountId)) {
            hasError = true
            binding.errorSource.visible()
        }
        if ((type.needsDestinationAccount() && null == history.secondaryAccountId)) {
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
        val groupId: Long? = getSelectedGroup()?.id ?: getArgGroup()?.id
        return if (isActionEdit()) {
            viewModel.getStoredHistory()!!.copy(
                type=type, date=date, amount=amount, note=note, groupId=groupId
            )
        }
        else {
            var srcAccountId: Long? = getSelectedAccount(ARG_SOURCE)?.id
            var destAccountId: Long? = getSelectedAccount(ARG_DESTINATION)?.id
            if (null == srcAccountId && type.needsSourceAccount() && hasArgument(Constants.ARG_ACCOUNT)) {
                srcAccountId = getArgAccount()?.id
            }
            if (null == destAccountId && type == HistoryType.CREDIT && hasArgument(Constants.ARG_ACCOUNT)) {
                destAccountId = getArgAccount()?.id
            }
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
                    navController.popBackStack()
                }
                else {
                    Toast.makeText(requireContext(), R.string.message_success_create_history, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onClickCancel() {
        navController.popBackStack()
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

    private fun showSourceAccount(account: AccountModel?, enabled: Boolean = true) {
        createChip(binding.selectedSourceContainer, account,
            { createInputChip(requireContext(), it.name!!, true) }, { removeSelectedAccount(ARG_SOURCE) }, enabled)
    }

    private fun showDestinationAccount(account: AccountModel?, enabled: Boolean = true) {
        createChip(binding.selectedDestinationContainer, account,
            { createInputChip(requireContext(), it.name!!, true) }, { removeSelectedAccount(ARG_DESTINATION) }, enabled)
    }

    private fun showGroup(group: GroupModel?, enabled: Boolean = true) {
        createChip(binding.selectedGroupContainer, group,
            { createInputChip(requireContext(), it.name!!, true) }, { removeSelectedGroup() }, enabled)
    }

    private fun <T> createChip(container: ViewGroup, data: T?, chipFactory: (T)->Chip, onRemove: ()->Unit, enabled: Boolean): Chip? {
        Log.d(TAG, "createChip data=$data enabled=$enabled")
        container.removeAllViews()
        data?.let {
            val chip = chipFactory.invoke(it)
            chip.setOnClickListener {
                container.removeView(chip)
                onRemove()
            }
            chip.isEnabled = enabled
            container.addView(chip)
            return@createChip chip
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}