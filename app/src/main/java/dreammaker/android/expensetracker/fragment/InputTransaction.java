package dreammaker.android.expensetracker.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.util.CalculatorKeyboard;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.util.Helper;
import dreammaker.android.expensetracker.view.AccountsSpinnerAdapter;
import dreammaker.android.expensetracker.view.PersonsSpinnerAdapter;
import dreammaker.android.expensetracker.viewmodel.OperationCallback;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

import static dreammaker.android.expensetracker.database.Transaction.TYPE_CREDIT;

public class InputTransaction extends BaseFragment<InputTransaction.InputTransactionViewHolder> implements View.OnClickListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "InputTransaction";

    private static final String DATE_FORMAT = "dd-MMMM-yyyy";

    private TransactionsViewModel viewModel;
    private AccountsSpinnerAdapter accountsAdapter;
    private PersonsSpinnerAdapter personsAdapter;
    private NavController navController;
    private CalculatorKeyboard calculatorKeyboard;

    public InputTransaction() { super(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (Check.isNonNull(getActivity())){
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
        }
    }

    @NonNull
    @Override
    protected InputTransactionViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return new InputTransactionViewHolder(
                inflater.inflate(R.layout.input_transaction, container, false));
    }

    @Override
    protected void onFragmentViewHolderCreated(@NonNull InputTransactionViewHolder vh) {
        accountsAdapter = new AccountsSpinnerAdapter(getContext());
        personsAdapter = new PersonsSpinnerAdapter(getContext());
        calculatorKeyboard = getCalculatorKeyboard();
        vh.date.setOnClickListener(this);
        vh.account.setAdapter(accountsAdapter);
        vh.account.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.getWorkingTransaction().setAccountId(id);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        vh.person.setAdapter(personsAdapter);
        vh.person.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.getWorkingTransaction().setPersonId(PersonsSpinnerAdapter.NO_ID == id ? null
                        : id);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        vh.cancel.setOnClickListener(this);
        vh.addCredit.setOnClickListener(this);
        vh.addDebit.setOnClickListener(this);

        viewModel.setOperationCallback(callback);
        viewModel.getAllAccountsNameAndId().observe(this, this::onAccountsFetched);
        viewModel.getAllPersonNamAndId().observe(this, this::onPeopleFetched);
        viewModel.getWorkingTransactionLiveData().observe(this, transaction -> populateWithInitialValues(vh,transaction));
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull InputTransactionViewHolder vh) {
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
        if (v == getViewHolder().date)
            onChooseDate();
        else if (v == getViewHolder().cancel)
            onCancel();
        else if (v == getViewHolder().addCredit)
            onSave(TYPE_CREDIT);
        else if (v == getViewHolder().addDebit)
            onSave(Transaction.TYPE_DEBIT);
    }

    private void onAccountsFetched(List<Account> accounts){
        accountsAdapter.changeList(accounts);
        setInitialSelectedAccount();
    }

    private void onPeopleFetched(List<Person> people){
        personsAdapter.changeList(people);
        setInitialSelectedPerson();
    }

    private void setInitialSelectedAccount(){
        final long accountId = viewModel.getWorkingTransaction().getAccountId();
        int selectedPosition = accountsAdapter.getPositionForId(accountId);
        if (AccountsSpinnerAdapter.NO_POSITION != selectedPosition)
            getViewHolder().account.setSelection(selectedPosition);
        else
            getViewHolder().account.setSelection(0);
    }

    private void setInitialSelectedPerson(){
        long personId = null == viewModel.getWorkingTransaction().getPersonId() ? PersonsSpinnerAdapter.NO_ID
                : viewModel.getWorkingTransaction().getPersonId();
        int selectedPosition = personsAdapter.getPositionForId(personId) ;
        if (PersonsSpinnerAdapter.NO_POSITION != selectedPosition)
            getViewHolder().person.setSelection(selectedPosition);
        else
            getViewHolder().person.setSelection(0);
    }

    private void populateWithInitialValues(InputTransactionViewHolder vh, Transaction transaction) {
        if (DEBUG) Log.d(TAG,transaction.toString());
        vh.amount.setText(Helper.floatToString(transaction.getAmount()));
        vh.date.setText(transaction.getDate().format(DATE_FORMAT));
        setInitialSelectedAccount();
        setInitialSelectedPerson();
        vh.description.setText(transaction.getDescription());
        if (transaction.getTransactionId() > 0)
            Helper.setTitle(getActivity(),R.string.label_update_transaction);
        else
            Helper.setTitle(getActivity(),R.string.label_insert_transaction);
    }

    private void onChooseDate(){
        if (null == getContext()) return;
        Date date = viewModel.getWorkingTransaction().getDate();
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            Date newDate = new Date(year,month,day);
            viewModel.getWorkingTransaction().setDate(newDate);
            getViewHolder().date.setText(newDate.format(DATE_FORMAT));
        }, date.getYear(),
               date.getMonth(),
                date.getDayOfMonth())
                .show();
    }

    private void onCancel(){
        navController.popBackStack();
    }

    private void onSave(int transactionType){
        getViewHolder().amountInput.setError(null);

        float _amount;
        try{
            Editable editable = getViewHolder().amount.getText();
            _amount = calculatorKeyboard.calculate(editable);
            if (_amount < 0){
                getViewHolder().amountInput.setError(getString(R.string.error_negative_amount));
                return;
            }
        }
        catch (Exception e){
            getViewHolder().amountInput.setError(getString(R.string.error_invalid_amount));
            return;
        }
        Account account = (Account) getViewHolder().account.getSelectedItem();
        if (transactionType == TYPE_CREDIT && account.getBalance() < _amount) {
            showQuickMessage(R.string.error_insufficient_balance);
            return;
        }
        final Transaction transaction = viewModel.getWorkingTransaction();
        transaction.setAmount(_amount);
        transaction.setType(transactionType);
        transaction.setDescription(getViewHolder().description.getText().toString());
        if (transaction.getTransactionId() > 0) {
            viewModel.updateTransaction(transaction);
        }
        else {
            viewModel.insertTransaction(transaction);
        }
    }

    private void onCompleteSave(boolean success){
        showQuickMessage( success ? R.string.transaction_save_successful
                : R.string.transaction_save_unsuccessful);
        if (success) {
            viewModel.reloadWithLastFilterTransactionParameters();
        }
    }

    private final OperationCallback callback = new OperationCallback(){
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

    private void showQuickMessage(@StringRes int id) {
        MainActivity.showQuickMessage(getActivity(),id, android.R.string.ok);
    }

    private CalculatorKeyboard getCalculatorKeyboard() {
        return new CalculatorKeyboard(getActivity(),getViewHolder().amount);
    }

    public static class InputTransactionViewHolder extends BaseFragment.FragmentViewHolder{
        TextInputLayout amountInput;
        EditText amount;
        Button date;
        Spinner account;
        Spinner person;
        EditText description;
        Button cancel;
        Button addCredit;
        Button addDebit;

        public InputTransactionViewHolder(@NonNull View root) {
            super(root);amountInput = findViewById(R.id.amount_input);
            amount = findViewById(R.id.amount);
            date = findViewById(R.id.when);
            account = findViewById(R.id.account);
            person = findViewById(R.id.person);
            description = findViewById(R.id.description);
            cancel = findViewById(R.id.cancel);
            addCredit = findViewById(R.id.add_credit);
            addDebit = findViewById(R.id.add_debit);
        }
    }
}
