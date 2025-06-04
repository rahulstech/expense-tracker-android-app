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
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.TransferHistoryInputLayoutBinding
import dreammaker.android.expensetracker.util.AccountModelParcel
import dreammaker.android.expensetracker.util.Constants
import dreammaker.android.expensetracker.util.OperationResult
import dreammaker.android.expensetracker.util.createInputChip
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.hasArgument
import dreammaker.android.expensetracker.util.setActivitySubTitle
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TransferHistoryInputFragment : Fragment() {

    companion object {
        private val TAG = TransferHistoryInputFragment::class.simpleName
        private const val HISTORY_INPUT_DATE_FORMAT = "EEEE, dd MMMM, yyyy"
        const val ARG_HISTORY_DATE = "arg.history_date"
        const val ARG_SOURCE = "arg_source"
        const val ARG_DESTINATION = "arg_destination"
    }

    private var _binding: TransferHistoryInputLayoutBinding? = null
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

    private fun getArgAccount(): AccountModelParcel?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountModelParcel::class.java) }

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

    /////////////////////////////////////////////////////////////////
    ///                     Fragment Api                         ///
    ///////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasArgument(Constants.ARG_ACTION)) {
            throw IllegalStateException("'${Constants.ARG_ACTION}' is not found in arguments")
        }
        if (isActionEdit() && !hasArgument(Constants.ARG_ID)) {
            throw IllegalStateException("${Constants.ARG_ID} argument not found; ${Constants.ARG_ACTION}='${Constants.ACTION_EDIT}' requires ${Constants.ARG_ID}")
        }
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
        lifecycleScope.launch {
            viewModel.resultState.collectLatest {
                onSave(it)
                viewModel.emptyResult()
            }
        }
        if (isActionEdit()) {
            val history = viewModel.getStoredHistory()
            if (history == null) {
                viewModel.findHistory(getArgId(),HistoryType.TRANSFER).observe(viewLifecycleOwner, this::onHistoryLoaded)
            }
        }
        else {
            val type = HistoryType.TRANSFER
            val argAccount = getArgAccount()
            val history = HistoryModel(
                getArgId(),
                type,
                argAccount?.id,
                null,
                null,
                argAccount?.toAccountModel(),
                null,
                null,
                null,
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
        val date = selectedDate
        val amount = binding.inputAmount.text.toString().toFloatOrNull()
        val note = binding.inputNote.text.toString()
        return if (isActionEdit()) {
            viewModel.getStoredHistory()!!.copy(date=date, amount=amount, note=note)
        }
        else {
            val srcAccountId: Long? = getSelectedAccount(ARG_SOURCE)?.id ?: getArgAccount()?.id
            val destAccountId: Long? = getSelectedAccount(ARG_DESTINATION)?.id
            HistoryModel(
                null,HistoryType.TRANSFER, srcAccountId,destAccountId,null,null,null,null, amount,date,note
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

    override fun onPause() {
        super.onPause()
        setActivitySubTitle("")
    }

    /////////////////////////////////////////////////////////////////
    ///                     Prepare Methods                      ///
    ///////////////////////////////////////////////////////////////

    private fun prepare(history: HistoryModel) {
        val date = history.date
        prepareDate(date, null == date)
        prepareAmount(history.amount)
        prepareNote(history.note)
        preparePrimaryAccount(history.primaryAccount)
        prepareSecondaryAccount(history.secondaryAccount)
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

    private fun prepareSecondaryAccount(account: AccountModel?) {
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