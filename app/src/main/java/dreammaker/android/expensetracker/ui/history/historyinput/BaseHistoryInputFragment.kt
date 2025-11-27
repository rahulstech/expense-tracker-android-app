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
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.FULL_DATE_FORMAT
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.ui.history.historyinput.TransactionInputFragment.Companion.ARG_HISTORY_DATE
import dreammaker.android.expensetracker.util.AccountParcelable
import dreammaker.android.expensetracker.ui.UIState
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.isActionEdit
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

    protected abstract val dateTextView: TextView
    protected abstract val amountInput: EditText
    protected abstract val noteInput: EditText
    protected abstract val btnSave: View
    protected abstract val btnCancel: View

    protected fun getArgDate(): LocalDate = arguments?.getDate(ARG_HISTORY_DATE) ?: LocalDate.now()

    protected fun getArgAccount(): Account?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountParcelable::class.java)?.toAccount() }

    protected abstract fun getInputHistory(): History

    protected abstract fun validateInput(history: History): Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnSave.setOnClickListener { onClickSave() }
        btnCancel.setOnClickListener { onClickCancel() }

        dateTextView.setOnClickListener {
            onClickInputDate(viewModel.getDate())
        }

        // observe date changes (both fragments do this)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dateState.collectLatest { date ->
                dateTextView.text = date.format(FULL_DATE_FORMAT)
            }
        }

        if (isActionEdit()) {
            viewModel.findHistory(getArgId())
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.historyState.filterNotNull().collectLatest { onFindHistoryUIStateChange(it) }
            }
        }

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
        val picker = DatePickerDialog(requireContext(), { _, y, m, d ->
            viewModel.setDate(LocalDate.of(y, m, d))
        }, date.year, date.monthValue, date.dayOfMonth)
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
}
