package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.HistoryInputLayoutBinding
import dreammaker.android.expensetracker.util.AccountParcel
import dreammaker.android.expensetracker.util.GroupParcel
import dreammaker.android.expensetracker.util.UIState
import dreammaker.android.expensetracker.util.createInputChip
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.isActionEdit
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionInputFragment : Fragment() {

    companion object {
        private val TAG = TransactionInputFragment::class.simpleName
        private const val KEY_IS_FIND_HISTORY_STARTED = "key_is_find_history_started"
        private val HISTORY_INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM, yyyy")
        const val ARG_HISTORY_DATE = "arg.history_date"
    }

    private var _binding: HistoryInputLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryInputViewModel by activityViewModels()
    private val navController: NavController by lazy { findNavController() }
    private var isFindHistoryStarted: Boolean = false


    /////////////////////////////////////////////////////////////////
    ///                 Fragment Argument                        ///
    ///////////////////////////////////////////////////////////////

    private fun getArgDate(): LocalDate = arguments?.getDate(ARG_HISTORY_DATE) ?: LocalDate.now()

    private fun getArgAccount(): Account?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountParcel::class.java)?.toAccount() }

    private fun getArgGroup(): Group?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_GROUP, GroupParcel::class.java)?.toGroup() }

    /////////////////////////////////////////////////////////////////
    ///                     Selections                           ///
    ///////////////////////////////////////////////////////////////

    private fun removeSelectedGroup() { viewModel.setGroup(null) }

    /////////////////////////////////////////////////////////////////
    ///                     Fragment Api                         ///
    ///////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setDate(getArgDate())
        viewModel.setAccount(getArgAccount())
        viewModel.setGroup(getArgGroup())
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

        prepareDate()
        preparePrimaryAccount()
        prepareGroup()

        if (isActionEdit()) {
            if (savedInstanceState?.getBoolean(KEY_IS_FIND_HISTORY_STARTED, false) ?: true) {
                Log.i(TAG,"loading history for id ${getArgId()}")
                viewModel.findHistory(getArgId())
                isFindHistoryStarted = true
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.historyState.filterNotNull()
                    .collectLatest { state ->
                        when(state) {
                            is UIState.UISuccess -> {
                                onHistoryLoaded(state.asData())
                            }
                            is UIState.UIError -> {
                                Log.e(TAG, null,state.cause)
                                QuickMessages.toastError(requireContext(), getString(R.string.message_error))
                                popBack()
                            }
                            else -> {}
                        }
                    }
            }
        }

        // NOTE: lifecycleScope bound to fragment lifecycle
        //      viewLifecycleOwner.lifecycleScope bound to view which starts onCreateView and ends onDestroyView
        // if i use lifecycleScope then on each recreate of view an launch code will be added i.e. multiple collectors will present
        // each of the collector will get notified for each ui state change, therefore there will be multiple unnecessary ui state handle
        // but when launch is added to viewLifecycleOwner.lifecycleScope, on each view recreate new lifecycleScope is created and lunch code
        // is added to the newer lifecycleScope and older is disabled. so do duplicate ui state handle
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveHistoryState.collectLatest { onSave(it) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_FIND_HISTORY_STARTED, isFindHistoryStarted)
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
        Log.d(TAG, "save history $history")
        if (!validateInput(history)) {
            if (isActionEdit()) {
                viewModel.setHistory(history)
            }
            else {
                viewModel.addHistory(history)
            }
        }
    }

    private fun validateInput(history: History): Boolean {
        binding.amountInputLayout.error = null
        binding.errorSource.visibilityGone()
        var hasError = false
        if (history.primaryAccountId == 0L) {
            hasError = true
            binding.errorSource.visible()
        }
        return hasError
    }

    private fun getInputHistory(): History {
        val id = when(isActionEdit()) {
            true -> getArgId()
            else -> 0L
        }
        val date = viewModel.getDate()
        val amountText = binding.inputAmount.text.toString()
        val amount = if (amountText.isBlank()) 0f else amountText.toFloat()
        val note = binding.inputNote.text.toString()
        val account: Account? = viewModel.getAccount()
        val group: Group? = viewModel.getGroup()
        return when(isCreditHistory()) {
            true -> History.CreditHistory(
                id = id,
                amount = amount,
                date = date,
                note = note,
                primaryAccountId = account?.id ?: 0,
                groupId = group?.id
            )
            else -> History.DebitHistory(
                id = id,
                amount = amount,
                date = date,
                note = note,
                primaryAccountId = account?.id ?: 0,
                groupId = group?.id
            )
        }
    }

    private fun onClickCancel() { popBack() }

    /////////////////////////////////////////////////////////////////
    ///              Background Response Handlers                ///
    ///////////////////////////////////////////////////////////////

    private fun onHistoryLoaded(history: History?) {
        Log.i(TAG,"loaded history $history")
        if (null == history) {
            QuickMessages.toastError(requireContext(), getString(R.string.message_history_not_found))
            popBack()
            return
        }
        prepare(history)
    }

    private fun onSave(state: UIState) {
        Log.i(TAG,"save state ${state.javaClass.simpleName}")
        when(state) {
            is UIState.UISuccess -> {
                if (isActionEdit()) {
                    popBack()
                }
                else {
                    QuickMessages
                        .alertSuccess(requireContext(),
                            getString(R.string.message_success_save_history_ask_add_more),
                            QuickMessages.AlertButton(getString(R.string.label_yes)){
                                reset()
                            },
                            QuickMessages.AlertButton(getString(R.string.label_no)){
                                popBack()
                            })
                        .setOnCancelListener { reset() }
                }

            }
            is UIState.UIError -> {
                Log.e(TAG,"save error",state.cause)
                QuickMessages.simpleAlertError(requireContext(),R.string.message_error_save_history)
            }
            else -> {}
        }
    }

    /////////////////////////////////////////////////////////////////
    ///                     Prepare Methods                      ///
    ///////////////////////////////////////////////////////////////

    private fun prepare(history: History) {
        selectHistoryType(history)
        prepareAmount(history.amount)
        prepareNote(history.note)
        viewModel.setDate(history.date)
        viewModel.setAccount(history.primaryAccount)
        viewModel.setGroup(history.group)
    }

    private fun prepareDate() {
        binding.inputDate.setOnClickListener { onClickInputDate(viewModel.getDate()) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dateState.collectLatest { date ->
                binding.inputDate.text = date.format(HISTORY_INPUT_DATE_FORMAT)
            }
        }
        viewModel.setDate(getArgDate())
    }

    private fun onClickInputDate(date: LocalDate) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val newDate = LocalDate.of(year,month,dayOfMonth)
            viewModel.setDate(newDate)
        }, date.year, date.monthValue, date.dayOfMonth)
        datePicker.show()
    }

    private fun isCreditHistory(): Boolean = binding.containerTypes.checkedChipId == R.id.type_credit

    private fun selectHistoryType(history: History) {
        when(history) {
            is History.CreditHistory -> binding.containerTypes.check(R.id.type_credit)
            else -> binding.containerTypes.check(R.id.type_debit)
        }
    }

    private fun selectDefaultHistoryType() {
        binding.containerTypes.check(R.id.type_debit)
    }

    private fun prepareAmount(amount: Float?) {
        binding.inputAmount.setText(amount?.toString())
    }

    private fun prepareNote(note: String? = null) {
        binding.inputNote.setText(note)
    }

    private fun preparePrimaryAccount() {
        binding.inputSource.setOnClickListener {
            navController.navigate(R.id.action_input_transaction_to_account_picker, bundleOf(
                Constants.KEY_IS_PRIMARY to true
            ))
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.primaryAccountState.collectLatest { account ->
                updatePrimaryAccountChip(account,false)
            }
        }
    }

    private fun prepareGroup() {
        binding.inputGroup.setOnClickListener {
            navController.navigate(R.id.action_create_history_to_group_picker)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.groupState.collectLatest { group ->
                updateGroupChip(group,true)
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    ///                    utility Methods                       ///
    ///////////////////////////////////////////////////////////////

    private fun updatePrimaryAccountChip(account: Account?, cancelable: Boolean = true) {
        val container = binding.selectedSourceContainer
        createChip(container,account,{
            createInputChip(container, it.name, cancelable)
        }, cancelable /*, { removeSelectedAccount() }*/)
    }

    private fun updateGroupChip(group: Group?, cancelable: Boolean = true) {
        val container = binding.selectedGroupContainer
        createChip(container, group, {
            createInputChip(container, it.name, cancelable)
        }, cancelable, { removeSelectedGroup() })
    }

    private fun <T> createChip(container: ViewGroup, data: T?,
                               chipFactory: (T)->Chip,
                               cancelable: Boolean = true,
                               onRemove: (()->Unit)? = null): Chip? {
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

    private fun popBack() {
        requireActivity().finish()
    }

    private fun reset() {
        selectDefaultHistoryType()
        binding.inputAmount.text = null
        binding.inputNote.text = null
        viewModel.setDate(getArgDate())
        viewModel.setAccount(getArgAccount())
        viewModel.setGroup(getArgGroup())
    }
}