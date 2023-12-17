package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.ActivityModel;
import dreammaker.android.expensetracker.activity.ActivityModelProvider;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.databinding.InputPersonBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.PersonViewModel;

@SuppressWarnings("unused")
public class InputPerson extends Fragment {

    private static final String TAG = InputPerson.class.getSimpleName();

    private static final String KEY_PERSON_SET = "key_person_set";

    private NavController navController;

    private PersonViewModel mViewModel;

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
                .get(PersonViewModel.class);
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
            mViewModel.getPersonById(id).observe(this,this::onPersonFetched);
        }
        mViewModel.setCallbackIfTaskExists(PersonViewModel.SAVE_PERSON,getViewLifecycleOwner(),this::onPersonSaveComplete);
        if (requireActivity() instanceof ActivityModelProvider) {
            ActivityModel model = ((ActivityModelProvider) requireActivity()).getActivityModel();
            model.addOnBackPressedCallback(this,this::onBackPressed);
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
        requireActivity().setTitle(title);
    }

    private boolean onBackPressed() {
        if (hasAnyValueChanged()) {
            DialogUtil.createMessageDialog(requireContext(),getText(R.string.warning_not_saved),
                    getText(R.string.label_discard),null,
                    null,null,
                    getText(R.string.label_exit),(di,which)->exit(),
                    false).show();
            return true;
        }
        return false;
    }

    private boolean hasAnyValueChanged() {
        CharSequence firstName = mBinding.firstName.getText();
        CharSequence lastName = mBinding.lastName.getText();
        Currency due = TextUtil.tryConvertToCurrencyOrNull(mBinding.due.getEditableText().toString());
        Currency borrow = TextUtil.tryConvertToCurrencyOrNull(mBinding.borrow.getEditableText().toString());
        if (isEditOperation()) {
            //noinspection ConstantConditions
            return null != mPerson && (!TextUtils.equals(mPerson.getFirstName(),firstName))
                    || !TextUtils.equals(mPerson.getLastName(),lastName)
                    || !mPerson.getDue().equals(due)
                    || !mPerson.getBorrow().equals(borrow);
        }
        else {
            return !TextUtils.isEmpty(firstName) || !TextUtils.isEmpty(lastName)
                    || !Currency.ZERO.equals(due) || !Currency.ZERO.equals(borrow);
        }
    }

    private void onToggleCalculator(@NonNull View view) {
        // TODO: toggle calculator
    }

    private void onClickSave() {
        if (!validate()) {
            return;
        }
        CharSequence firstName = mBinding.firstName.getEditableText().toString().trim();
        CharSequence lastName = mBinding.lastName.getEditableText().toString().trim();
        Currency due = TextUtil.tryConvertToCurrencyOrNull(mBinding.due.getText());
        Currency borrow = TextUtil.tryConvertToCurrencyOrNull(mBinding.borrow.getText());
        Person person = new Person();
        if (isEditOperation()) {
            if (!hasAnyValueChanged()) {
                ToastUtil.showMessageShort(requireContext(),R.string.message_no_change_no_save);
                exit();
                return;
            }
            person.setId(getPersonId());
        }
        person.setFirstName(firstName.toString());
        person.setLastName(lastName.toString());
        //noinspection ConstantConditions
        person.setDue(due);
        //noinspection ConstantConditions
        person.setBorrow(borrow);
        mViewModel.savePerson(person).observe(getViewLifecycleOwner(),this::onPersonSaveComplete);
    }

    @SuppressWarnings("UnusedAssignment")
    private boolean validate() {
        CharSequence firstName = mBinding.firstName.getEditableText().toString().trim();
        CharSequence txtDue = mBinding.due.getEditableText().toString().trim();
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
            mBinding.containerBorrow.setError(getText(R.string.error_empty_input));
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
            ToastUtil.showErrorShort(requireContext(),R.string.error_person_not_found);
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
        Person person = (Person) result.getResult();
        if (null == person) {
            ToastUtil.showErrorShort(requireContext(),R.string.error_save);
            return;
        }
        ToastUtil.showSuccessShort(requireContext(),R.string.person_save_successful);
        if (isEditOperation()) {
            exit();
        }
        else {
            showPersonDetails(person);
        }
    }

    private void showPersonDetails(Person person) {
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ID,person.getId());
        navController.navigate(R.id.action_input_person_to_person_details,args);
    }

    private void exit() {
        navController.popBackStack();
    }
}
