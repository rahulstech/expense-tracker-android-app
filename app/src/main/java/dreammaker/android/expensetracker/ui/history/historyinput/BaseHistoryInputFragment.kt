package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.DATE_WITH_WEAKDAY_FORMAT
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.ui.UIState
import dreammaker.android.expensetracker.ui.history.historyinput.TransactionInputFragment.Companion.ARG_HISTORY_DATE
import dreammaker.android.expensetracker.util.AccountParcelable
import dreammaker.android.expensetracker.util.AppLocalCache
import dreammaker.android.expensetracker.util.getArgAction
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.isActionEdit
import dreammaker.android.expensetracker.util.setActivityTitle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

abstract class BaseHistoryInputFragment : Fragment() {

    companion object {
        private val TAG = BaseHistoryInputFragment::class.simpleName
    }

    protected val viewModel: HistoryInputViewModel by activityViewModels()
    protected val navController: NavController by lazy { findNavController() }
    protected val cache: AppLocalCache by lazy { AppLocalCache(requireContext()) }

    protected abstract val dateTextView: TextView
    protected abstract val amountInput: EditText
    protected abstract val noteInput: EditText
    protected abstract val btnSave: View
    protected abstract val btnCancel: View

    protected fun getArgDate(): LocalDate = arguments?.getDate(ARG_HISTORY_DATE) ?: LocalDate.now()

