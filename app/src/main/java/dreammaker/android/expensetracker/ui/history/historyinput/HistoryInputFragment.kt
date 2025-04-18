package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HistoryInputLayoutBinding
import dreammaker.android.expensetracker.ui.util.ARG_DESTIATION_LABEL
import dreammaker.android.expensetracker.ui.util.createAccountChip
import kotlinx.coroutines.launch

class HistoryInputFragment : Fragment() {

    private val TAG = HistoryInputFragment::class.simpleName

    private val HISTORY_INPUT_DATE_FORMAT = "dd-MMMM-yyyy"

    companion object {
        val ARG_HISTORY_TYPE = "arg.history_type"
        val ARG_ACTION = "action"
        val ACTION_CREATE_HISTORY = "action.history.create"
        val ACTION_EDIT_HISTORY = "action.history.edit"
    }

    private lateinit var binding: HistoryInputLayoutBinding
    private lateinit var viewModel: HistoryInputViewModel
    private lateinit var navController: NavController
    private lateinit var selectedDate: Date

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[HistoryInputViewModel::class.java]
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
        navController = Navigation.findNavController(view)

        setSelectedDate(Date())
        binding.inputDate.setOnClickListener{ onClickInputDate(selectedDate) }

        val type = HistoryType.DEBIT
        prepareSourceInput(type)
        prepareDestinationInput(type)

        lifecycleScope.launch {
            viewModel.selectedSrcAccount.collect { account ->
                binding.selectedSourceContainer.removeAllViews()
                if (null != account) {
                    val chip = createAccountChip(requireContext(), account)
                    binding.selectedSourceContainer.addView(chip)
                }
            }
        }
    }

    private fun prepareSourceInput(type: HistoryType) {
        if (type == HistoryType.CREDIT) {
            binding.labelInputSource.setText(R.string.label_history_input_source_credit)
            binding.inputSource.setOnClickListener{
                val args = Bundle().apply {
                    putString(ARG_DESTIATION_LABEL, getString(R.string.title_choose_credit_source_person))
                }
                navController.navigate(R.id.action_history_input_to_person_chooser_list, args)
            }
        }
        else if (type == HistoryType.DEBIT) {
            binding.labelInputSource.setText(R.string.label_history_input_source_debit)
            binding.inputSource.setOnClickListener{
                val args = Bundle().apply {
                    putString(ARG_DESTIATION_LABEL, getString(R.string.title_choose_debit_source_account))
                    putString(PickerHistoryAccountFragment.ARG_KEY_SELECTED_ACCOUNT, PickerHistoryAccountFragment.SELECTED_SRC_ACCOUNT)
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
        }
        else if (type == HistoryType.TRANSFER) {
            binding.labelInputSource.setText(R.string.label_history_input_source_transfer)
            binding.inputSource.setOnClickListener{
                val args = Bundle().apply {
                    putString(ARG_DESTIATION_LABEL, getString(R.string.title_choose_transfer_source_person))
                    putString(PickerHistoryAccountFragment.ARG_KEY_SELECTED_ACCOUNT, PickerHistoryAccountFragment.SELECTED_SRC_ACCOUNT)
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
        }
    }

    private fun prepareDestinationInput(type: HistoryType) {
        if (type == HistoryType.CREDIT) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_credit)
            binding.inputDestination.setOnClickListener{
                val args = Bundle().apply {
                    putString(ARG_DESTIATION_LABEL, getString(R.string.title_choose_credit_destination_account))
                    putString(PickerHistoryAccountFragment.ARG_KEY_SELECTED_ACCOUNT, PickerHistoryAccountFragment.SELECTED_DEST_ACCOUNT)
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
        }
        else if (type == HistoryType.DEBIT) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_debit)
            binding.inputDestination.setOnClickListener{
                val args = Bundle().apply {
                    putString(ARG_DESTIATION_LABEL, getString(R.string.title_choose_debit_destination_person))
                }
                navController.navigate(R.id.action_history_input_to_person_chooser_list, args)
            }
        }
        else if (type == HistoryType.TRANSFER) {
            binding.labelInputDestination.setText(R.string.label_history_input_destination_transfer)
            binding.inputDestination.setOnClickListener{
                val args = Bundle().apply {
                    putString(ARG_DESTIATION_LABEL, getString(R.string.title_choose_transfer_detination_person))
                    putString(PickerHistoryAccountFragment.ARG_KEY_SELECTED_ACCOUNT, PickerHistoryAccountFragment.SELECTED_DEST_ACCOUNT)
                }
                navController.navigate(R.id.action_history_input_to_account_chooser_list, args)
            }
        }
    }

    private fun onClickInputDate(date: Date) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val newDate = Date(year, month, day)
            setSelectedDate(newDate)
        }, date.year, date.month, date.dayOfMonth)
        datePicker.show()
    }

    private fun setSelectedDate(date: Date) {
        selectedDate = date
        binding.inputDate.text = date.format(HISTORY_INPUT_DATE_FORMAT)
    }
}