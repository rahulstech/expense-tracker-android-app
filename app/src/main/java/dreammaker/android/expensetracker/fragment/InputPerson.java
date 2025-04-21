package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.util.CalculatorKeyboard;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Helper;
import dreammaker.android.expensetracker.viewmodel.OperationCallback;
import dreammaker.android.expensetracker.viewmodel.PersonsViewModel;

public class InputPerson extends BaseFragment<InputPerson.InputPersonViewHolder> implements View.OnClickListener {

    private PersonsViewModel viewModel;
    private NavController navController;
    private CalculatorKeyboard calculatorKeyboard;

    public InputPerson(){
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (Check.isNonNull(getActivity())){
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity()
                            .getApplication()))
                    .get(PersonsViewModel.class);
        }
    }

    @NonNull
    @Override
    protected InputPersonViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return new InputPersonViewHolder(
                inflater.inflate(R.layout.input_person, container, false));
    }

    @Override
    protected void onFragmentViewHolderCreated(@NonNull InputPersonViewHolder vh) {
        calculatorKeyboard = getCalculatorKeyboard();
        vh.cancel.setOnClickListener(this);
        vh.save.setOnClickListener(this);
        viewModel.setOperationCallback(callback);
        viewModel.getSelectedPersonLiveData().observe(this, person -> populateWithInitialValue(vh,person));
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull InputPersonViewHolder vh) {
        navController = Navigation.findNavController(vh.getRoot());
    }

    @Override
    public void onPause() {
        calculatorKeyboard.hideCalculatorKeyboard();
        super.onPause();
    }

    @Override
    public boolean onBackPressed() {
        if (calculatorKeyboard.onBackPressed()) return true;
        return super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if(v == getViewHolder().cancel)
            onCancel();
        else if(v == getViewHolder().save)
            onSave();
    }

    private void onCancel() {
        navController.popBackStack();
    }

    private void onSave() {
        getViewHolder().personNameInput.setError(null);
        getViewHolder().dueInput.setError(null);
        boolean isDue = getViewHolder().typeDue.isChecked();

        final Person person = viewModel.getSelectedPerson();
        float _due;
        try{
            _due = calculatorKeyboard.calculate(getViewHolder().due.getEditableText());
            if (_due < 0) {
                throw new IllegalArgumentException("negative due");
            }
        }
        catch (Exception e){
            getViewHolder().dueInput.setError(getString(R.string.error_invalid_amount));
            return;
        }
        String name = getViewHolder().personName.getText().toString();
        if(Check.isEmptyString(name)){
            getViewHolder().personNameInput.setError(getString(R.string.error_empty_person_name));
            return;
        }
        person.setPersonName(name);
        person.setDue(isDue ? _due : -_due);

        if (person.getPersonId() <= 0)
            viewModel.insertPerson(person);
        else
            viewModel.updatePerson(person);
    }

    private void populateWithInitialValue(InputPersonViewHolder vh, Person person) {
        vh.personName.setText(person.getPersonName());
        float amount = person.getDue();
        vh.due.setText(Helper.floatToString(Math.abs(amount)));
        if (amount >= 0)
            vh.typeDue.setChecked(true);
        else
            vh.typeAdvanced.setChecked(true);
        if (person.getPersonId() <= 0)
            Helper.setTitle(getActivity(), R.string.label_insert_person);
        else
            Helper.setTitle(getActivity(), R.string.label_update_person);
    }

    private void onCompleteSave(boolean success){
        showQuickMessage(success ? R.string.person_save_successful
                : R.string.person_save_unsuccessful);
    }

    private void showQuickMessage(@StringRes int id) {
        MainActivity.showQuickMessage(getActivity(),id, android.R.string.ok);
    }

    private CalculatorKeyboard getCalculatorKeyboard() {
        return new CalculatorKeyboard(getActivity(),getViewHolder().due);
    }

    private OperationCallback callback = new OperationCallback(){
        @Override
        public void onCompleteInsert(boolean success) {
            onCompleteSave(success);
        }

        @Override
        public void onCompleteUpdate(boolean success) {
            onCompleteSave(success);
            if (success) {
                navController.popBackStack();
            }
        }
    };

    public static class InputPersonViewHolder extends BaseFragment.FragmentViewHolder{

        TextInputLayout personNameInput;
        TextInputLayout dueInput;
        EditText personName;
        EditText due;
        Button cancel;
        Button save;
        RadioButton typeDue;
        RadioButton typeAdvanced;

        public InputPersonViewHolder(@NonNull View root) {
            super(root);
            personNameInput = findViewById(R.id.person_name_input);
            personName = findViewById(R.id.to_account);
            cancel = findViewById(R.id.btn_cancel);
            save = findViewById(R.id.btn_save);
            dueInput = findViewById(R.id.due_input);
            due = findViewById(R.id.due);
            typeDue = findViewById(R.id.type_due);
            typeAdvanced = findViewById(R.id.type_advanced);
        }
    }
}
