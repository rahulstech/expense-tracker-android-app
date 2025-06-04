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
import dreammaker.android.expensetracker.util.AccountModelParcel
import dreammaker.android.expensetracker.util.Constants
import dreammaker.android.expensetracker.util.GroupModelParcel
import dreammaker.android.expensetracker.util.OperationResult
import dreammaker.android.expensetracker.util.createInputChip
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.getHistoryType
import dreammaker.android.expensetracker.util.hasArgument
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryInputFragment : Fragment() {

    companion object {
        private val TAG = HistoryInputFragment::class.simpleName
        private const val HISTORY_INPUT_DATE_FORMAT = "EEEE, dd MMMM, yyyy"
        const val ARG_HISTORY_TYPE = "arg.history_type"
        const val ARG_HISTORY_DATE = "arg.history_date"
        const val ARG_SOURCE = "arg_source"
    }

    private var _binding: HistoryInputLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryInputViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private lateinit var selectedDate: Date


    /////////////////////////////////////////////////////////////////
    ///                 Fragment Argument                        ///
    ///////////////////////////////////////////////////////////////

    private fun getArgAction(): String? = arguments?.getString(Constants.ARG_ACTION)

    private fun getArgId(): Long = arguments?.getLong(Constants.ARG_ID) ?: 0

    private fun getArgDate(): Date? = arguments?.getDate(ARG_HISTORY_DATE)

    private fun getArgType(): HistoryType = arguments?.getHistoryType(ARG_HISTORY_TYPE)!!

    private fun getArgAccount(): AccountModelParcel?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountModelParcel::class.java) }

    private fun getArgGroup(): GroupModelParcel?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_GROUP, GroupModelParcel::class.java) }

    private fun isActionEdit(): Boolean = getArgAction() == Constants.ACTION_EDIT

    /////////////////////////////////////////////////////////////////
    ///                     Selections                           ///
    ///////////////////////////////////////////////////////////////

    private fun getSelectedAccountLiveData(): LiveData<AccountModelParcel?>
            = navController.currentBackStackEntry?.savedStateHandle!!.getLiveData(ARG_SOURCE, null)

    private fun getSelectedAccount(): AccountModel?
            = navController.currentBackStackEntry?.savedStateHandle?.get<AccountModelParcel?>(ARG_SOURCE)?.toAccountModel()

    private fun removeSelectedAccount() {
        navController.currentBackStackEntry?.savedStateHandle?.set(ARG_SOURCE,null)
    }

    private fun getSelectedGroupLiveData(): LiveData<GroupModelParcel?>
            = navController.currentBackStackEntry?.savedStateHandle!!.getLiveData(Constants.ARG_GROUP, null)

    private fun getSelectedGroup(): GroupModel?
            = navController.currentBackStackEntry?.savedStateHandle?.get<GroupModelParcel?>(
        Constants.ARG_GROUP)?.toGroupModel()

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
        if (isActionEdit()) {
            if (!hasArgument(Constants.ARG_ID)) {
                throw IllegalStateException("${Constants.ARG_ID} argument not found; ${Constants.ACTION_EDIT} requires ${Constants.ARG_ID}")
            }
            if (!hasArgument(ARG_HISTORY_TYPE)) {
                throw IllegalStateException("$ARG_HISTORY_TYPE argument not found; ${Constants.ACTION_EDIT} requires $ARG_HISTORY_TYPE")
            }
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
            val argAccount = getArgAccount()
            val argGroup = getArgGroup()
            val history = HistoryModel(
                null,
                null,
                argAccount?.id,
                null,
                argGroup?.id,
                argAccount?.toAccountModel(),
                null,
                argGroup?.toGroupModel(),
                null,
                getArgDate(),
                null
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
        var hasError = false
        if (null == history.amount) {
            hasError = true
            binding.amountInputLayout.error = getString(R.string.error_invalid_history_amount_input)
        }
        if ((type.needsSourceAccount() && null == history.primaryAccountId)) {
            hasError = true
            binding.errorSource.visible()
        }
        return hasError
    }

    private fun getInputHistory(): HistoryModel {
        val type = getSelectedHistoryType() /*selectedType*/
        val date = selectedDate
        val amount = binding.inputAmount.text.toString().toFloatOrNull()
        val note = binding.inputNote.text.toString()
        return if (isActionEdit()) {
            viewModel.getStoredHistory()!!.copy(
                type=type, date=date, amount=amount, note=note
            )
        }
        else {
            var srcAccountId: Long? = getSelectedAccount()?.id
            val groupId: Long? = getSelectedGroup()?.id ?: getArgGroup()?.id
            if (null == srcAccountId && type.needsSourceAccount() && hasArgument(Constants.ARG_ACCOUNT)) {
                srcAccountId = getArgAccount()?.id
            }
            HistoryModel(
                null,type, srcAccountId,null,groupId,null,null,null, amount,date,note
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
                Toast.makeText(requireContext(), R.string.message_history_not_saved, Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(requireContext(), R.string.message_history_saved, Toast.LENGTH_LONG).show()
                if (isActionEdit()) {
                    navController.popBackStack()
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    ///                     Prepare Methods                      ///
    ///////////////////////////////////////////////////////////////

    private fun prepare(history: HistoryModel) {
        val type = history.type ?: HistoryType.DEBIT
        val date = history.date ?: Date()
        prepareDate(date)
        prepareType(type)
        prepareAmount(history.amount)
        prepareNote(history.note ?: type.name)
        preparePrimaryAccount(history.primaryAccount)
        prepareGroup(history.group)
    }

    private fun prepareDate(date: Date) {
        updateDate(date)
        binding.inputDate.setOnClickListener { onClickInputDate(selectedDate) }
    }

    private fun onClickInputDate(date: Date) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val newDate = Date(year, month, day)
            updateDate(newDate)
        }, date.year, date.month, date.dayOfMonth)
        datePicker.show()
    }

    private fun prepareType(type: HistoryType) {
        if (type == HistoryType.CREDIT) {
            binding.containerTypes.check(R.id.type_credit)
        }
        else {
            binding.containerTypes.check(R.id.type_debit)
        }
        if (isActionEdit()) {
            if (type == HistoryType.CREDIT) {
                binding.typeDebit.visibilityGone()
            }
            else {
                binding.typeCredit.visibilityGone()
            }
        }
        else {
            binding.containerTypes.setOnCheckedStateChangeListener { _,_ ->
                val note = binding.inputNote.text.toString()
                val selectedType = getSelectedHistoryType()
                if (note == HistoryType.CREDIT.name || note == HistoryType.DEBIT.name) {
                    binding.inputNote.setText(selectedType.name)
                }
            }
        }
    }

    private fun getSelectedHistoryType(): HistoryType {
        val checkedId = binding.containerTypes.checkedChipId
        return if (checkedId == R.id.type_credit) {
            HistoryType.CREDIT
        }
        else {
            HistoryType.DEBIT
        }
    }

    private fun prepareAmount(amount: Float?) {
        binding.inputAmount.setText(amount?.toString() ?: "")
    }

    private fun prepareNote(note: String?) {
        binding.inputNote.setText(note)
    }

    private fun preparePrimaryAccount(account: AccountModel?) {
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
                    putLong(Constants.ARG_INITIAL_SELECTION, getSelectedAccount()?.id ?: 0)
                })
            }
            getSelectedAccountLiveData().observe(viewLifecycleOwner) { selectedPrimary ->
                updatePrimaryAccountChip(selectedPrimary?.toAccountModel())
            }
        }
        else {
            // it is edit action but primary account not found, may be deleted, show some placeholder
            // TODO: show a placeholder account in history input for primary account
        }
    }

    private fun prepareGroup(group: GroupModel?) {
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
                    putLong(Constants.ARG_INITIAL_SELECTION, getSelectedGroup()?.id ?: 0)
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
        }, cancelable, { removeSelectedAccount()})
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