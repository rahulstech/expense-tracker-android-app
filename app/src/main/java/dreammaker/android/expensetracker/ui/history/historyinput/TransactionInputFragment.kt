package dreammaker.android.expensetracker.ui.history.historyinput

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.HistoryInputLayoutBinding
import dreammaker.android.expensetracker.util.GroupParcelable
import dreammaker.android.expensetracker.util.createInputChip
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.isActionEdit
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History

class TransactionInputFragment : BaseHistoryInputFragment() {

    companion object {
        private val TAG = TransactionInputFragment::class.simpleName
        const val ARG_HISTORY_DATE = "arg.history_date"
    }

    private lateinit var binding: HistoryInputLayoutBinding

    override val dateTextView: TextView get() = binding.inputDate
    override val amountInput: EditText get() = binding.inputAmount
    override val noteInput: EditText get() = binding.inputNote
    override val btnSave: View get() = binding.btnSave
    override val btnCancel: View get() = binding.btnCancel

    /////////////////////////////////////////////////////////////////
    ///                 Fragment Argument                        ///
    ///////////////////////////////////////////////////////////////

    private fun getArgGroup(): Group?
            = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_GROUP, GroupParcelable::class.java)?.toGroup() }

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
        binding = HistoryInputLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preparePrimaryAccount()
        prepareGroup()
    }

    /////////////////////////////////////////////////////////////////
    ///                     Event Handler                        ///
    ///////////////////////////////////////////////////////////////

    override fun validateInput(history: History): Boolean {
        binding.errorSource.visibilityGone()
        var allValid = true
        if (history.primaryAccountId == 0L) {
            allValid = false
            binding.errorSource.visible()
        }
        return allValid
    }

    override fun getInputHistory(): History {
        val id = getArgId(0L)
        val date = viewModel.getDate()
        val amountText = binding.inputAmount.text.toString()
        val amount = if (amountText.isBlank()) 0f else amountText.toFloat()
        val note = binding.inputNote.text.toString().takeUnless { it.isBlank() }
        val account: Account? = viewModel.getAccount()
        val group: Group? = viewModel.getGroup()
        return when(isCreditHistory()) {
            true -> History.CreditHistory(
                id = id,
                amount = amount,
                date = date,
                note = note ?: getString(R.string.label_history_type_credit),
                primaryAccountId = account?.id ?: 0,
                groupId = group?.id
            )
            else -> History.DebitHistory(
                id = id,
                amount = amount,
                date = date,
                note = note ?: getString(R.string.label_history_type_debit),
                primaryAccountId = account?.id ?: 0,
                groupId = group?.id
            )
        }
    }

    /////////////////////////////////////////////////////////////////
    ///              Background Response Handlers                ///
    ///////////////////////////////////////////////////////////////

    override fun onHistoryFound(history: History) {
        super.onHistoryFound(history)
        selectHistoryType(history)
        viewModel.setAccount(history.primaryAccount)
        viewModel.setGroup(history.group)
    }

    override fun onHistorySaved(history: History?) {
        super.onHistorySaved(history)
        if (isActionEdit()) {
            QuickMessages.toastSuccess(requireContext(),getString(R.string.message_success_save_history))
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

    /////////////////////////////////////////////////////////////////
    ///                     Prepare Methods                      ///
    ///////////////////////////////////////////////////////////////

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

    private fun preparePrimaryAccount() {
        binding.inputSource.setOnClickListener {
            navController.navigate(R.id.action_input_transaction_to_account_picker, bundleOf(
                Constants.KEY_IS_PRIMARY to true
            ))
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.primaryAccountState.collectLatest { account ->
                Log.d(TAG, "account changed $account")
                updatePrimaryAccountChip(account)
            }
        }
    }

    private fun prepareGroup() {
        binding.inputGroup.setOnClickListener {
            navController.navigate(R.id.action_create_history_to_group_picker)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.groupState.collectLatest { group ->
                Log.d(TAG,"group changed $group")
                updateGroupChip(group)
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    ///                    utility Methods                       ///
    ///////////////////////////////////////////////////////////////

    private fun updatePrimaryAccountChip(account: Account?) {
        val container = binding.selectedSourceContainer
        createChip(container,account,{
            createInputChip(container, it.name, false)
        }, false)
    }

    private fun updateGroupChip(group: Group?) {
        val container = binding.selectedGroupContainer
        createChip(container, group, {
            createInputChip(container, it.name, true)
        }, true, { removeSelectedGroup() })
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