package dreammaker.android.expensetracker.ui.history.historyinput

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.TransferHistoryInputLayoutBinding
import dreammaker.android.expensetracker.util.createInputChip
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.isActionEdit
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.History

class TransferHistoryInputFragment : BaseHistoryInputFragment() {

    companion object {
        private val TAG = TransferHistoryInputFragment::class.simpleName
    }

    private var _binding: TransferHistoryInputLayoutBinding? = null
    private val binding get() = _binding!!

    override val dateTextView: TextView get() = binding.inputDate
    override val amountInput: EditText get() = binding.inputAmount
    override val noteInput: EditText get() = binding.inputNote
    override val btnSave: View get() = binding.btnSave
    override val btnCancel: View get() = binding.btnCancel
    override val title: String
        get() = if (isActionEdit()) {
            getString(R.string.title_edit_transfer_history)
        } else {
            getString(R.string.title_create_transfer_history)
        }

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
        super.onViewCreated(view, savedInstanceState)
        preparePrimaryAccount()
        prepareSecondaryAccount()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun validateInput(history: History): Boolean {
        binding.amountInputLayout.error = null
        binding.errorSource.visibilityGone()
        binding.errorDestination.visibilityGone()
        var allValid = true
        if (history.primaryAccountId==0L) {
            allValid = false
            binding.errorSource.visible()
        }
        if (history.secondaryAccountId==0L) {
            allValid = false
            binding.errorDestination.visible()
        }
        return allValid
    }

    override fun getInputHistory(): History {
        val id = getArgId()
        val date = viewModel.getDate()
        val amountText = binding.inputAmount.text.toString()
        val amount = if (amountText.isBlank()) 0f else amountText.toFloat()
        val note = binding.inputNote.text.toString().takeUnless { it.isBlank() } ?: getString(R.string.label_history_type_transfer)
        val srcAccount: Account? = viewModel.getAccount()
        val destAccount: Account? = viewModel.getAccount(false)
        return History.TransferHistory(
            id = id,
            amount = amount,
            date = date,
            note = note,
            primaryAccountId = srcAccount?.id ?: 0,
            secondaryAccountId = destAccount?.id ?: 0
        )
    }

    /////////////////////////////////////////////////////////////////
    ///              Background Response Handlers                ///
    ///////////////////////////////////////////////////////////////

    override fun onHistoryFound(history: History) {
        super.onHistoryFound(history)
        viewModel.setAccount(history.primaryAccount)
        viewModel.setAccount(history.secondaryAccount,false)
    }

    override fun onHistorySaved(history: History?) {
        super.onHistorySaved(history)
        QuickMessages.toastSuccess(requireContext(), getString(R.string.message_success_save_history))
        popBack()
    }

    /////////////////////////////////////////////////////////////////
    ///                     Prepare Methods                      ///
    ///////////////////////////////////////////////////////////////

    private fun preparePrimaryAccount() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.primaryAccountState.collectLatest { account ->
                Log.d(TAG,"secondary account changed $account")
                updatePrimaryAccountChip(account)
            }
        }
    }

    private fun prepareSecondaryAccount() {
        binding.inputDestination.setOnClickListener {
            navController.navigate(R.id.action_input_money_transfer_to_account_picker, bundleOf(
                Constants.KEY_IS_PRIMARY to false,
                Constants.ARG_DISABLED_ID to getArgAccount()?.id
            ))
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.secondaryAccountState.collectLatest { account ->
                Log.d(TAG,"secondary account changed $account")
                updateSecondaryAccountChip(account)
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    ///                     Update Methods                       ///
    ///////////////////////////////////////////////////////////////

    private fun updatePrimaryAccountChip(account: Account?) {
        val container = binding.selectedSourceContainer
        createChip(
            container, account,{
            createInputChip(container, it.name, false)
        }, false)
    }

    private fun updateSecondaryAccountChip(account: Account?) {
        val container = binding.selectedDestinationContainer
        createChip(container,account, {
            createInputChip(container, it.name, false)
        }, false)
    }
}