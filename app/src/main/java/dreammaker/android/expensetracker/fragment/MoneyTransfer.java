package dreammaker.android.expensetracker.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.util.CalculatorKeyboard;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.util.Helper;
import dreammaker.android.expensetracker.view.AccountsSpinnerAdapter;
import dreammaker.android.expensetracker.viewmodel.OperationCallback;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

import static dreammaker.android.expensetracker.database.Transaction.TYPE_CREDIT;
import static dreammaker.android.expensetracker.database.Transaction.TYPE_DEBIT;
import static dreammaker.android.expensetracker.util.Helper.CATEGORY;
import static dreammaker.android.expensetracker.util.Helper.CATEGORY_RECEIVE_MONEY;
import static dreammaker.android.expensetracker.util.Helper.CATEGORY_SEND_MONEY;
import static dreammaker.android.expensetracker.util.Helper.EXTRA_ID;

public class MoneyTransfer extends BaseFragment<MoneyTransfer.MoneyTransferViewHolder> implements View.OnClickListener {

    private static final String TAG = "MoneyTransfer";
    private static final String DATE_FORMAT = "dd-MMMM-yyyy";
    private static final int ID_SAVED_DATA = 1755;
    
    private TransactionsViewModel viewModel;
    private AccountsSpinnerAdapter accountsFromAdapter;
    private AccountsSpinnerAdapter accountsToAdapter;
    private NavController navController;
    private CalculatorKeyboard calculatorKeyboard;
    private Date date;

    public MoneyTransfer() { super(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (Check.isNonNull(getActivity())) {
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
            date = viewModel.getSavedData(ID_SAVED_DATA, new Date());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        viewModel.putSavedData(ID_SAVED_DATA, date);
    }

    @NonNull
    @Override
    protected MoneyTransferViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return new MoneyTransferViewHolder(inflater.
                inflate(R.layout.money_transfer, container, false));
    }

    @Override
    protected void onFragmentViewHolderCreated(@NonNull MoneyTransferViewHolder vh) {
        accountsFromAdapter = new AccountsSpinnerAdapter(getContext());
        accountsToAdapter = new AccountsSpinnerAdapter(getContext());
        calculatorKeyboard = getCalculatorKeyboard();
        vh.date.setOnClickListener(this);
        vh.accountFrom.setAdapter(accountsFromAdapter);
        vh.accountTo.setAdapter(accountsToAdapter);
        vh.cancel.setOnClickListener(this);
        vh.save.setOnClickListener(this);
        viewModel.setOperationCallback(callback);
        viewModel.getAllAccountsNameAndId().observe(this, this::onAccountsFetched);
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull MoneyTransferViewHolder vh) {
        navController = Navigation.findNavController(vh.getRoot());
        calculatorKeyboard.registerEditText(vh.amount);
        Helper.setTitle(getActivity(), R.string.label_money_transfer);
        updateDateText();
    }

    @Override
    public void onPause() {
        calculatorKeyboard.unregisterEditText(getViewHolder().amount);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v == getViewHolder().date)
            onChooseDate();
        else if (v == getViewHolder().cancel)
            onCancel();
        else if (v == getViewHolder().save)
            onSave();
    }

    private void onAccountsFetched(List<Account> accounts) {
        accountsFromAdapter.changeList(accounts);
        accountsToAdapter.changeList(accounts);
        if (hasArguments()) {
            Bundle args = getArguments();
            int category = args.getInt(CATEGORY, 0);
            long id = args.getLong(EXTRA_ID, 0);
            if (CATEGORY_SEND_MONEY == category)
                setInitialAccount(category, accountsFromAdapter.getPositionForId(id));
            else if (CATEGORY_RECEIVE_MONEY == category)
                setInitialAccount(category, accountsToAdapter.getPositionForId(id));
        }
    }

    private void setInitialAccount(int category, int selection) {
        if (AccountsSpinnerAdapter.NO_POSITION == selection) return;
        if (CATEGORY_SEND_MONEY == category)
            getViewHolder().accountFrom.setSelection(selection);
        else
            getViewHolder().accountTo.setSelection(selection);
    }

    private void onChooseDate(){
        if (null == getContext()) return;
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            date.set(year, month, day);
            updateDateText();
        }, date.getYear(),
                date.getMonth(),
                date.getDayOfMonth())
                .show();
    }

    private void onCancel(){
        navController.popBackStack();
    }

    private void onSave(){
        getViewHolder().amountInput.setError(null);

        float _amount;
        try{
            _amount = calculatorKeyboard.calculate(getViewHolder().amount.getEditableText());
            if (_amount < 0){
                getViewHolder().amountInput.setError(getString(R.string.error_negative_amount));
                return;
            }
        }
        catch (Exception ignored){
            getViewHolder().amountInput.setError(getString(R.string.error_invalid_amount));
            return;
        }
        long _accountFrom = getViewHolder().accountFrom.getSelectedItemId();
        long _accountTo = getViewHolder().accountTo.getSelectedItemId();
        if (_accountFrom == _accountTo) {
            showQuickMessage(R.string.error_same_account_money_transfer);
            return;
        }
        String _description = getViewHolder().description.getText().toString();
        viewModel.transferMoney(new Transaction(_accountFrom, null,
                        _amount, TYPE_CREDIT, date, _description),
                new Transaction(_accountTo, null,
                        _amount, TYPE_DEBIT, date, _description));
    }

    private void onCompleteMoneyTransfer(boolean success) {
        showQuickMessage(success ? R.string.money_transfer_successful
                : R.string.money_transfer_unsuccessful);
    }

    private void updateDateText(){
        getViewHolder().date.setText(date.format(DATE_FORMAT));
    }

    private void showQuickMessage(@StringRes int id) {
        MainActivity.showQuickMessage(getActivity(),id,android.R.string.ok);
    }

    private final OperationCallback callback = new OperationCallback() {
        @Override
        public void onCompleteInsert(boolean success) {
            onCompleteMoneyTransfer(success);
        }
    };

    private CalculatorKeyboard getCalculatorKeyboard() {
        return ((MainActivity) getActivity()).getCalculatorKeyboard();
    }

    static class MoneyTransferViewHolder extends BaseFragment.FragmentViewHolder{

        TextInputLayout amountInput;
        EditText amount;
        Button date;
        Spinner accountFrom;
        Spinner accountTo;
        EditText description;
        Button cancel;
        Button save;

        MoneyTransferViewHolder(@NonNull View root) {
            super(root);

            amountInput = findViewById(R.id.amount_input);
            amount = findViewById(R.id.amount);
            date = findViewById(R.id.date);
            accountFrom = findViewById(R.id.account_from);
            accountTo = findViewById(R.id.account_to);
            description = findViewById(R.id.description);
            cancel = findViewById(R.id.cancel);
            save = findViewById(R.id.save);
        }
    }
}
