package dreammaker.android.expensetracker.ui.history.historyitem

import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HistoryItemLayoutBinding
import dreammaker.android.expensetracker.ui.util.getHistoryType
import java.util.Locale


class ViewHistoryItemViewModel(app: Application): AndroidViewModel(app) {

    private val historyDao: HistoryDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
    }

    private lateinit var historyLiveData: LiveData<HistoryModel?>

    fun findHistory(id: Long, type: HistoryType): LiveData<HistoryModel?> {
        if (!::historyLiveData.isInitialized) {
            historyLiveData = historyDao.findHistoryByIdAndType(id, type)
        }
        return historyLiveData
    }
}

class ViewHistoryItemFragment: Fragment() {

    companion object {
        private val TAG = ViewHistoryItemFragment::class.simpleName
        const val DATE_FORMAT = "EEEE, dd MMMM, yyyy"
        const val ARG_HISTORY_ID = "arg.history_id"
        const val ARG_HISTORY_TYPE = "arg.history_type"
    }

    private var binding: HistoryItemLayoutBinding? = null
    private var navController: NavController? = null
    private lateinit var viewModel: ViewHistoryItemViewModel
    private var loadedHistory: HistoryModel? = null

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
            loadedHistory = null
            navController?.popBackStack()
             return
        }
        binding?.let {
            loadedHistory = history
            prepareDate(history.date!!,binding!!)
            prepareType(history.type!!, binding!!)
            prepareAmount(history.amount!!, binding!!)
            prepareNote(history.note, binding!!)
            prepareSource(history, binding!!)
            prepareDestination(history, binding!!)
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
            HistoryType.EXPENSE -> {
                setTypeTextAndBackgroundColor(binding, R.string.label_history_type_expense,R.color.colorExpense)
            }
            HistoryType.INCOME -> {
                setTypeTextAndBackgroundColor(binding, R.string.label_history_type_income,R.color.colorIncome)
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
        binding.note.setText(note)
    }

    private fun prepareSource(history: HistoryModel, binding: HistoryItemLayoutBinding) {
        val type = history.type
        when(type) {
            HistoryType.CREDIT -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_credit)
                binding.source.text = history.srcPerson?.name
            }
            HistoryType.DEBIT -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_debit)
                binding.source.text = history.srcAccount?.name
            }
            HistoryType.EXPENSE -> {
                binding.sourceLabel.text = getString(R.string.label_history_item_source_expense)
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

    }

    private fun prepareDestination(history: HistoryModel, binding: HistoryItemLayoutBinding) {
        val type = history.type
        when(type) {
            HistoryType.CREDIT -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_destination_credit)
                binding.destination.text = history.destAccount?.name
            }
            HistoryType.DEBIT -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_source_debit)
                binding.destination.text = history.destPerson?.name
            }
            HistoryType.INCOME -> {
                binding.destinationLabel.text = getString(R.string.label_history_item_destination_income)
                binding.destination.text = history.destAccount?.name
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

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.history_item_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.delete) {
            loadedHistory?.let { onClickDeleteHistory(loadedHistory!!) }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onClickDeleteHistory(history: HistoryModel) {}

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}