package dreammaker.android.expensetracker.ui.history.viewhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.DATE_WITH_WEAKDAY_FORMAT
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.ViewHistoryLayoutBinding
import dreammaker.android.expensetracker.ui.UIState
import dreammaker.android.expensetracker.util.UNKNOWN_ACCOUNT
import dreammaker.android.expensetracker.util.UNKNOWN_GROUP
import dreammaker.android.expensetracker.util.createInputChip
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.getTypeBackgroundColor
import dreammaker.android.expensetracker.util.getTypeColorOnBackground
import dreammaker.android.expensetracker.util.getTypeLabel
import dreammaker.android.expensetracker.util.hasArgument
import dreammaker.android.expensetracker.util.toCurrencyString
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

class ViewHistoryItemFragment: Fragment(), MenuProvider {

    companion object {
        private val TAG = ViewHistoryItemFragment::class.simpleName
    }

    private var _binding: ViewHistoryLayoutBinding? = null
    private val binding get() = _binding!!

    private val navController: NavController by lazy { findNavController() }
    private val viewModel: ViewHistoryItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasArgument(Constants.ARG_ID)) {
            throw IllegalArgumentException("'${Constants.ARG_ID}' argument is required")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewHistoryLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = getArgId()
        viewModel.findHistory(id).observe(viewLifecycleOwner, this::onHistoryLoaded)
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

    private fun onHistoryLoaded(history: History?) {
        if (null == history){
            QuickMessages.toastError(requireContext(), getString(R.string.message_history_not_found))
            navController.popBackStack()
            return
        }
        binding.containerGroupAndTags.removeAllViews()
        prepareDate(history.date)
        prepareType(history)
        prepareAmount(history.amount)
        prepareNote(history.note)
        prepareSource(history)
        prepareDestination(history)
        prepareGroup(history)
        requireActivity().invalidateOptionsMenu()
    }

    private fun prepareDate(date: LocalDate) {
        binding.date.text = date.format(DATE_WITH_WEAKDAY_FORMAT)
    }

    private fun prepareType(history: History) {
        val container = binding.containerGroupAndTags
        val chip = createChip(container, history.getTypeLabel(requireContext()))
        chip.chipBackgroundColor = history.getTypeBackgroundColor(requireContext())
        chip.setTextColor(history.getTypeColorOnBackground(requireContext()))
        container.addView(chip, 0)

    }

    private fun prepareAmount(amount: Float) {
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

    private fun prepareSource(history: History) {
        val account = when(history) {
            is History.DebitHistory,
            is History.TransferHistory -> history.primaryAccount ?: UNKNOWN_ACCOUNT
            else -> {
                binding.sourceGroup.visibilityGone()
                return
            }
        }
        binding.sourceLabel.text = getString(R.string.label_history_item_source_account)
        binding.source.text = account.name
        binding.viewSource.setOnClickListener { onClickViewAccount(account.id) }
        binding.sourceGroup.visible()
    }

    private fun prepareDestination(history: History) {
        val account = when(history) {
            is History.CreditHistory -> history.primaryAccount ?: UNKNOWN_ACCOUNT
            is History.TransferHistory -> history.secondaryAccount ?: UNKNOWN_ACCOUNT
            else -> {
                binding.destinationGroup.visibilityGone()
                return
            }
        }
        binding.destinationLabel.text = getString(R.string.label_history_item_destination_account)
        binding.destination.text = account.name
        binding.viewDestination.setOnClickListener { onClickViewAccount(account.id) }
        binding.destinationGroup.visible()
    }

    private fun onClickViewAccount(id: Long) {
        navController.navigate(R.id.action_view_history_to_view_account, bundleOf (
            Constants.ARG_ID to id
        ))
    }

    private fun prepareGroup(history: History) {
        val group = if (history.groupId != null) { history.group ?: UNKNOWN_GROUP } else return
        val chip = createChip(binding.containerGroupAndTags, group.name)
        chip.setOnClickListener {
            navController.navigate(R.id.action_view_history_to_view_group, bundleOf (
                Constants.ARG_ID to group.id
            ))
        }
        binding.containerGroupAndTags.addView(chip)
    }

    private fun createChip(container: ViewGroup, text: String): Chip {
        return createInputChip(container, text, false)
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

    private fun onClickEditHistory(history: History?) {
        when(history) {
            null -> {}
            is History.TransferHistory -> {
                navController.navigate(R.id.action_view_history_to_edit_transfer_history, Bundle().apply {
                    putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
                    putLong(Constants.ARG_ID, history.id)
                })
            }
            else -> {
                navController.navigate(R.id.action_view_history_to_edit_history, Bundle().apply {
                    putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
                    putLong(Constants.ARG_ID, history.id)
                })
            }
        }
    }

    private fun onClickDeleteHistory(history: History?) {
        history?.let {
            QuickMessages.alertWarning(requireContext(),
                getString(R.string.message_warning_delete_history), // TODO: fix this message
                QuickMessages.AlertButton(getString(R.string.label_no)),
                QuickMessages.AlertButton(getString(R.string.label_yes)){
                    viewModel.removeHistory(history)
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}