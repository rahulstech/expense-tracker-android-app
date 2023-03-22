package dreammaker.android.expensetracker.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.concurrent.TaskMaster;
import dreammaker.android.expensetracker.concurrent.TaskResult;
import dreammaker.android.expensetracker.database.model.Person;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.viewmodel.PersonsViewModel;
import dreammaker.android.expensetracker.viewmodel.SavedStateViewModel;

public class InputPerson extends Fragment {

    private static final String KEY_OPERATION_KEY = "operation_key";

    private static final String KEY_PERSON_FETCHED = "person_fetched";

    private static final String KEY_GET_PERSON = "get_person";

    private static final String KEY_SAVE_PERSON = "save_person";

    private PersonsViewModel viewModel;
    private SavedStateViewModel mSavedState;
    private NavController navController;

    private TextInputLayout containerFirstName;
    private TextInputLayout containerDue;
    private TextInputLayout containerBorrow;
    private EditText inpFirstName;
    private EditText inpLastName;
    private EditText inpDue;
    private EditText inpBorrow;


    private String mLastOperationKey = null;
    private long mPersonId = 0;
    private boolean isPersonFetched = false;

    public InputPerson(){
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.input_person, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mSavedState = new ViewModelProvider(this).get(SavedStateViewModel.class);
        viewModel = new ViewModelProvider(requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity()
                        .getApplication()))
                .get(PersonsViewModel.class);

        containerFirstName = view.findViewById(R.id.container_first_name);
        containerDue = view.findViewById(R.id.container_due);
        containerBorrow = view.findViewById(R.id.container_borrow);
        inpFirstName = view.findViewById(R.id.first_name);
        inpLastName = view.findViewById(R.id.last_name);
        inpDue = view.findViewById(R.id.due);
        inpBorrow = view.findViewById(R.id.borrow);
        Button btnSave = view.findViewById(R.id.button_save);
        Button btnCancel = view.findViewById(R.id.button_cancel);
        btnSave.setOnClickListener(v -> onClickSave());
        btnCancel.setOnClickListener(v -> onClickCancel());

        mLastOperationKey = mSavedState.getString(KEY_OPERATION_KEY);
        isPersonFetched = mSavedState.getBoolean(KEY_PERSON_FETCHED);
        fetchPersonIfNeeded();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSavedState.put(KEY_OPERATION_KEY,mLastOperationKey);
        mSavedState.put(KEY_GET_PERSON,isPersonFetched);
    }

    @Override
    public void onStart() {
        super.onStart();
        TaskMaster taskMaster = viewModel.getTaskMaster();
        if (taskMaster.hasTask(mLastOperationKey)) {
            taskMaster.addTaskCallback(mLastOperationKey,this::onTaskResult);
        }
        else {
            onTaskResult(taskMaster.getResult(mLastOperationKey));
        }
    }

    private void onClickCancel() {
        // TODO: warn before discard
        navController.popBackStack();
    }

    private void onClickSave() {
        String firstName = inpFirstName.getText().toString();
        String lastName = inpLastName.getText().toString();
        String txtDue = inpDue.getText().toString();
        String txtBorrow = inpBorrow.getText().toString();

        containerFirstName.setError(null);
        containerDue.setError(null);
        containerBorrow.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(firstName)) {
            containerFirstName.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        if (TextUtils.isEmpty(txtDue)) {
            containerDue.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        else if (!Check.isNumeric(txtDue)) {
            containerDue.setError(getString(R.string.error_invalid_numeric_value));
            valid = false;
        }
        if (TextUtils.isEmpty(txtBorrow)) {
            containerBorrow.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        else if (!Check.isNumeric(txtBorrow)) {
            containerBorrow.setError(getString(R.string.error_invalid_numeric_value));
            valid = false;
        }
        if (!valid) return;

        Person person = new Person();
        person.setId(mPersonId);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setAmountDue(new BigDecimal(txtDue));
        person.setAmountBorrow(new BigDecimal(txtBorrow));
        mLastOperationKey = KEY_SAVE_PERSON;
        viewModel.savePerson(mLastOperationKey,person,this::onTaskResult);
    }

    private void fetchPersonIfNeeded() {
        Bundle args = getArguments();
        if (null != args) {
            mPersonId = args.getLong(Constants.EXTRA_PERSON);
        }
        if (mPersonId > 0 && !isPersonFetched) {
            mLastOperationKey = KEY_GET_PERSON;
            viewModel.findPersonById(mLastOperationKey,mPersonId,this::onTaskResult);
        }
    }

    private void onTaskResult(@Nullable TaskResult result) {
        if (null != result) {
            int taskCode = result.taskCode;
            if (Constants.DB_QUERY == taskCode) {
                onPersonFetched((Person) result.result,result);
            }
            else {
                onPersonSaved((Person) result.parameter,result);
            }
        }
    }

    private void onPersonFetched(@Nullable Person person, @NonNull TaskResult result) {
        if (null == person) {
            Toast.makeText(requireContext(),R.string.person_not_found,Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }
        isPersonFetched = true;
        inpFirstName.setText(person.getFirstName());
        inpLastName.setText(person.getLastName());
        inpDue.setText(person.getAmountDue().toPlainString());
        inpBorrow.setText(person.getAmountBorrow().toPlainString());
    }

    private void onPersonSaved(@NotNull Person person, @NonNull TaskResult result) {
        if (result.successful) {
            Toast.makeText(requireContext(),R.string.person_save_successful,Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(requireContext(), R.string.person_save_unsuccessful, Toast.LENGTH_SHORT).show();
        }
        navController.popBackStack();
    }
}
