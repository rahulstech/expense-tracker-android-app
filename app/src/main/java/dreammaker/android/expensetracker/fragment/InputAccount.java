package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.util.CalculatorKeyboard;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Helper;
import dreammaker.android.expensetracker.viewmodel.AccountsViewModel;
import dreammaker.android.expensetracker.viewmodel.OperationCallback;

public class InputAccount extends BaseFragment<InputAccount.InputAccountViewHolder> implements View.OnClickListener {

    private AccountsViewModel viewModel;
    private NavController navController;
    private CalculatorKeyboard calculatorKeyboard;

    public InputAccount(){
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (Check.isNonNull(getActivity())) {
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(AccountsViewModel.class);
        }
    }

    @NonNull
    @Override
    protected InputAccountViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return new InputAccountViewHolder(inflater.
                inflate(R.layout.input_account, container, false));
    }

    @Override
    protected void onFragmentViewHolderCreated(@NonNull InputAccountViewHolder vh) {
        calculatorKeyboard = getCalculatorKeyboard();
        vh.save.setOnClickListener(this);
        vh.cancel.setOnClickListener(this);
        vh.accountName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.getSelectedAccount().setAccountName(s.toString());
            }
        });
        viewModel.setOperationCallback(callback);
        viewModel.getSelectedAccountLiveData().observe(this, account -> populateWithInitialValues(vh,account));
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull InputAccountViewHolder vh) {
        navController = Navigation.findNavController(vh.getRoot());
        calculatorKeyboard.registerEditText(vh.balance);
    }

    @Override
    public void onPause() {
        calculatorKeyboard.unregisterEditText(getViewHolder().balance);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if(v == getViewHolder().cancel){
            onCancel();
        }
        else if(v == getViewHolder().save){
            onSave();
        }
    }

    private void onCancel() {
        navController.popBackStack();
    }

    private void onSave() {
        getViewHolder().accountNameInput.setError(null);
        getViewHolder().balanceInput.setError(null);

        final Account account = viewModel.getSelectedAccount();
        try{
            float _balance = calculatorKeyboard.calculate(getViewHolder().balance.getEditableText());
            account.setBalance(_balance);
        }
        catch (Exception e){
            getViewHolder().balanceInput.setError(getString(R.string.error_invalid_amount));
            return;
        }

        String name = account.getAccountName();
        if (Check.isEmptyString(name)){
            getViewHolder().accountNameInput.setError(getText(R.string.error_empty_account_name));
            return;
        }

        if (account.getAccountId() <= 0){
            viewModel.insertAccount(account);
        }
        else {
            viewModel.updateAccount(account);
        }
    }

    private void populateWithInitialValues(InputAccountViewHolder vh, Account account) {
        vh.accountName.setText(account.getAccountName());
        vh.balance.setText(Helper.floatToString(account.getBalance()));
        if (account.getAccountId() <= 0)
            Helper.setTitle(getActivity(),R.string.label_insert_account);
        else
            Helper.setTitle(getActivity(),R.string.label_update_account);
    }

    private void onCompleteSave(boolean success){
        showQuickMessage(success ? R.string.account_save_successful
                : R.string.account_save_unsuccessful);
    }

    private void showQuickMessage(@StringRes int id) {
        MainActivity.showQuickMessage(getActivity(),id, android.R.string.ok);
    }

    private CalculatorKeyboard getCalculatorKeyboard() {
        return ((MainActivity) getActivity()).getCalculatorKeyboard();
    }

    private OperationCallback callback = new OperationCallback(){
        @Override
        public void onCompleteInsert(boolean success) {
            onCompleteSave(success);
        }

        @Override
        public void onCompleteUpdate(boolean success) {
            onCompleteSave(success);
            navController.popBackStack();
        }
    };

    public static class InputAccountViewHolder extends BaseFragment.FragmentViewHolder{

        TextInputLayout accountNameInput;
        EditText accountName;
        TextInputLayout balanceInput;
        EditText balance;
        Button cancel;
        Button save;

        public InputAccountViewHolder(@NonNull View root) {
            super(root);
            accountNameInput = findViewById(R.id.account_name_input);
            accountName = findViewById(R.id.person_name);
            cancel = findViewById(R.id.cancel);
            save = findViewById(R.id.save);
            balanceInput = findViewById(R.id.balance_input);
            balance = findViewById(R.id.balance);
        }
    }
}
