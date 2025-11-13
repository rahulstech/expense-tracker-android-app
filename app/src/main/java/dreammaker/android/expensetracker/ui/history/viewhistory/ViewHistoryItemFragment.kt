package dreammaker.android.expensetracker.ui.history.viewhistory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HistoryItemLayoutBinding
import dreammaker.android.expensetracker.ui.history.historyinput.TransactionInputFragment
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.util.OperationResult
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.util.UIState
import dreammaker.android.expensetracker.util.createInputChip
import dreammaker.android.expensetracker.util.getBackgroundColor
import dreammaker.android.expensetracker.util.getColorOnBackground
import dreammaker.android.expensetracker.util.getHistoryType
import dreammaker.android.expensetracker.util.getLabel
import dreammaker.android.expensetracker.util.hasArgument
import dreammaker.android.expensetracker.util.putHistoryType
import dreammaker.android.expensetracker.util.toCurrencyString
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewHistoryItemFragment: Fragment(), MenuProvider {

    companion object {
        private val TAG = ViewHistoryItemFragment::class.simpleName
        const val DATE_FORMAT = "EEEE, dd MMMM, yyyy"
        const val ARG_HISTORY_TYPE = "arg.history_type"
    }

    private var _binding: HistoryItemLayoutBinding? = null
    private val binding get() = _binding!!

    private val navController: NavController by lazy { findNavController() }
    private val viewModel: ViewHistoryItemViewModel by viewModels()

    private fun getArgHistoryId(): Long = requireArguments().getLong(Constants.ARG_ID)

    private fun getArgHistoryType(): HistoryType = requireArguments().getHistoryType(ARG_HISTORY_TYPE)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasArgument(Constants.ARG_ID)) {
            throw IllegalArgumentException("'${Constants.ARG_ID}' argument is required")
        }
        if (!hasArgument(ARG_HISTORY_TYPE)) {
            throw IllegalArgumentException("'$ARG_HISTORY_TYPE' argument is required")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HistoryItemLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = getArgHistoryId()
        val type = getArgHistoryType()
        viewModel.findHistory(id,type).observe(viewLifecycleOwner, this::onHistoryLoaded)
        lifecycleScope.launch {
            viewModel.removeHistoryState.collectLatest { state ->
                when (state) {
                    is UIState.UISuccess -> {
                        QuickMessages.toastSuccess(requireContext(),getString(R.string.message_success_delete_history))
                        navController.popBackStack()
                    }
                    is UIState.UIError -> {
                        QuickMessages.simpleAlertError(requireContext(),R.string.message_fail_delete_history)
                    }
                    else -> {}
                }
            }
        }
        (requireActivity() as MenuHost).addMenuProvider(this,viewLifecycleOwner)
    }

    private fun onHistoryLoaded(history: HistoryModel?) {
        if (null == history){
            Toast.makeText(requireContext(), R.string.message_history_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return
        }
        binding.containerGroupAndTags.removeAllViews()
        prepareDate(history.date!!)
        prepareType(history.type!!)
        prepareAmount(history.amount!!)
        prepareNote(history.note)
        prepareSource(history)
        prepareDestination(history)
        prepareGroup(history.group)
        requireActivity().invalidateOptionsMenu()
    }

    private fun prepareDate(date: Date) {
        binding.date.text = date.format(DATE_FORMAT)
    }

    private fun prepareType(type: HistoryType) {
        val container = binding.containerGroupAndTags
        val chip = createChip(container, type.getLabel(requireContext()))
        chip.chipBackgroundColor = type.getBackgroundColor(requireContext())
        chip.setTextColor(type.getColorOnBackground(requireContext()))
        container.addView(chip, 0)
    }

    private fun prepareAmount(amount: Float,) {
        binding.amount.text = amount.toCurrencyString()
    }

    private fun prepareNote(note: String?) {
        if (note.isNullOrBlank()) {
            binding.note.text = null
            binding.note.visibilityGone()
        }
        else {
            binding.note.text = note
            binding.note.visible()
        }
    }

    private fun prepareSource(history: HistoryModel) {
        when(history.type) {
            HistoryType.DEBIT -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_account)
                binding.source.text = history.primaryAccount?.name
                binding.sourceGroup.visible()
            }
            HistoryType.TRANSFER -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_account)
                binding.source.text = history.primaryAccount?.name
                binding.sourceGroup.visible()
            }
            else -> {
                binding.sourceGroup.visibilityGone()
            }
        }
        binding.viewSource.setOnClickListener { onClickViewSource(history) }
    }

    private fun onClickViewSource(history: HistoryModel) {
        val type = history.type!!
        if (type != HistoryType.CREDIT) {
            navController.navigate(R.id.action_view_history_to_view_account, Bundle().apply {
                putLong(Constants.ARG_ID, history.primaryAccountId!!)
            })
        }
    }

    private fun prepareDestination(history: HistoryModel) {
        when(history.type) {
            HistoryType.CREDIT -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_destination_account)
                binding.destination.text = history.primaryAccount?.name
                binding.destinationGroup.visible()
            }
            HistoryType.TRANSFER -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_destination_account)
                binding.destination.text = history.secondaryAccount?.name
                binding.destinationGroup.visible()
            }
            else -> {
                binding.destinationGroup.visibilityGone()
            }
        }
        binding.viewDestination.setOnClickListener { onClickViewDestination(history) }
    }

    private fun prepareGroup(group: GroupModel?) {
        // TODO: handle null group
        group?.let {
            val chip = createChip(binding.containerGroupAndTags, group.name!!)
            chip.setOnClickListener {
                navController.navigate(R.id.action_view_history_to_view_group, Bundle().apply {
                    putLong(Constants.ARG_ID, group.id!!)
                })
            }
            binding.containerGroupAndTags.addView(chip)
        }
    }

    private fun createChip(container: ViewGroup, text: String): Chip {
        return createInputChip(container, text, false)
    }

    private fun onClickViewDestination(history: HistoryModel) {
        val type = history.type!!
        if (type != HistoryType.DEBIT) {
            navController.navigate(R.id.action_view_history_to_view_account, Bundle().apply {
                putLong(Constants.ARG_ID, history.primaryAccountId!!)
            })
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        viewModel.getStoredHistory()?.let {
            inflater.inflate(R.menu.view_history_menu, menu)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit -> {
                onClickEditHistory(viewModel.getStoredHistory())
                true
            }
            R.id.delete -> {
                onClickDeleteHistory(viewModel.getStoredHistory())
                true
            }
            else ->  false
        }
    }

    private fun onClickEditHistory(history: HistoryModel?) {
        history?.let {
            val type = history.type
            when(type) {
                null -> {}
                HistoryType.TRANSFER -> {
                    navController.navigate(R.id.action_view_history_to_edit_transfer_history, Bundle().apply {
                        putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
                        putLong(Constants.ARG_ID, history.id!!)
                    })
                }
                else -> {
                    navController.navigate(R.id.action_view_history_to_edit_history, Bundle().apply {
                        putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
                        putLong(Constants.ARG_ID, history.id!!)
                        putHistoryType(TransactionInputFragment.ARG_HISTORY_TYPE, history.type!!)
                    })
                }
            }
        }
    }

    private fun onClickDeleteHistory(history: HistoryModel?) {
        history?.let {
            QuickMessages.alertWarning(requireContext(),
                getString(R.string.message_warning_delete),
                QuickMessages.AlertButton(getString(R.string.label_no)),
                QuickMessages.AlertButton(getString(R.string.label_yes)){
                    viewModel.removeHistory(history)
                }
            )
        }
    }

    private fun onHistoryDeleted(result: OperationResult<HistoryModel>?) {
        result?.let {
            if (result.isFailure()) {
                Log.e(TAG, "onHistoryDeleted failed history=${getArgHistoryId()}", result.error)
            }
            else {
                navController.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}