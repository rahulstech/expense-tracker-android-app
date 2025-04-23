package dreammaker.android.expensetracker.ui.person.inputperson

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.InputPersonBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.setActivityTitle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class PersonInputFragment : Fragment() {

    private val TAG = PersonInputFragment::class.simpleName

    private var _binding: InputPersonBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PersonInputViewModel
    private lateinit var navController: NavController

    private val observer = Observer<PersonModel?> { onPersonLoaded(it) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[PersonInputViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InputPersonBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)

        if (isActionEdit()) {
            if (null == viewModel.getStoredPerson()) {
                val id = requireArguments().getLong(Constants.ARG_ID)
                viewModel.findPersonById(id).observe(viewLifecycleOwner, observer)
            }
        }
        lifecycleScope.launch {
            viewModel.resultState.filterNotNull().collect {
                onSave(it)
                viewModel.emptyState()
            }
        }
    }

    private fun onPersonLoaded(person: PersonModel?) {
        if (null == person) {
            Toast.makeText(requireContext(), R.string.message_person_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        else {
            binding.name.setText(person.name)
            binding.due.setText(person.due.toString())
            viewModel.personLiveData.removeObserver(observer)
        }
    }

    private fun getArgAction(): String? = arguments?.getString(Constants.ARG_ACTION)

    private fun isActionEdit(): Boolean = arguments?.getString(Constants.ARG_ACTION) == Constants.ACTION_EDIT

    private fun onClickCancel() {
        navController.popBackStack()
    }

    private fun onClickSave() {
        val peron = getInputPerson()
        if (!validateInput(peron)) {
            if (isActionEdit()) {
                viewModel.setPerson(peron)
            }
            else {
                viewModel.addPerson(peron)
            }
        }
    }

    private fun validateInput(person: PersonModel): Boolean {
        binding.personNameInput.error = null
        binding.dueInput.error = null
        var hasError = false
        if (null == person.name) {
            hasError = true
            binding.personNameInput.error = getString(R.string.error_empty_person_name)
        }
        if (null == person.due) {
            hasError = true
            binding.dueInput.error = getString(R.string.error_invalid_due_input)
        }
        return hasError
    }

    private fun getInputPerson(): PersonModel {
        val name = binding.name.text.toString()
        val due = binding.due.text.toString().toFloatOrNull()
        return if (isActionEdit()) {
            viewModel.getStoredPerson()!!.copy(name=name,due=due)
        }
        else {
            PersonModel(null,name,due)
        }
    }

    private fun onSave(result: OperationResult<PersonModel>?) {
        result?.let {
            if (result.isFailure()) {
                Log.e(TAG, "onSave action=${getArgAction()}",result.error)
                val message = getString(
                    if (isActionEdit()) R.string.message_fail_edit_person
                    else R.string.message_fail_create_person
                )
                Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
            }
            else {
                val message = getString(
                    if (isActionEdit()) R.string.message_success_edit_person
                    else R.string.message_success_create_person
                )
                Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setActivityTitle(getString(
            if (isActionEdit()) R.string.title_edit_person
            else R.string.title_create_person
        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}