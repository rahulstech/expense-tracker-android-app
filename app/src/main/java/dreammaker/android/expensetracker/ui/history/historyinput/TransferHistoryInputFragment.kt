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
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.TransferHistoryInputLayoutBinding
import dreammaker.android.expensetracker.util.AccountModelParcel
import dreammaker.android.expensetracker.util.UIState
import dreammaker.android.expensetracker.util.createInputChip
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.isActionEdit
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class TransferHistoryInputFragment : Fragment() {

    companion object {
        private val TAG = TransferHistoryInputFragment::class.simpleName
        private const val KEY_IS_FIND_HISTORY_STARTED = "key_is_find_history_stated"
        private const val HISTORY_INPUT_DATE_FORMAT = "EEEE, dd MMMM, yyyy"
    }

    private var _binding: TransferHistoryInputLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryInputViewModel by activityViewModels()
    private val navController: NavController by lazy { findNavController() }
    private var isFindHistoryStarted: Boolean = false

    /////////////////////////////////////////////////////////////////
    ///                 Fragment Argument                        ///
    ///////////////////////////////////////////////////////////////

    private fun getArgAccount(): AccountModel?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountModelParcel::class.java)?.toAccountModel() }

    /////////////////////////////////////////////////////////////////
    ///                     Fragment Api                         ///
    ///////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setAccount(getArgAccount())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TransferHistoryInputLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSave.setOnClickListener { onClickSave() }
        binding.btnCancel.setOnClickListener { onClickCancel() }

        prepareDate()
        preparePrimaryAccount()
        prepareSecondaryAccount()

        if (isActionEdit()) {
            if (savedInstanceState?.getBoolean(KEY_IS_FIND_HISTORY_STARTED,false) == false) {
                viewModel.findHistory(getArgId(), HistoryType.TRANSFER)
                isFindHistoryStarted = true
            }
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.historyState.filterNotNull().collectLatest { state ->
                    when(state) {
                        is UIState.UISuccess -> { onHistoryLoaded(state.asData()) }
                        is UIState.UIError -> {
                            Log.e(TAG,null,state.cause)
                            QuickMessages.toastError(requireContext(),getString(R.string.message_error))
                            popBack()
                        }
                        is UIState.UILoading -> {}
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveHistoryState.collectLatest { onSave(it) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_FIND_HISTORY_STARTED,isFindHistoryStarted)
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

    private fun validateInput(history: HistoryModel): Boolean {
        binding.amountInputLayout.error = null
        binding.errorSource.visibilityGone()
        binding.errorDestination.visibilityGone()
        var hasError = false
        if (null == history.amount) {
            hasError = true
            binding.amountInputLayout.error = getString(R.string.error_invalid_history_amount_input)
        }
        if (null == history.primaryAccountId) {
            hasError = true
            binding.errorSource.visible()
        }
        if (null == history.secondaryAccountId) {
            hasError = true
            binding.errorDestination.visible()
        }
        return hasError
    }

    private fun getInputHistory(): HistoryModel {
        val id = if(isActionEdit()) viewModel.history?.id else null
        val date = viewModel.getDate()
        val amountText = binding.inputAmount.text.toString()
        val amount = if (amountText.isBlank()) 0f else amountText.toFloat()
        var note = binding.inputNote.text.toString()
        if (note.isBlank()) {
            note = HistoryType.TRANSFER.name
        }

        val srcAccount: AccountModel? = viewModel.getAccount()
        val destAccount: AccountModel? = viewModel.getAccount(false)
        return HistoryModel(
            id,HistoryType.TRANSFER, srcAccount?.id,destAccount?.id,null,null,null,null, amount,date,note
        )
    }

    private fun onClickCancel() { popBack() }

    /////////////////////////////////////////////////////////////////
    ///              Background Response Handlers                ///
    ///////////////////////////////////////////////////////////////

    private fun onHistoryLoaded(history: HistoryModel?) {
        if (null == history) {
            QuickMessages.toastError(requireContext(),getString(R.string.message_history_not_found))
            popBack()
            return
        }
        prepare(history)
    }

    private fun onSave(state: UIState) {
        Log.i(TAG,"save state ${state.javaClass.simpleName}")
        when(state) {
            is UIState.UISuccess -> {
                QuickMessages.toastSuccess(requireContext(), getString(R.string.message_success_save_history))
                popBack()
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

    private fun prepare(history: HistoryModel) {
        prepareAmount(history.amount)
        prepareNote(history.note)
        viewModel.setDate(history.date!!)
        viewModel.setAccount(history.primaryAccount)
        viewModel.setAccount(history.secondaryAccount,false)
    }

    private fun prepareDate() {
        binding.inputDate.setOnClickListener { onClickInputDate(viewModel.dateState.value) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dateState.collectLatest { date ->
                updateDate(date)
            }
        }
    }

    private fun onClickInputDate(date: Date) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val newDate = Date(year, month, day)
            updateDate(newDate)
        }, date.year, date.month, date.dayOfMonth)
        datePicker.show()
    }

    private fun prepareAmount(amount: Float?) {
        binding.inputAmount.setText(amount?.toString() ?: "")
    }

    private fun prepareNote(note: String?) {
        binding.inputNote.setText(note)
    }

    private fun preparePrimaryAccount() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.primaryAccountState.collectLatest { account ->
                updatePrimaryAccountChip(account)
            }
        }
    }

    private fun prepareSecondaryAccount() {
        binding.inputDestination.setOnClickListener {
            navController.navigate(R.id.action_input_money_transfer_to_account_picker, bundleOf(
                Constants.KEY_IS_PRIMARY to false
            ))
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.secondaryAccountState.collectLatest { account ->
                Log.i(TAG,"secondary account changed $account")
                updateSecondaryAccountChip(account)
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    ///                     Update Methods                       ///
    ///////////////////////////////////////////////////////////////

    private fun updateDate(date: Date) {
        binding.inputDate.text = date.format(HISTORY_INPUT_DATE_FORMAT)
    }

    private fun updatePrimaryAccountChip(account: AccountModel?) {
        val container = binding.selectedSourceContainer
        createChip(container,account) {
            createInputChip(container, it.name!!, false)
        }
    }

    private fun updateSecondaryAccountChip(account: AccountModel?) {
        val container = binding.selectedDestinationContainer
        createChip(container,account) {
            createInputChip(container, it.name!!, false)
        }
    }

    private fun <T> createChip(container: ViewGroup,
                               data: T?,
                               chipFactory: (T)->Chip): Chip? {
        container.removeAllViews()
        data?.let {
            val chip = chipFactory.invoke(it)
            container.addView(chip)
            return@createChip chip
        }
        return null
    }

    private fun popBack() {
        requireActivity().finish()
    }
}