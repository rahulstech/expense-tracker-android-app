package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

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
import dreammaker.android.expensetracker.database.model.Account;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.viewmodel.AccountsViewModel;
import dreammaker.android.expensetracker.viewmodel.SavedStateViewModel;

public class InputAccount extends Fragment {

    private static final String KEY_OPERATION_KEY = "operation_key";

    private static final String KEY_ACCOUNT_FETCHED = "account_fetched";

    private static final String KEY_QUERY_ACCOUNT = "query_account";

    private static final String KEY_SAVE_ACCOUNT = "save_account";


    private AccountsViewModel viewModel;
    private SavedStateViewModel mSavedState;
    private NavController navController;

    private TextInputLayout containerName;
    private TextInputLayout containerBalance;
    private EditText inpName;
    private EditText inpBalance;

    private String mLastOperationKey = null;
    private long mAccountId = 0;
    private boolean isAccountFetched = false;

    public InputAccount(){
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.input_account,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mSavedState = new ViewModelProvider(this).get(SavedStateViewModel.class);
        viewModel = new ViewModelProvider(requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(AccountsViewModel.class);
        containerName = view.findViewById(R.id.container_name);
        containerBalance = view.findViewById(R.id.container_balance);
        inpName = view.findViewById(R.id.name);
        inpBalance = view.findViewById(R.id.balance);
        Button btnCancel = view.findViewById(R.id.button_cancel);
        Button btnSave = view.findViewById(R.id.button_save);
        btnSave.setOnClickListener(v -> onClickSave());
        btnCancel.setOnClickListener(v -> onClickCancel());
        mLastOperationKey = mSavedState.getString(KEY_OPERATION_KEY);
        isAccountFetched = mSavedState.getBoolean(KEY_ACCOUNT_FETCHED);
        fetchAccountIfNeeded();
    }

    @Override
    public void onStart() {
        super.onStart();
        TaskMaster taskMaster = viewModel.getTaskMaster();
        if (null != mLastOperationKey && taskMaster.hasTask(mLastOperationKey)) {
            taskMaster.addTaskCallback(mLastOperationKey,this::onTaskResult);
        }
        else {
            onTaskResult(taskMaster.getResult(mLastOperationKey));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSavedState.put(KEY_OPERATION_KEY,mLastOperationKey);
        mSavedState.put(KEY_ACCOUNT_FETCHED,isAccountFetched);
    }

    private void onClickCancel() {
        // TODO: warn before discard
        navController.popBackStack();
    }

    private void onClickSave() {
        String name = inpName.getText().toString();
        String txtBalance = inpBalance.getText().toString();
        containerName.setError(null);
        containerBalance.setError(null);
        // validate inputs
        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            containerName.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        if (TextUtils.isEmpty(txtBalance)) {
            containerBalance.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        else if (!Check.isNumeric(txtBalance)) {
            containerBalance.setError(getString(R.string.error_invalid_numeric_value));
            valid = false;
        }
        // at least one invalid input exists, don't go further
        if (!valid) return;
        // all inputs are valid proceed to save
        Account account = new Account();
        account.setId(mAccountId);
        account.setName(name);
        account.setBalance(new BigDecimal(txtBalance));
        mLastOperationKey = KEY_SAVE_ACCOUNT;
        viewModel.saveAccount(mLastOperationKey,account,this::onTaskResult);
    }

    private void fetchAccountIfNeeded() {
        Bundle args = getArguments();
        if (null != args) {
            mAccountId = args.getLong(Constants.EXTRA_ACCOUNT);
        }
        if (mAccountId > 0 && !isAccountFetched) {
            mLastOperationKey = KEY_QUERY_ACCOUNT;
            viewModel.findAccountById(mLastOperationKey, mAccountId, result -> onAccountFetched((Account) result.result, result));
        }
    }

    private void onTaskResult(@Nullable TaskResult result) {
        if (null != result) {
            int taskCode = result.taskCode;
            if (Constants.DB_QUERY == taskCode) {
                onAccountFetched((Account) result.result,result);
            }
            else {
                onAccountSaved((Account) result.parameter,result);
            }
        }
    }

    private void onAccountFetched(@Nullable Account account, @NonNull TaskResult result) {
        if (null == account) {
            Toast.makeText(requireContext(),R.string.account_not_found,Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }
        isAccountFetched = true;
        inpName.setText(account.getName());
        inpBalance.setText(account.getBalance().toPlainString());
    }

    private void onAccountSaved(@NonNull Account account, @NonNull TaskResult result) {
        if (result.successful) {
            Toast.makeText(requireContext(),R.string.account_save_successful,Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(requireContext(),R.string.account_save_unsuccessful,Toast.LENGTH_SHORT).show();
        }
        navController.popBackStack();
    }
}
