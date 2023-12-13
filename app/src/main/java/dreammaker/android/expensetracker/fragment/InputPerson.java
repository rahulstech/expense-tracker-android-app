package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.databinding.InputPersonBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.InputPersonViewModel;

public class InputPerson extends Fragment {

    private static final String TAG = InputPerson.class.getSimpleName();

    private static final String KEY_PERSON_SET = "key_person_set";

    private NavController navController;

    private InputPersonViewModel mViewModel;

    private InputPersonBinding mBinding;

    private PersonModel mPerson;
    private boolean mPersonSet = false;

    public InputPerson(){
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(InputPersonViewModel.class);
    }

    private boolean isEditOperation() {
        return Constants.ACTION_UPDATE.equals(requireArguments().getString(Constants.EXTRA_ACTION));
    }

    private long getPersonId() {
        return requireArguments().getLong(Constants.EXTRA_ID,0L);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = InputPersonBinding.inflate(inflater,container,false);
        if (isEditOperation()) {
            long id = getPersonId();
            mViewModel.getPersonById(id).observe(getViewLifecycleOwner(),this::onPersonFetched);
        }
        LiveData<DBViewModel.AsyncQueryResult> result = mViewModel.getLiveResult(InputPersonViewModel.SAVE_PERSON);
        if (null != result) {
            result.observe(getViewLifecycleOwner(),this::onPersonSaveComplete);
        }
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mBinding.containerDue.setEndIconOnClickListener(this::onToggleCalculator);
        mBinding.containerBorrow.setEndIconOnClickListener(this::onToggleCalculator);
        mBinding.buttonSave.setOnClickListener(v -> onClickSave());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mPersonSet = savedInstanceState.getBoolean(KEY_PERSON_SET,false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_PERSON_SET,mPersonSet);
    }

    private void setTitle() {
        CharSequence title = isEditOperation() ? getText(R.string.label_update_person) : getText(R.string.label_insert_person);
        mBinding.actionBar.toolbar.setTitle(title);
    }

    private void onBackPressed() {
        if (hasAnyValueChanged()) {
            DialogUtil.createMessageDialog(requireContext(),R.string.warning_not_saved,
                    R.string.label_discard,null,R.string.label_exit,(di,which)->exit(),
                    false);
            return;
        }
        exit();
    }

    private boolean hasAnyValueChanged() {
        CharSequence firstName = mBinding.firstName.getText();
        CharSequence lastName = mBinding.lastName.getText();
        CharSequence txtDue = mBinding.due.getText();
        CharSequence txtBorrow = mBinding.borrow.getText();
        if (isEditOperation()) {
            return mPersonSet && (!TextUtils.equals(mPerson.getFirstName(),firstName)) || !TextUtils.equals(mPerson.getLastName(),lastName)
                    || !mPerson.getDue().equals(TextUtil.tryConvertToCurrencyOrNull(txtDue))
                    || !mPerson.getBorrow().equals(TextUtil.tryConvertToCurrencyOrNull(txtBorrow));
        }
        else {
            return !TextUtils.isEmpty(firstName) || !TextUtils.isEmpty(lastName)
                    || !TextUtils.isEmpty(txtDue) || !TextUtils.isEmpty(txtBorrow);
        }
    }

    private void onToggleCalculator(@NonNull View view) {
        // TODO: toggle calculator
    }

    private void onClickSave() {
        if (!validate()) {
            return;
        }

        CharSequence firstName = mBinding.firstName.getText();
        CharSequence lastName = mBinding.lastName.getText();
        Currency due = TextUtil.tryConvertToCurrencyOrNull(mBinding.due.getText());
        Currency borrow = TextUtil.tryConvertToCurrencyOrNull(mBinding.borrow.getText());

        Person person = new Person();
        if (isEditOperation()) {
            if (!hasAnyValueChanged()) {
                Toast.makeText(requireContext(),R.string.message_no_change_no_save,Toast.LENGTH_SHORT).show();
                exit();
                return;
            }
            person.setId(getPersonId());
        }
        person.setFirstName(firstName.toString());
        person.setLastName(lastName.toString());
        person.setDue(due);
        person.setBorrow(borrow);
        mViewModel.savePerson(person).observe(getViewLifecycleOwner(),this::onPersonSaveComplete);
    }

    private boolean validate() {
        CharSequence firstName = mBinding.firstName.getText();
        CharSequence txtDue = mBinding.due.getText();
        CharSequence txtBorrow = mBinding.borrow.getText();

        mBinding.containerFirstName.setError(null);
        mBinding.containerDue.setError(null);
        mBinding.containerDue.setError(null);

        boolean valid;
        if (TextUtils.isEmpty(firstName)) {
            mBinding.containerFirstName.setError(getText(R.string.error_empty_input));
            valid = false;
        }
        if (TextUtils.isEmpty(txtDue)) {
            mBinding.containerDue.setError(getText(R.string.error_empty_input));
            valid = false;
        }
        else {
            Currency due = TextUtil.tryConvertToCurrencyOrNull(txtDue);
            valid = due != null;
            if (!valid) {
                mBinding.containerDue.setError(getText(R.string.error_invalid_numeric_value));
            }
        }
        if (TextUtils.isEmpty(txtBorrow)) {
            mBinding.containerBorrow.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        else {
            Currency borrow = TextUtil.tryConvertToCurrencyOrNull(txtBorrow);
            valid = null != borrow;
            if (!valid) {
                mBinding.containerBorrow.setError(getText(R.string.error_invalid_numeric_value));
            }
        }
        return valid;
    }

    private void onPersonFetched(@Nullable PersonModel person) {
        if (null == person) {
            Toast.makeText(requireContext(),R.string.error_person_not_found,Toast.LENGTH_SHORT).show();
            exit();
            return;
        }
        mPerson = person;
        if (!mPersonSet) {
            mBinding.firstName.setText(person.getFirstName());
            mBinding.lastName.setText(person.getLastName());
            mBinding.due.setText(person.getDue().toString());
            mBinding.borrow.setText(person.getBorrow().toString());
        }
        mPersonSet = true;
    }

    private void onPersonSaveComplete(@NonNull DBViewModel.AsyncQueryResult result) {
        if (null == result.getResult()) {
            Toast.makeText(requireContext(),R.string.error_save,Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(),R.string.person_save_successful,Toast.LENGTH_SHORT).show();
        if (isEditOperation()) {
            exit();
        }
        else {
            // TODO: show new person details
        }
    }

    private void exit() {
        // TODO: implement exit method
        navController.popBackStack();
    }
}
