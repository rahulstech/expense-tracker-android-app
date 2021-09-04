package dreammaker.android.expensetracker.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
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
import dreammaker.android.expensetracker.database.MoneyTransfer;
import dreammaker.android.expensetracker.util.CalculatorKeyboard;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.util.Helper;
import dreammaker.android.expensetracker.view.AccountsSpinnerAdapter;
import dreammaker.android.expensetracker.viewmodel.MoneyTransferViewModel;
import dreammaker.android.expensetracker.viewmodel.OperationCallback;

public class InputMoneyTransfer extends BaseFragment<InputMoneyTransfer.MoneyTransferViewHolder> implements View.OnClickListener {

    private static final String TAG = "InputMoneyTransfer";
    private static final String DATE_FORMAT = "dd-MMMM-yyyy";

    private MoneyTransferViewModel viewModel;
    private AccountsSpinnerAdapter accountsFromAdapter;
    private AccountsSpinnerAdapter accountsToAdapter;
    private NavController navController;
    private CalculatorKeyboard calculatorKeyboard;

    public InputMoneyTransfer() { super(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (Check.isNonNull(getActivity())) {
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(MoneyTransferViewModel.class);
        }
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
        viewModel.getSelectedMoneyTransferLiveData().observe(this, mt -> populateWithInitialValues(vh,mt));
        viewModel.getAccounts().observe(this, this::onAccountsFetched);
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull MoneyTransferViewHolder vh) {
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
        else if (v == getViewHolder().save)
            onSave();
    }

    private void onAccountsFetched(List<Account> accounts) {
        accountsFromAdapter.changeList(accounts);
        accountsToAdapter.changeList(accounts);
        MoneyTransfer mt = viewModel.getSelectedMoneyTransfer();
        int posFromAcc = accountsFromAdapter.getPositionForId(mt.getPayer_account_id());
        int posToAcc = accountsToAdapter.getPositionForId(mt.getPayee_account_id());
        Log.d(TAG,"posFromAcc: "+posFromAcc+" posToAcc: "+posFromAcc);
        getViewHolder().accountFrom.setSelection(posFromAcc);
        getViewHolder().accountTo.setSelection(posToAcc);
    }

    private void onChooseDate(){
        if (null == getContext()) return;
        Date date = viewModel.getSelectedMoneyTransfer().getWhen();
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            viewModel.getSelectedMoneyTransfer().setWhen(new Date(year,month,day));
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
        catch (Exception ex){
            Log.d(TAG,"exception occurred during amount calculate: "+ex.getMessage());
            getViewHolder().amountInput.setError(getString(R.string.error_invalid_amount));
            return;
        }
        Account payer = (Account) getViewHolder().accountFrom.getSelectedItem();
        Account payee = (Account) getViewHolder().accountTo.getSelectedItem();
        if (Check.isEquals(payer,payee)) {
            showQuickMessage(R.string.error_same_account_money_transfer);
            return;
        }
        else if (payer.getBalance() < _amount) {
            showQuickMessage(R.string.error_insufficient_balance);
            return;
        }
        String _description = getViewHolder().description.getText().toString();

        final MoneyTransfer mt = viewModel.getSelectedMoneyTransfer();
        mt.setAmount(_amount);
        mt.setPayer_account_id(payer.getAccountId());
        mt.setPayee_account_id(payee.getAccountId());
        mt.setDescription(_description);
        if (mt.getId() == 0)
            viewModel.insertMoneyTransfer(mt);
        else
            viewModel.updateMoneyTransfer(mt);
    }

    private void populateWithInitialValues(MoneyTransferViewHolder vh, MoneyTransfer mt) {
        Log.d(TAG,"populating money transfer: "+mt);
        vh.amount.setText(Helper.floatToString(mt.getAmount()));
        updateDateText();
        vh.description.setText(mt.getDescription());
        if (mt.getId() == 0) {
            Helper.setTitle(getActivity(),R.string.label_new_money_transfer);
        }
        else {
            Helper.setTitle(getActivity(),R.string.label_edit_money_transfer);
        }
    }

    private void onCompleteMoneyTransfer(boolean success) {
        showQuickMessage(success ? R.string.money_transfer_successful
                : R.string.money_transfer_unsuccessful);
    }

    private void updateDateText(){
        Date date = viewModel.getSelectedMoneyTransfer().getWhen();
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

        @Override
        public void onCompleteUpdate(boolean success) {
            onCompleteMoneyTransfer(success);
            if (success) {
                navController.popBackStack();
            }
        }
    };

    private CalculatorKeyboard getCalculatorKeyboard() {
        return new CalculatorKeyboard(getActivity(),getViewHolder().amount);
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
            date = findViewById(R.id.when);
            accountFrom = findViewById(R.id.account_from);
            accountTo = findViewById(R.id.account_to);
            description = findViewById(R.id.description);
            cancel = findViewById(R.id.cancel);
            save = findViewById(R.id.btn_restore);
        }
    }
}
