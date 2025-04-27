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
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HistoryItemLayoutBinding
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.createInputChip
import dreammaker.android.expensetracker.ui.util.getBackgroundColor
import dreammaker.android.expensetracker.ui.util.getColorOnBackground
import dreammaker.android.expensetracker.ui.util.getHistoryType
import dreammaker.android.expensetracker.ui.util.getLabel
import dreammaker.android.expensetracker.ui.util.putHistoryType
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ViewHistoryItemFragment: Fragment(), MenuProvider {

    companion object {
        private val TAG = ViewHistoryItemFragment::class.simpleName
        const val DATE_FORMAT = "EEEE, dd MMMM, yyyy"
        const val ARG_HISTORY_ID = "arg.history_id"
        const val ARG_HISTORY_TYPE = "arg.history_type"
    }

    private var _binding: HistoryItemLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val viewModel: ViewHistoryItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey(ARG_HISTORY_ID) != true) {
            throw IllegalArgumentException("'$ARG_HISTORY_ID' argument is required")
        }
        if (arguments?.containsKey(ARG_HISTORY_TYPE) != true) {
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
        navController = Navigation.findNavController(view)
        val id = requireArguments().getLong(ARG_HISTORY_ID)
        val type = requireArguments().getHistoryType(ARG_HISTORY_TYPE)!!
        viewModel.findHistory(id,type).observe(viewLifecycleOwner, this::onHistoryLoaded)
        lifecycleScope.launch {
            viewModel.resultState.filterNotNull().collect{
                onHistoryDeleted(it)
                viewModel.emptyResult()
            }
        }
    }

    private fun onHistoryLoaded(history: HistoryModel?) {
        if (null == history){
            Toast.makeText(requireContext(), R.string.message_history_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return
        }
        prepareDate(history.date!!)
        prepareType(history.type!!)
        prepareAmount(history.amount!!)
        prepareNote(history.note)
        prepareSource(history)
        prepareDestination(history)
        prepareGroupAndTags(history.group)
        requireActivity().invalidateOptionsMenu()
    }

    private fun prepareDate(date: Date) {
        binding.date.text = date.format(DATE_FORMAT)
    }

    private fun prepareType(type: HistoryType) {
        binding.type.text = type.getLabel(requireContext())
        binding.type.chipBackgroundColor = type.getBackgroundColor(requireContext())
        binding.type.setTextColor(type.getColorOnBackground(requireContext()))
    }

    private fun prepareAmount(amount: Float,) {
        binding.amount.text = amount.toCurrencyString()
    }

    private fun prepareNote(note: String?) {
        note?.let {
            binding.note.text = note
            binding.note.visible()
        }
    }

    private fun prepareSource(history: HistoryModel) {
        when(history.type) {
            HistoryType.DEBIT -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_debit)
                binding.source.text = history.srcAccount?.name
                binding.sourceGroup.visible()
            }
            HistoryType.TRANSFER -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_transfer)
                binding.source.text = history.srcAccount?.name
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
                putLong(Constants.ARG_ID, history.srcAccountId!!)
            })
        }
    }

    private fun prepareDestination(history: HistoryModel) {
        when(history.type) {
            HistoryType.CREDIT -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_destination_credit)
                binding.destination.text = history.destAccount?.name
                binding.destinationGroup.visible()
            }
            HistoryType.TRANSFER -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_destination_transfer)
                binding.destination.text = history.destAccount?.name
                binding.destinationGroup.visible()
            }
            else -> {
                binding.destinationGroup.visibilityGone()
            }
        }
        binding.viewDestination.setOnClickListener { onClickViewDestination(history) }
    }

    private fun prepareGroupAndTags(group: GroupModel?) {
        group?.let {
            val chip = createInputChip(requireContext(), group.name!!, false)
            chip.setOnClickListener {
                navController.navigate(R.id.action_view_history_to_view_group, Bundle().apply {
                    putLong(Constants.ARG_ID, group.id!!)
                })
            }
            binding.containerGroupAndTags.addView(chip)
            binding.groupAndTagsGroup.visible()
        }
    }

    private fun onClickViewDestination(history: HistoryModel) {
        val type = history.type!!
        if (type != HistoryType.DEBIT) {
            navController.navigate(R.id.action_view_history_to_view_account, Bundle().apply {
                putLong(Constants.ARG_ID, history.destAccountId!!)
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
            navController.navigate(R.id.action_view_history_to_edit_history, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
                putLong(Constants.ARG_ID, history.id!!)
                putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, history.type!!)
            })
        }
    }

    private fun onClickDeleteHistory(history: HistoryModel?) {
        history?.let {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(resources.getQuantityString(R.plurals.warning_delete_history, 1 ))
                .setPositiveButton(R.string.label_no, null)
                .setNegativeButton(R.string.label_yes) { _,_ -> viewModel.removeHistory(history) }
                .show()
        }
    }

    private fun onHistoryDeleted(result: OperationResult<HistoryModel>?) {
        result?.let {
            if (result.isFailure()) {
                Log.e(TAG, "onHistoryDeleted delete failed history=${viewModel.getStoredHistory()}", result.error)
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