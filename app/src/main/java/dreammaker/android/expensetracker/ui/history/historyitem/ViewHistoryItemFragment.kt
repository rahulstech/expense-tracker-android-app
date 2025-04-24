package dreammaker.android.expensetracker.ui.history.historyitem

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HistoryItemLayoutBinding
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.getHistoryType
import dreammaker.android.expensetracker.ui.util.putHistoryType
import java.util.Locale

class ViewHistoryItemFragment: Fragment() {

    companion object {
        private val TAG = ViewHistoryItemFragment::class.simpleName
        const val DATE_FORMAT = "EEEE, dd MMMM, yyyy"
        const val ARG_HISTORY_ID = "arg.history_id"
        const val ARG_HISTORY_TYPE = "arg.history_type"
    }

    private var binding: HistoryItemLayoutBinding? = null
    private lateinit var navController: NavController
    private lateinit var viewModel: ViewHistoryItemViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[ViewHistoryItemViewModel::class.java]
        setHasOptionsMenu(true)
    }

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
        binding = HistoryItemLayoutBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        val id = requireArguments().getLong(ARG_HISTORY_ID)
        val type = requireArguments().getHistoryType(ARG_HISTORY_TYPE)!!
        viewModel.findHistory(id,type).observe(viewLifecycleOwner, this::onHistoryLoaded)
    }

    private fun onHistoryLoaded(history: HistoryModel?) {
        if (null == history){
            Toast.makeText(requireContext(), R.string.message_history_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
             return
        }
        binding?.let {
            prepareDate(history.date!!,binding!!)
            prepareType(history.type!!, binding!!)
            prepareAmount(history.amount!!, binding!!)
            prepareNote(history.note, binding!!)
            prepareSource(history, binding!!)
            prepareDestination(history, binding!!)
            requireActivity().invalidateOptionsMenu()
        }
    }

    private fun prepareDate(date: Date, binding: HistoryItemLayoutBinding) {
        binding.date.text = date.format(DATE_FORMAT)
    }

    private fun prepareType(type: HistoryType, binding: HistoryItemLayoutBinding) {
        when(type) {
            HistoryType.CREDIT -> {
                setTypeTextAndBackgroundColor(binding, R.string.label_history_type_credit,R.color.colorCredit)
            }
            HistoryType.DEBIT -> {
                setTypeTextAndBackgroundColor(binding, R.string.label_history_type_debit,R.color.colorDebit)
            }
            HistoryType.TRANSFER -> {
                setTypeTextAndBackgroundColor(binding, R.string.label_history_type_transfer,R.color.colorTransfer)
            }
        }
    }

    private fun setTypeTextAndBackgroundColor(binding: HistoryItemLayoutBinding, @StringRes text: Int, @ColorRes backgroundColor: Int) {
        binding.type.text = getString(text)
        binding.type.chipBackgroundColor = ColorStateList.valueOf(resources.getColor(backgroundColor, null))
    }

    private fun prepareAmount(amount: Float, binding: HistoryItemLayoutBinding) {
        binding.amount.text = String.format(Locale.ENGLISH, "%.02f", amount)
    }

    private fun prepareNote(note: String?, binding: HistoryItemLayoutBinding) {
        binding.note.text = note
    }

    private fun prepareSource(history: HistoryModel, binding: HistoryItemLayoutBinding) {
        val type = history.type
        when(type) {
            HistoryType.CREDIT -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_credit)
                binding.source.text = history.group?.name
            }
            HistoryType.DEBIT -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_debit)
                binding.source.text = history.srcAccount?.name
            }
            HistoryType.TRANSFER -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_transfer)
                binding.source.text = history.srcAccount?.name
            }
            else -> {
                binding.divider2.visibility = View.GONE
                binding.sourceLabel.visibility = View.GONE
                binding.source.visibility = View.GONE
                binding.viewSource.visibility = View.GONE
            }
        }
        binding.viewSource.setOnClickListener { onClickViewSource(history) }
    }

    private fun onClickViewSource(history: HistoryModel) {
        val type = history.type!!
        if (type == HistoryType.TRANSFER || type == HistoryType.DEBIT) {
            navController.navigate(R.id.action_history_item_to_view_account, Bundle().apply {
                putLong(Constants.ARG_ID, history.srcAccountId!!)
            })
        }
    }

    private fun prepareDestination(history: HistoryModel, binding: HistoryItemLayoutBinding) {
        val type = history.type
        when(type) {
            HistoryType.CREDIT -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_destination_credit)
                binding.destination.text = history.destAccount?.name
            }
            HistoryType.DEBIT -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_destination_debit)
                binding.destination.text = history.group?.name
            }
            HistoryType.TRANSFER -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_destination_transfer)
                binding.destination.text = history.destAccount?.name
            }
            else -> {
                binding.divider1.visibility = View.GONE
                binding.destinationLabel.visibility = View.GONE
                binding.destination.visibility = View.GONE
                binding.viewDestination.visibility = View.GONE
            }
        }
        binding.viewDestination.setOnClickListener { onClickViewDestination(history) }
    }

    private fun onClickViewDestination(history: HistoryModel) {
        val type = history.type!!
        if (type == HistoryType.TRANSFER || type == HistoryType.CREDIT) {
            navController.navigate(R.id.action_history_item_to_view_account, Bundle().apply {
                putLong(Constants.ARG_ID, history.destAccountId!!)
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        viewModel.getStoredHistory()?.let {
            inflater.inflate(R.menu.history_item_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        return when (itemId) {
            R.id.edit -> {
                onClickEditHistory(viewModel.getStoredHistory()!!)
                true
            }
            R.id.delete -> {
                onClickDeleteHistory(viewModel.getStoredHistory()!!)
                true
            }
            else ->  return super.onOptionsItemSelected(item)
        }
    }

    private fun onClickEditHistory(history: HistoryModel) {
        navController.navigate(R.id.action_history_item_to_edit_history, Bundle().apply {
            putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
            putLong(Constants.ARG_ID, history.id!!)
            putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, history.type!!)
        })
    }

    private fun onClickDeleteHistory(history: HistoryModel) {
        viewModel.removeHistory(history)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}