    protected fun getArgAccount(): Account?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountParcelable::class.java)?.toAccount() }

    protected abstract fun getInputHistory(): History

    /**
     * @return true means all inputs are valid, false otherwise
     */
    protected abstract fun validateInput(history: History): Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnSave.setOnClickListener { onClickSave() }
        btnCancel.setOnClickListener { onClickCancel() }
        dateTextView.setOnClickListener { onClickInputDate(viewModel.getDate()) }

        observeDateChange()
        observeAccountSelectionChange()
        observeSaveUIState()
        loadHistoryIfRequired()
        loadDefaultSelectedAccountIfRequired()
    }

    override fun onResume() {
        super.onResume()
        setActivityTitle(title)
    }

    private fun observeDateChange() {
        // observe date changes (both fragments do this)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dateState.collectLatest { date ->
                dateTextView.text = date.format(DATE_WITH_WEAKDAY_FORMAT)
            }
        }
    }

    private fun observeAccountSelectionChange() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.accountState.collectLatest { account ->
                Log.d(TAG, "account picked = $account")
                onAccountSelectionChange(account)
                if (null != account) {
                    showSetDefaultAccount(account)
                }
                else if (getArgAction() == Constants.ACTION_CREATE && null == getArgAccount()) {
                    loadDefaultAccount()
                }
            }
        }
    }

    protected open fun onAccountSelectionChange(account: Account?) {}

    private fun loadHistoryIfRequired() {
        if (isActionEdit() && viewModel.history == null) {
            viewModel.findHistory(getArgId())
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.historyState.filterNotNull().collectLatest { onFindHistoryUIStateChange(it) }
            }
        }
    }

    private fun observeSaveUIState() {
        // NOTE: lifecycleScope bound to fragment lifecycle
        //      viewLifecycleOwner.lifecycleScope bound to view which starts onCreateView and ends onDestroyView
        // if i use lifecycleScope then on each recreate of view an launch code will be added i.e. multiple collectors will present
        // each of the collector will get notified for each ui state change, therefore there will be multiple unnecessary ui state handle
        // but when launch is added to viewLifecycleOwner.lifecycleScope, on each view recreate new lifecycleScope is created and lunch code
        // is added to the newer lifecycleScope and older is disabled. so do duplicate ui state handle
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveHistoryState.collectLatest { onSaveHistoryUIStateChange(it) }
        }
    }

    /////////////////////////////////////////////////////////////////
    ///                     Event Handler                        ///
    ///////////////////////////////////////////////////////////////

    // ui state event for find history
    private fun onFindHistoryUIStateChange(state: UIState<History>) {
        Log.d(TAG,"onFindHistoryUIStateChange: $state")
        when(state) {
            is UIState.UISuccess -> {
                if (null == state.data) {
                    onHistoryNotFound()
                }
                else {
                    onHistoryFound(state.data)
                }
            }
            is UIState.UIError -> onFindHistoryError(state.cause)
            is UIState.UILoading -> onStartFindHistory()
        }
    }

    protected open fun onStartFindHistory() {}

    protected open fun onHistoryNotFound() {
        QuickMessages.toastError(requireContext(),getString(R.string.message_history_not_found))
        popBack()
    }

    protected open fun onHistoryFound(history: History) {
        fillCommonFields(history)
    }

    protected open fun onFindHistoryError(cause: Throwable?) {
        Log.e(TAG,null,cause)
        QuickMessages.toastError(requireContext(),getString(R.string.message_error))
        popBack()
    }

    // ui state event for save history

    private fun onSaveHistoryUIStateChange(state: UIState<History>) {
        when(state) {
            is UIState.UISuccess -> onHistorySaved(state.data)
            is UIState.UIError -> onSaveHistoryError(state.cause)
            is UIState.UILoading -> onStartSaveHistory()
        }
    }

    protected open fun onStartSaveHistory() {}

    protected open fun onHistorySaved(history: History?) {}

    protected open fun onSaveHistoryError(cause: Throwable?) {
        Log.e(TAG,"save error",cause)
        QuickMessages.simpleAlertError(requireContext(),R.string.message_error_save_history)
    }

    protected open fun onClickInputDate(date: LocalDate) {
        // NOTE: DatePickerDialog works with 0 based month i.e. 0 = January 1 = February etc.
        // where as LocalDate works with 1 based month i.e. 1 = January 2 = February etc.
        val picker = DatePickerDialog(requireContext(), { _, y, m, d ->
            viewModel.setDate(LocalDate.of(y, m+1, d))
        }, date.year, date.monthValue-1, date.dayOfMonth)
        picker.show()
    }

    protected open fun onClickSave() { handleSave() }

    protected open fun onClickCancel() { popBack() }

    /////////////////////////////////////////////////////////////////
    ///                    Utility Methods                       ///
    ///////////////////////////////////////////////////////////////

    protected open val title: String get() = getString(R.string.app_name)

    protected fun handleSave() {
        val history = getInputHistory()
        Log.d(TAG,"handleSave: input history $history")
        if (validateInput(history)) {
            if (isActionEdit()) {
                viewModel.setHistory(history)
            }
            else {
                viewModel.addHistory(history)
            }
        }
    }

    protected fun fillCommonFields(history: History) {
        amountInput.setText(history.amount.toString())
        noteInput.setText(history.note)
        viewModel.setDate(history.date)
    }

    protected fun popBack() { requireActivity().finish() }

    protected fun <T> createChip(container: ViewGroup,
                                 data: T?,
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

    protected fun loadDefaultSelectedAccountIfRequired() {
        if (getArgAction() == Constants.ACTION_CREATE && viewModel.getAccountSelection() == null) {
            loadDefaultAccount()
        }
    }

    private fun loadDefaultAccount() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getDefaultAccount().collectLatest { defaultAccount ->
                viewModel.setAccountSelection(defaultAccount)
            }
        }
    }

    private fun showSetDefaultAccount(account: Account) {
        if (cache.isShowSetDefaultAccount() && viewModel.defaultAccount != account && getArgAccount() == null) {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setMessage(getString(R.string.message_set_default_account, account.name))
                .setNeutralButton(R.string.label_never_show) { _,_ ->
                    cache.setShowSetDefaultAccount(false)
                }
                .setPositiveButton(R.string.label_yes) { _,_ ->
                    viewModel.setDefaultAccount(account)
                }
                .setNegativeButton(R.string.label_cancel,null)
                .create()
            dialog.show()
        }
    }
}
