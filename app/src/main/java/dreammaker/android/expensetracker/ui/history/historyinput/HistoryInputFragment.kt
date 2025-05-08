package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.DatePickerDialog
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
import androidx.navigation.fragment.findNavController
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
import dreammaker.android.expensetracker.ui.util.getBackgroundColor
import dreammaker.android.expensetracker.ui.util.getColorOnBackground
import dreammaker.android.expensetracker.ui.util.getDate
import dreammaker.android.expensetracker.ui.util.getHistoryType
import dreammaker.android.expensetracker.ui.util.getLabel
import dreammaker.android.expensetracker.ui.util.hasArgument
import dreammaker.android.expensetracker.ui.util.needsSecondaryAccount
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible
import kotlinx.coroutines.flow.collectLatest
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
    private val navController: NavController by lazy { findNavController() }
    private lateinit var selectedDate: Date
    private lateinit var selectedType: HistoryType


    /////////////////////////////////////////////////////////////////
    ///                 Fragment Argument                        ///
    ///////////////////////////////////////////////////////////////

    private fun getArgAction(): String? = arguments?.getString(Constants.ARG_ACTION)

    private fun getArgId(): Long = arguments?.getLong(Constants.ARG_ID) ?: 0

    private fun getArgDate(): Date? = arguments?.getDate(ARG_HISTORY_DATE)

    private fun getArgType(): HistoryType = requireArguments().getHistoryType(ARG_HISTORY_TYPE)!!

    private fun getArgAccount(): AccountModelParcel?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountModelParcel::class.java) }

    private fun getArgGroup(): GroupModelParcel?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_GROUP, GroupModelParcel::class.java) }

    private fun isActionEdit(): Boolean = getArgAction() == Constants.ACTION_EDIT

    /////////////////////////////////////////////////////////////////
    ///                     Selections                           ///
    ///////////////////////////////////////////////////////////////

    private fun getSelectedAccountLiveData(key: String): LiveData<AccountModelParcel?>
            = navController.currentBackStackEntry?.savedStateHandle!!.getLiveData(key, null)

    private fun getSelectedAccount(key: String): AccountModel?
            = navController.currentBackStackEntry?.savedStateHandle?.get<AccountModelParcel?>(key)?.toAccountModel()

    private fun removeSelectedAccount(key: String) {
        navController.currentBackStackEntry?.savedStateHandle?.set(key,null)
    }

    private fun getSelectedGroupLiveData(): LiveData<GroupModelParcel?>
            = navController.currentBackStackEntry?.savedStateHandle!!.getLiveData(Constants.ARG_GROUP, null)

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
        if (!hasArgument(Constants.ARG_ACTION)) {
            throw IllegalStateException("'${Constants.ARG_ACTION}' is not found in arguments")
        }
        val action = getArgAction()
        if (action == Constants.ACTION_EDIT && !hasArgument(Constants.ARG_ID)) {
            throw IllegalStateException("${Constants.ARG_ID} argument not found; ${Constants.ARG_ACTION}='${Constants.ACTION_EDIT}' requires ${Constants.ARG_ID}")
        }
        if (!hasArgument(ARG_HISTORY_TYPE)) {
            throw IllegalStateException("$ARG_HISTORY_TYPE argument not found")
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
        binding.btnSave.setOnClickListener { onClickSave() }
        binding.btnCancel.setOnClickListener { onClickCancel() }
        lifecycleScope.launch {
            viewModel.resultState.collectLatest {
                    onSave(it)
                    viewModel.emptyResult()
                }
        }
        if (isActionEdit()) {
            val history = viewModel.getStoredHistory()
            if (history == null) {
                viewModel.findHistory(getArgId(),getArgType()).observe(viewLifecycleOwner, this::onHistoryLoaded)
            }
        }
        else {
            val type = getArgType()
            val history = HistoryModel(
                getArgId(),
                type,
                getArgAccount()?.id,
                null,
                getArgGroup()?.id,
                getArgAccount()?.toAccountModel(),
                null,
                getArgGroup()?.toGroupModel(),
                0f,
                getArgDate(),
                type.name
            )
            prepare(history)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /////////////////////////////////////////////////////////////////
    ///                     Event Handler                        ///
    ///////////////////////////////////////////////////////////////

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
        return if (isActionEdit()) {
            viewModel.getStoredHistory()!!.copy(
                type=type, date=date, amount=amount, note=note
            )
        }
        else {
            var srcAccountId: Long? = getSelectedAccount(ARG_SOURCE)?.id
            var destAccountId: Long? = getSelectedAccount(ARG_DESTINATION)?.id
            val groupId: Long? = getSelectedGroup()?.id ?: getArgGroup()?.id
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

    private fun onClickCancel() {
        navController.popBackStack()
    }

    /////////////////////////////////////////////////////////////////
    ///              Background Response Handlers                ///
    ///////////////////////////////////////////////////////////////

    private fun onHistoryLoaded(history: HistoryModel?) {
        if (null == history) {
            Toast.makeText(requireContext(), R.string.message_history_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return
        }
        prepare(history)
        viewModel.historyLiveData.removeObservers(viewLifecycleOwner)
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

    /////////////////////////////////////////////////////////////////
    ///                     Prepare Methods                      ///
    ///////////////////////////////////////////////////////////////

    private fun prepare(history: HistoryModel) {
        val type = history.type!!
        val date = history.date
        prepareDate(date, null == date)
        prepareType(type)
        prepareAmount(history.amount!!)
        prepareNote(history.note)
        preparePrimaryAccount(history.primaryAccount,type)
        prepareSecondaryAccount(history.secondaryAccount, type)
        prepareGroup(history.group,type)
    }

    private fun prepareDate(date: Date?, enabled: Boolean = true) {
        binding.inputDate.isEnabled = enabled
        updateDate(date ?: Date())
        if (enabled) {
            binding.inputDate.setOnClickListener { onClickInputDate(selectedDate) }
        }
    }

    private fun onClickInputDate(date: Date) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val newDate = Date(year, month, day)
            updateDate(newDate)
        }, date.year, date.month, date.dayOfMonth)
        datePicker.show()
    }

    private fun prepareType(type: HistoryType) {
        val view = binding.type
        view.chipBackgroundColor = type.getBackgroundColor(requireContext())
        view.setTextColor(type.getColorOnBackground(requireContext()))
        view.text = type.getLabel(requireContext())
        selectedType = type
    }

    private fun prepareAmount(amount: Float) {
        binding.inputAmount.setText(amount.toString())
    }

    private fun prepareNote(note: String?) {
        binding.inputNote.setText(note)
    }

    private fun preparePrimaryAccount(account: AccountModel?, type: HistoryType) {
        binding.labelSourceAccount.text = when(type) {
            HistoryType.CREDIT -> getString(R.string.label_history_input_destination_account)
            else -> getString(R.string.label_history_input_source_account)
        }
        if (null != account) {
            // primary account available, update the chip
            updatePrimaryAccountChip(account, false)
        }
        else if (!isActionEdit()) {
            // primary account not available and it is create action, observe primary account selection
            binding.inputSource.visible()
            binding.inputSource.setOnClickListener {
                navController.navigate(R.id.action_create_history_to_account_picker,Bundle().apply {
                    putString(Constants.ARG_RESULT_KEY, ARG_SOURCE)
                    putLong(Constants.ARG_INITIAL_SELECTION, getSelectedAccount(ARG_SOURCE)?.id ?: 0)
                })
            }
            getSelectedAccountLiveData(ARG_SOURCE).observe(viewLifecycleOwner) { selectedPrimary ->
                updatePrimaryAccountChip(selectedPrimary?.toAccountModel())
            }
        }
        else {
            // it is edit action but primary account not found, may be deleted, show some placeholder
            // TODO: show a placeholder account in history input for primary account
        }
    }

    private fun prepareSecondaryAccount(account: AccountModel?, type: HistoryType) {
        if (type.needsSecondaryAccount()) {
            if (null != account) {
                // secondary account available, update the chip
                updateSecondaryAccountChip(account, false)
            }
            else if (!isActionEdit()) {
                // secondary account not available and it is create action, observe secondary account selection
                binding.inputDestination.visible()
                binding.inputDestination.setOnClickListener {
                    navController.navigate(R.id.action_create_history_to_account_picker,Bundle().apply {
                        putString(Constants.ARG_RESULT_KEY, ARG_DESTINATION)
                        putLong(Constants.ARG_INITIAL_SELECTION, getSelectedAccount(ARG_DESTINATION)?.id ?: 0)
                    })
                }
                getSelectedAccountLiveData(ARG_DESTINATION).observe(viewLifecycleOwner) { selectedSecondary ->
                    updateSecondaryAccountChip(selectedSecondary?.toAccountModel())
                }
            }
            else {
                // it is edit action but primary account not found, may be deleted, show some placeholder
                // TODO: show a placeholder account in history input for primary account
            }
            binding.destinationLayout.visible()
        }
    }

    private fun prepareGroup(group: GroupModel?, type: HistoryType) {
        if (type.needsGroup()) {
            if (null != group) {
                // group is available, update the chip and don't allow group picking
                updateGroupChip(group, false)
            }
            else if (!isActionEdit()) {
                // group is not available and it is create action, observer the group selection
                binding.inputGroup.visible()
                binding.inputGroup.setOnClickListener {
                    navController.navigate(R.id.action_create_history_to_group_picker,Bundle().apply {
                        putString(Constants.ARG_RESULT_KEY, Constants.ARG_GROUP)
                        putLong(Constants.ARG_ID, getSelectedGroup()?.id ?: 0)
                    })
                }
                getSelectedGroupLiveData().observe(viewLifecycleOwner) { selectedGroup ->
                    updateGroupChip(selectedGroup?.toGroupModel())
                }
            }
            else {
                // it is edit action but group not found, may be deleted, show some placeholder
                // TODO: show a placeholder group in history input
            }
            binding.groupLayout.visible()
        }
    }

    /////////////////////////////////////////////////////////////////
    ///                     Update Methods                       ///
    ///////////////////////////////////////////////////////////////

    private fun updateDate(date: Date) {
        binding.inputDate.text = date.format(HISTORY_INPUT_DATE_FORMAT)
        selectedDate = date
    }

    private fun updatePrimaryAccountChip(account: AccountModel?, cancelable: Boolean = true) {
        val container = binding.selectedSourceContainer
        createChip(container,account,{
            createInputChip(container, it.name!!, cancelable)
        }, cancelable, { removeSelectedAccount(ARG_SOURCE)})
    }

    private fun updateSecondaryAccountChip(account: AccountModel?, cancelable: Boolean = true) {
        val container = binding.selectedDestinationContainer
        createChip(container,account,{
            createInputChip(container, it.name!!, cancelable)
        }, cancelable, { removeSelectedAccount(ARG_DESTINATION)})
    }

    private fun updateGroupChip(group: GroupModel?, cancelable: Boolean = true) {
        val container = binding.selectedGroupContainer
        createChip(container, group, {
            createInputChip(container, it.name!!, cancelable)
        }, cancelable, { removeSelectedGroup() })
    }

    private fun <T> createChip(container: ViewGroup, data: T?, chipFactory: (T)->Chip, cancelable: Boolean = true, onRemove: (()->Unit)? = null): Chip? {
        container.removeAllViews()
        data?.let {
            val chip = chipFactory.invoke(it)
            if (cancelable) {
                chip.setOnClickListener {
                    container.removeView(chip)
                    onRemove?.invoke()
                }
            }
            container.addView(chip)
            return@createChip chip
        }
        return null
    }
}